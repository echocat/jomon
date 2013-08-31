/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012-2013 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.runtime.concurrent;

import org.echocat.jomon.runtime.util.Duration;
import org.echocat.jomon.runtime.util.ServiceTemporaryUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.*;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@ThreadSafe
public class BatchedHitWriter {

    private static final Logger LOG = LoggerFactory.getLogger(BatchedHitWriter.class);

    @NotThreadSafe
    public static class Hits<K> {
        private final Set<HitWriter<K>> _writers = new HashSet<>();

        private final K _key;
        private volatile long _hits;
        private volatile Date _lastAccessed;

        public Hits(@Nullable K key) {
            _key = key;
        }

        @Nullable
        public K getKey() {
            return _key;
        }

        @Nonnegative
        public long getHits() {
            return _hits;
        }

        @Nonnull
        public Date getLastAccessed() {
            return _lastAccessed;
        }

        public void record(@Nonnull HitWriter<K> writer) {
            _writers.add(writer);
            _hits++;
            _lastAccessed = new Date();
        }

        @Nonnull
        protected Set<HitWriter<K>> getWriters() {
            return _writers;
        }
    }

    public interface HitWriter<K> {
        public void write(@Nonnull Set<Hits<K>> hits);
    }

    private final Lock _lock = new ReentrantLock();
    private final Condition _condition = _lock.newCondition();

    private volatile Duration _cycleInterval = new Duration("15s");
    private volatile int _maxQueueSize = 1000;
    private volatile String _name = "Hits";

    private Map<Object, Hits<?>> _keyToHits = new HashMap<>();
    private Thread _cyclerThread;

    @Nonnull
    public Duration getCycleInterval() {
        return _cycleInterval;
    }

    public void setCycleInterval(@Nonnull Duration cycleInterval) {
        _cycleInterval = cycleInterval;
    }

    @Nonnegative
    public int getMaxQueueSize() {
        return _maxQueueSize;
    }

    public void setMaxQueueSize(@Nonnegative int maxQueueSize) {
        _maxQueueSize = maxQueueSize;
    }

    @Nonnull
    public String getName() {
        return _name;
    }

    public void setName(@Nonnull String name) {
        _name = name;
        synchronized (this) {
            if (_cyclerThread != null) {
                _cyclerThread.setName(name + ".Cycler");
            }
        }
    }

    @PostConstruct
    public void init() {
        synchronized (this) {
            if (_cyclerThread != null) {
                throw new IllegalStateException("The recorder was already initialized.");
            }
            _cyclerThread = new Thread(new Cycler(), _name + ".Cycler");
            _cyclerThread.start();
        }
    }

    @PreDestroy
    public void destroy() {
        synchronized (this) {
            try {
                if (_cyclerThread != null) {
                    try {
                        _cyclerThread.interrupt();
                        while (!currentThread().isInterrupted() && _cyclerThread.isAlive()) {
                            _cyclerThread.join(10);
                            if (_cyclerThread.isAlive()) {
                                LOG.info("Still wait for termination of " + _cyclerThread + "...");
                                _cyclerThread.interrupt();
                            }
                        }
                    } catch (InterruptedException ignored) {
                        currentThread().interrupt();
                        LOG.debug("Could not wait for termination of " + _cyclerThread + ". This thread was interrupted.");
                    }
                }
            } finally {
                _cyclerThread = null;
            }
        }
    }

    public <K> void recordHitOf(@Nonnull K key, @Nonnull HitWriter<K> writer) {
        _lock.lock();
        try {
            final Hits<K> hits = getHitsFor(key);
            hits.record(writer);
            _condition.signalAll();
        } finally {
            _lock.unlock();
        }
    }

    @Nonnull
    @GuardedBy("_lock")
    protected  <K> Hits<K> getHitsFor(@Nonnull K key) {
        Hits<?> hits = _keyToHits.get(key);
        if (hits == null) {
            hits = new Hits<>(key);
            _keyToHits.put(key, hits);
        }
        // noinspection unchecked
        return (Hits<K>) hits;
    }

    @Nonnull
    protected Map<Object, Hits<?>> cycleKeyToHits() {
        final Map<Object, Hits<?>> newKeyToHits = new HashMap<>();
        final Map<Object, Hits<?>> oldKeyToHits;
        _lock.lock();
        try {
            oldKeyToHits = _keyToHits;
            _keyToHits = newKeyToHits;
        } finally {
            _lock.unlock();
        }
        return oldKeyToHits;
    }

    @Nonnull
    protected Map<HitWriter<?>, Set<Hits<?>>> groupByWriter(@Nonnull Map<Object, Hits<?>> keyToHits) {
        final Map<HitWriter<?>, Set<Hits<?>>> result = new HashMap<>();
        for (Entry<Object, Hits<?>> keyAndHits : keyToHits.entrySet()) {
            final Hits<?> hits = keyAndHits.getValue();
            for (HitWriter<?> writer : hits.getWriters()) {
                Set<Hits<?>> groupedKeyToHits = result.get(writer);
                if (groupedKeyToHits == null) {
                    groupedKeyToHits = new HashSet<>();
                    result.put(writer, groupedKeyToHits);
                }
                groupedKeyToHits.add(hits);
            }
        }
        return result;
    }

    @Nonnull
    protected Map<HitWriter<?>, Set<Hits<?>>> getWriterToHitsForNextCycle() {
        final Map<Object, Hits<?>> keyToHits = cycleKeyToHits();
        return groupByWriter(keyToHits);
    }

    public void cycle() {
        try {
            final Map<HitWriter<?>, Set<Hits<?>>> writerToKeyToHits = getWriterToHitsForNextCycle();
            for (Entry<HitWriter<?>, Set<Hits<?>>> writerAndHits : writerToKeyToHits.entrySet()) {
                // noinspection unchecked
                final HitWriter<Object> writer = (HitWriter<Object>) writerAndHits.getKey();
                // noinspection unchecked
                final Set<Hits<Object>> hits = (Set<Hits<Object>>)(Set) writerAndHits.getValue();
                    writer.write(hits);
            }
        } catch (ServiceTemporaryUnavailableException e) {
            LOG.warn("Could not record the hits. This hits are lost now and will not be rescheduled. This is only a problem for statistics.", e);
        }
    }

    @Override
    public String toString() {
        return getClass().getName() + "{" + _name + "}";
    }

    protected class Cycler implements Runnable {

        @Override
        public void run() {
            long lastUpdate = 0;
            while (!currentThread().isInterrupted()) {
                final Duration interval = _cycleInterval;
                final int maxQueueSize = _maxQueueSize;
                try {
                    if (isLastUpdateLongAgo(interval, lastUpdate) || isQueueToBig(_keyToHits, maxQueueSize)) {
                        cycle();
                        lastUpdate = currentTimeMillis();
                    }
                    _lock.lockInterruptibly();
                    try {
                        _condition.await(interval.toMilliSeconds() - (currentTimeMillis() - lastUpdate), MILLISECONDS);
                    } finally {
                        _lock.unlock();
                    }
                } catch (InterruptedException ignored) {
                    currentThread().interrupt();
                }
            }
        }

        protected void waitForSpecifiedInterval() {
            try {
                Thread.sleep(_cycleInterval.toMilliSeconds());
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }

        protected boolean isQueueToBig(@Nonnull Map<Object, Hits<?>> elements, @Nonnegative int maxSize) {
            _lock.lock();
            try {
                return elements.size() >= maxSize;
            } finally {
                _lock.unlock();
            }
        }

        protected boolean isLastUpdateLongAgo(@Nonnull Duration touchFilesNotLaterThan, @Nonnegative long lastUpdate) {
            return lastUpdate + touchFilesNotLaterThan.toMilliSeconds() <= currentTimeMillis();
        }

    }

}

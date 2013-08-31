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

package org.echocat.jomon.net.cluster.cache;

import org.echocat.jomon.cache.Cache;
import org.echocat.jomon.cache.InMemoryBasedCacheSupport;
import org.echocat.jomon.cache.LocalTrackingEnabledCacheListener;
import org.echocat.jomon.cache.LruCache;
import org.echocat.jomon.cache.management.CacheRepository;
import org.echocat.jomon.net.cluster.channel.HandlerEnabledClusterChannel;
import org.echocat.jomon.net.cluster.channel.HandlerEnabledClusterChannel.MessageHandler;
import org.echocat.jomon.net.cluster.channel.Message;
import org.echocat.jomon.runtime.math.OverPeriodCounter;
import org.echocat.jomon.runtime.util.Duration;
import org.echocat.jomon.runtime.util.ValueProducer;

import javax.annotation.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.System.getProperty;
import static java.lang.Thread.currentThread;
import static java.util.Collections.unmodifiableCollection;

public abstract class CacheListenerForClusterChannelSupport implements LocalTrackingEnabledCacheListener, AutoCloseable {

    public static final Charset CHARSET = Charset.forName("ISO-8859-1");

    private final ThreadLocal<Boolean> _inHandleMessage = new ThreadLocal<>();

    private final CacheRepository _cacheRepository;
    private final HandlerEnabledClusterChannel<?, ?> _clusterChannel;

    private final InMemoryBasedCacheSupport<ReportKey, ReportImpl> _localTrackingCache;
    private volatile boolean _trackLocalEventsEnabled;

    public CacheListenerForClusterChannelSupport(@Nonnull CacheRepository cacheRepository, @Nonnull HandlerEnabledClusterChannel<?, ?> clusterChannel) {
        _cacheRepository = cacheRepository;
        _clusterChannel = clusterChannel;
        _localTrackingCache = createCache();
    }

    @Nonnull
    private InMemoryBasedCacheSupport<ReportKey, ReportImpl> createCache() {
        final LruCache<ReportKey, ReportImpl> cache = new LruCache<>(ReportKey.class, ReportImpl.class);
        cache.setCapacity(0L);
        return cache;
    }

    @Override
    public void setNumberOfLocalTrackedEvents(int numberOfLocalTrackedEvents) {
        _localTrackingCache.setCapacity((long) numberOfLocalTrackedEvents);
        _trackLocalEventsEnabled = numberOfLocalTrackedEvents > 0;
    }

    @Override
    public int getNumberOfLocalTrackedEvents() {
        return _localTrackingCache.getCapacity().intValue();
    }

    @Nonnull
    @Override
    public Collection<? extends Report> getReports() {
        final Set<ReportImpl> reports = new TreeSet<>();
        for (ReportKey reportKey : _localTrackingCache) {
            final ReportImpl report = _localTrackingCache.get(reportKey);
            reports.add(report);
        }
        return unmodifiableCollection(reports);
    }

    protected void record(@Nonnull final Event event) {
        if (_trackLocalEventsEnabled) {
            final StackTraceElement[] stackTrace = currentThread().getStackTrace();
            final ReportImpl report = _localTrackingCache.get(new ReportKey(event, stackTrace), new ValueProducer<ReportKey, ReportImpl>() { @Override public ReportImpl produce(@Nullable ReportKey key) throws Exception {
                return new ReportImpl(event, stackTrace);
            }});
            report.hit();
        }
    }

    @Nonnull
    protected abstract MessageHandler getMessageHandler();

    protected boolean isInHandleMessage() {
        // noinspection ObjectEquality
        return TRUE == _inHandleMessage.get();
    }

    protected boolean isPossibleEndlessLoop() {
        return isInHandleMessage();
    }

    protected void startHandleMessage() {
        _inHandleMessage.set(TRUE);
    }

    protected void finishHandleMessage() {
        _inHandleMessage.remove();
    }

    @Nonnull
    protected ThreadLocal<Boolean> getInHandleMessage() {
        return _inHandleMessage;
    }

    @Nonnull
    protected CacheRepository getCacheRepository() {
        return _cacheRepository;
    }

    @Nonnull
    protected HandlerEnabledClusterChannel<?, ?> getClusterChannel() {
        return _clusterChannel;
    }

    @PostConstruct
    public void init() throws Exception {
        _clusterChannel.register(getMessageHandler());
    }

    @Override
    @PreDestroy
    public void close() throws Exception {
        _clusterChannel.unregister(getMessageHandler());
    }

    public void send(@Nonnull Message message) throws IllegalArgumentException {
        _clusterChannel.send(message);
    }

    public void send(@Nonnull Message message, @Nonnegative long timeout, @Nonnull TimeUnit unit) throws IllegalArgumentException {
        _clusterChannel.send(message, timeout, unit);
    }

    @Nullable
    public <K, V> Cache<K, V> findCache(@Nonnull String id) {
        return _cacheRepository.find(id);
    }

    @Nonnull
    protected String getLogStackTracePropertyName() {
        return getClass().getName() + ".logStackTrace";
    }

    protected boolean isLogStackTrace() {
        final String plainValue = getProperty(getLogStackTracePropertyName(), FALSE.toString());
        return TRUE.toString().equalsIgnoreCase(plainValue);
    }

    @Nullable
    protected Throwable createThrowableIfLogStackTraceIsNeeded() {
        return isLogStackTrace() ? new Throwable() : null;
    }

    protected static class ReportKey {

        private final Event _event;
        private final StackTraceElement[] _stackTrace;

        public ReportKey(@Nonnull Event event, @Nonnull StackTraceElement[] stackTrace) {
            _event = event;
            _stackTrace = stackTrace;
        }

        @Override
        public boolean equals(Object o) {
            final boolean result;
            if (this == o) {
                result = true;
            } else if (!(o instanceof ReportKey)) {
                result = false;
            } else {
                final ReportKey that = (ReportKey) o;
                result = _event.equals(that._event) && Arrays.equals(_stackTrace, that._stackTrace);
            }
            return result;
        }

        @Override
        public int hashCode() {
            return 31 * _event.hashCode() + Arrays.hashCode(_stackTrace);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append(_event);
            for (StackTraceElement stackTraceElement : _stackTrace) {
                sb.append("\n\tat ").append(stackTraceElement);
            }
            return sb.toString();
        }
    }

    protected static class ReportImpl implements Report, Comparable<Report> {

        private final AtomicLong _numberOfInvocations = new AtomicLong();
        private final OverPeriodCounter  _numberOfInvocationsPerSecond = new OverPeriodCounter(new Duration("1m"), new Duration("1s"));
        @Nonnull
        private final Event _event;
        private final StackTraceElement[] _stackTrace;

        public ReportImpl(@Nonnull Event event, @Nonnull StackTraceElement[] stackTrace) {
            _event = event;
            _stackTrace = stackTrace;
        }

        protected void hit() {
            _numberOfInvocations.incrementAndGet();
            _numberOfInvocationsPerSecond.record();
        }

        @Nonnull
        @Override
        public Event getEvent() {
            return _event;
        }

        @Override
        @Nonnegative
        public long getNumberOfInvocations() {
            return _numberOfInvocations.get();
        }

        @Override
        @Nonnegative
        public double getNumberOfInvocationsPerSecond() {
            return _numberOfInvocationsPerSecond.getAsDouble();
        }

        @Nonnull
        @Override
        public StackTraceElement[] getStackTrace() {
            return _stackTrace;
        }

        @Override
        public int compareTo(@Nullable Report to) {
            final int result;
            if (to == null) {
                result = 1;
            } else {
                result = Double.compare(getNumberOfInvocationsPerSecond(), to.getNumberOfInvocationsPerSecond()) * -1;
            }
            return result;
        }
    }
}

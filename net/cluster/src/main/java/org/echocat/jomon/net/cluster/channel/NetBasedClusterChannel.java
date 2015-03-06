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

package org.echocat.jomon.net.cluster.channel;

import org.echocat.jomon.runtime.math.OverPeriodCounter;
import org.echocat.jomon.runtime.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.echocat.jomon.net.cluster.channel.ClusterChannelConstants.pingCommand;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

public abstract class NetBasedClusterChannel<ID, N extends Node<ID>> implements StatisticEnabledClusterChannel<ID, N>, PingEnabledClusterChannel<ID, N>, HandlerEnabledClusterChannel<ID, N> {

    private static final Logger LOG = LoggerFactory.getLogger(NetBasedClusterChannel.class);

    private final Collection<Handler> _handlers = new ArrayList<>();
    private final Lock _lock = new ReentrantLock();
    private final UUID _uuid;

    private volatile String _name;

    private Duration _soTimeout = new Duration("30s");
    private Duration _pingInterval = new Duration("10s");

    private volatile long _lastPingSend;

    private final OverPeriodCounter _messagesReceivedPerSecond = new OverPeriodCounter(new Duration("1m"), new Duration("1s"));
    private volatile long _messagesReceived;
    private volatile long _lastMessageReceived;

    private final OverPeriodCounter _messagesSendPerSecond = new OverPeriodCounter(new Duration("1m"), new Duration("1s"));
    private volatile long _messagesSend;
    private volatile long _lastMessageSend;

    protected NetBasedClusterChannel() {
        this(null);
    }

    protected NetBasedClusterChannel(@Nullable UUID uuid) {
        _uuid = uuid != null ? uuid : UUID.randomUUID();
    }

    @Override
    @Nonnull
    public final UUID getUuid() {
        return _uuid;
    }

    @Override
    @Nullable
    public final String getName() {
        return _name;
    }

    @Override
    public void setName(@Nullable final String name) {
        doSafeAndReinetIfNeeded(new Callable<Void>() {
            @Override
            public Void call() {
                _name = name;
                return null;
            }
        });
    }

    @Nonnull
    public final Duration getSoTimeout() {
        return _soTimeout;
    }

    public void setSoTimeout(@Nonnull Duration soTimeout) {
        if (soTimeout == null) {
            throw new NullPointerException();
        }
        _soTimeout = soTimeout;
    }

    @Override
    @Nonnull
    public final Duration getPingInterval() {
        return _pingInterval;
    }

    @Override
    public void setPingInterval(@Nonnull Duration pingInterval) {
        if (pingInterval == null) {
            throw new NullPointerException();
        }
        _pingInterval = pingInterval;
    }

    protected final void reInitIfNeeded() {
        if (isConnected()) {
            closeQuietly(this);
            try {
                init();
            } catch (final Exception e) {
                throw new RuntimeException("Could not reinitialize " + this + ".", e);
            }
        }
    }

    protected final <T> T doSafeAndReinetIfNeeded(@Nonnull Callable<T> by) {
        _lock.lock();
        try {
            try {
                return by.call();
            } catch (final Exception e) {
                throw new RuntimeException("Could not execute set.", e);
            } finally {
                reInitIfNeeded();
            }
        } finally {
            _lock.unlock();
        }
    }

    protected final <T> T doSafe(@Nonnull Callable<T> by) {
        _lock.lock();
        try {
            try {
                return by.call();
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Exception e) {
                throw new RuntimeException("Could not execute set.", e);
            }
        } finally {
            _lock.unlock();
        }
    }

    @PostConstruct
    public final void init() throws Exception {
        initBeforeLock();
        _lock.lock();
        try {
            boolean success = false;
            try {
                initInLock();
                success = true;
            } finally {
                if (!success) {
                    close();
                }
            }
        } finally {
            _lock.unlock();
        }
        initAfterLock();
    }

    protected void initBeforeLock() throws Exception {}
    protected void initInLock() throws Exception {}
    protected void initAfterLock() throws Exception {}

    @Override
    @PreDestroy
    public final void close() throws Exception {
        closeBeforeLock();
        _lock.lock();
        try {
            closeInLock();
        } finally {
            try {
                _lock.unlock();
            } finally {
                closeAfterLock();
            }
        }
    }

    protected void closeBeforeLock() throws Exception {}
    protected void closeInLock() throws Exception {}
    protected void closeAfterLock() throws Exception {}

    @Override
    public final void register(@Nonnull Handler handler) {
        synchronized (_handlers) {
            if (!_handlers.contains(handler)) {
                _handlers.add(handler);
            }
        }
    }

    @Override
    public final void unregister(@Nonnull Handler handler) {
        synchronized (_handlers) {
            _handlers.remove(handler);
        }
    }

    @Nonnull
    protected final Collection<Handler> getHandlers() {
        synchronized (_handlers) {
            return new ArrayList<>(_handlers);
        }
    }

    protected void recordMessageSend() {
        _messagesSendPerSecond.record();
        _messagesSend++;
        _lastMessageSend = currentTimeMillis();
    }

    protected void recordReceived() {
        _messagesReceivedPerSecond.record();
        _messagesReceived++;
        _lastMessageReceived = currentTimeMillis();
    }

    protected final void recordPingSend() {
        _lastPingSend = currentTimeMillis();
    }


    @Override
    @Nonnull
    public final Date getLastPingSendAt() {
        return new Date(_lastPingSend);
    }

    @Override
    public final Date getLastMessageReceived() {
        final long lastMessageReceived = _lastMessageReceived;
        return lastMessageReceived > 0 ? new Date(lastMessageReceived) : null;
    }

    @Override
    @Nonnegative
    @Nonnull
    public final Double getMessagesReceivedPerSecond() {
        return _messagesReceivedPerSecond.getAsDouble();
    }

    @Override
    @Nonnegative
    @Nonnull
    public final Long getMessagesReceived() {
        return _messagesReceived;
    }

    @Override
    public final Date getLastMessageSend() {
        final long lastMessageSend = _lastMessageSend;
        return lastMessageSend > 0 ? new Date(lastMessageSend) : null;
    }

    @Override
    @Nonnegative
    @Nonnull
    public final Double getMessagesSendPerSecond() {
        return _messagesSendPerSecond.getAsDouble();
    }

    @Override
    @Nonnegative
    @Nonnull
    public final Long getMessagesSend() {
        return _messagesSend;
    }

    protected final void read(@Nonnull ReceivedMessage<N> message) throws IOException {
        recordReceived();
        if (message.getCommand() == pingCommand) {
            readPing(message);
        } else {
            for (final Handler handler : getHandlers()) {
                if (handler instanceof MessageHandler) {
                    ((MessageHandler)handler).handle(this, message);
                }
            }
        }
    }

    protected abstract void readPing(@Nonnull ReceivedMessage<N> message);

    protected boolean isPingRequired() {
        return _lastPingSend + _pingInterval.in(MILLISECONDS) < currentTimeMillis();
    }

    @Nonnull
    public Lock getLock() {
        return _lock;
    }

    protected class Pinger implements Runnable {

        public Pinger() {}

        @Override
        public void run() {
            try {
                while (!currentThread().isInterrupted()) {
                    try {
                        ping();
                    } catch (final Exception e) {
                        LOG.warn("Ping failed. Ping will be retried in " + _pingInterval + ".", e);
                    }
                    _pingInterval.sleep();
                }
            } catch (final InterruptedException ignored) {
                currentThread().interrupt();
            }
        }
    }

}

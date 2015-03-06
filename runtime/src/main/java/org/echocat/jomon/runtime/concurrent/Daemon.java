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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.*;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Daemon implements AutoCloseable {

    private static final Random RANDOM = new Random();
    private static final Logger LOG = LoggerFactory.getLogger(Daemon.class);
    private static final MBeanServer M_BEAN_SERVER = ManagementFactory.getPlatformMBeanServer();
    
    private final AtomicLong _overallExecutionDurationInMillis = new AtomicLong();
    private final AtomicLong _overallExecutionCount = new AtomicLong();
    private final AtomicReference<Duration> _lastExecutionDuration = new AtomicReference<>();
    private final AtomicReference<Date> _nextExecution = new AtomicReference<>();
    private final AtomicReference<Date> _lastExecution = new AtomicReference<>();
    private final AtomicInteger _runningInstances = new AtomicInteger();

    private final Runnable _task;

    private Duration _startDelay = new Duration("0");
    private Duration _minStartDelay;
    private Duration _interval = new Duration("1ms");
    private boolean _initiallyActive = true;
    
    private volatile Thread _thread;

    public Daemon(@Nonnull final Runnable task) {
        _task = task;
    }

    @Nonnull
    public Duration getStartDelay() {
        return _startDelay;
    }

    public void setStartDelay(@Nonnull Duration startDelay) {
        _startDelay = startDelay;
    }

    @Nullable
    public Duration getMinStartDelay() {
        return _minStartDelay;
    }

    public void setMinStartDelay(@Nullable Duration minStartDelay) {
        _minStartDelay = minStartDelay;
    }

    @Nonnull
    public Duration getInterval() {
        return _interval;
    }

    public void setInterval(@Nonnull Duration interval) {
        _interval = interval;
        synchronized (this) {
            if (isActive()) {
                setActive(false);
                setActive(true);
            }
        }
    }

    public boolean isInitiallyActive() {
        return _initiallyActive;
    }

    public void setInitiallyActive(boolean initiallyActive) {
        _initiallyActive = initiallyActive;
    }

    @PostConstruct
    public void init() throws Exception {
        _lastExecutionDuration.set(null);
        _overallExecutionCount.set(0);
        _overallExecutionDurationInMillis.set(0);
        _lastExecution.set(null);
        try {
            final ObjectName objectName = createObjectName();
            M_BEAN_SERVER.registerMBean(new MBeanDaemonWrapper(this), objectName);
        } catch (final Exception e) {
            LOG.warn("Could not register " + _task + " in JMX. This daemon will be available but is not manageable over JMX.", e);
        }
        if (_initiallyActive) {
            setActive(true);
        }
    }

    @Nonnull
    protected ObjectName createObjectName() throws MalformedObjectNameException {
        return new ObjectName(Daemon.class.getPackage().getName() + ":type=" + Daemon.class.getSimpleName() + ",name=" + _task.toString());
    }

    @Override
    @PreDestroy
    public void close() {
        try {
            setActive(false);
        } finally {
            try {
                final ObjectName objectName = createObjectName();
                M_BEAN_SERVER.unregisterMBean(objectName);        
            } catch (final Exception e) {
                LOG.warn("Could not unregister " + _task + " in JMX. This daemon will be destroy but is still visitable over JMX.", e);
            }
        }
    }

    public void setActive(boolean active) {
        if (active) {
            synchronized (this) {
                if (_thread == null) {
                    _thread = new Executor(getTargetStartDelayInMillis(), _interval);
                    _thread.start();
                }
            }
        } else {
            synchronized (this) {
                if (_thread != null) {
                    try {
                        final Thread thread = _thread;
                        thread.interrupt();
                        // noinspection ObjectEquality
                        if (thread != currentThread()) {
                            try {
                                while (thread.isAlive()) {
                                    thread.join(SECONDS.toMillis(10));
                                    if (thread.isAlive()) {
                                        LOG.info("Still wait for termination of task '" + _task + "'...");
                                    }
                                }
                            } catch (final InterruptedException ignored) {
                                currentThread().interrupt();
                                LOG.debug("Could not wait for termination of '" + _task + "'. This thread was interrupted.");
                            }
                        }
                    } finally {
                        _thread = null;
                        _nextExecution.set(null);
                    }
                }
            }
        }
    }

    public boolean isActive() {
        synchronized (this) {
            return _thread != null;
        }
    }

    public boolean isRunning() {
        return _runningInstances.get() > 0;
    }

    public void run() {
        final long startTimeInMillis = System.currentTimeMillis();
        _runningInstances.incrementAndGet();
        try {
            _task.run();
        } finally {
            final long executionTimeInMillis = System.currentTimeMillis() - startTimeInMillis;
            _overallExecutionDurationInMillis.addAndGet(executionTimeInMillis);
            _overallExecutionCount.incrementAndGet();
            _lastExecution.set(new Date());
            _lastExecutionDuration.set(new Duration(executionTimeInMillis));
            _runningInstances.decrementAndGet();
        }
    }

    @Nullable
    protected Duration getTargetStartDelayInMillis() {
        final Duration startDelayInMillis = _startDelay;
        final Duration result;
        if (_minStartDelay == null) {
            result = startDelayInMillis;
        } else {
            final long n = startDelayInMillis.in(MILLISECONDS) - _minStartDelay.in(MILLISECONDS) + 1;
            final long plainValue = nextPositiveValue() % n;
            result = _minStartDelay.plus(plainValue);
        }
        return result;
    }

    @Nonnegative
    private long nextPositiveValue() {
        long result;
        do {
            result = RANDOM.nextLong();
        } while (result < 0);
        return result;
    }

    @Nullable
    public Date getNextExecution() {
        return _nextExecution.get();
    }

    @Nullable
    public Date getLastExecution() {
        return _lastExecution.get();
    }

    @Nullable
    public Duration getLastExecutionDuration() {
        return _lastExecutionDuration.get();
    }

    @Nonnull
    public Duration getOverallExecutionDuration() {
        return new Duration(_overallExecutionDurationInMillis.get());
    }

    @Nonnegative
    public long getOverallExecutionCount() {
        return _overallExecutionCount.get();
    }

    @Nonnull
    Runnable getTask() {
        return _task;
    }

    protected class Executor extends Thread {

        private final Duration _initialDelay;
        private final Duration _delayBetweenEachRun;

        public Executor(@Nullable Duration initialDelay, @Nonnull Duration delayBetweenEachRun) {
            super(_task.toString());
            _initialDelay = initialDelay;
            _delayBetweenEachRun = delayBetweenEachRun;
        }

        @Override
        public void run() {
            try {
                if (_initialDelay != null) {
                    _nextExecution.set(new Date(System.currentTimeMillis() + _initialDelay.in(MILLISECONDS)));
                    _initialDelay.sleep();
                }
            } catch (final InterruptedException ignored) {
                currentThread().interrupt();
            } finally {
                _nextExecution.set(null);
            }
            while (!currentThread().isInterrupted()) {
                boolean success = false;
                try {
                    Daemon.this.run();
                    _nextExecution.set(new Date(System.currentTimeMillis() + _delayBetweenEachRun.in(MILLISECONDS)));
                    _delayBetweenEachRun.sleep();
                    success = true;
                } catch (final InterruptedException ignored) {
                    currentThread().interrupt();
                } catch (final Throwable e) {
                    LOG.error("Task " + _task + " failed with an error. This daemon will now stop and this task will not be executed again.", e);
                    if (e instanceof Error) {
                        throw (Error)e;
                    }
                    currentThread().interrupt();
                } finally {
                    _nextExecution.set(null);
                    if (!success) {
                        _thread = null;
                    }
                }
            }
        }
    }
}

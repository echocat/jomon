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

package org.echocat.jomon.process.daemon.listeners.startup;

import org.echocat.jomon.process.GeneratedProcess;
import org.echocat.jomon.process.daemon.CouldNotStartProcessException;
import org.echocat.jomon.process.daemon.StreamType;
import org.echocat.jomon.runtime.util.Duration;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public abstract class StartupListenerSupport<L extends StartupListenerSupport<L>> implements StartupListener {

    private final Lock _lock = new ReentrantLock();
    private final Condition _condition = _lock.newCondition();
    private final StringBuilder _recordedContentWhileWaiting = new StringBuilder();

    private Duration _maxWaitTimeForStartupOfApplication = new Duration("1m");

    private volatile Boolean _startupDone;

    @Override
    public boolean waitForSuccessfulStart() throws InterruptedException {
        while (!currentThread().isInterrupted() && _startupDone == null) {
            _lock.lockInterruptibly();
            try {
                if (!_condition.await(_maxWaitTimeForStartupOfApplication.toMilliSeconds(), MILLISECONDS)) {
                    _startupDone = false;
                }
            } catch (InterruptedException ignored) {
                currentThread().interrupt();
            } finally {
                _lock.unlock();
            }
        }
        return _startupDone;
    }

    @Override
    public void notifyProcessStarted(@Nonnull GeneratedProcess process) {
    }

    @Override
    public void notifyProcessTerminated(@Nonnull GeneratedProcess process) {
        _lock.lock();
        try {
            if (_startupDone == null) {
                _startupDone = false;
                _condition.signalAll();
            }
        } finally {
            _lock.unlock();
        }
    }

    @Override
    public Boolean isSuccessfulStarted() {
        return _startupDone;
    }

    @Nullable
    @Override
    public String getRecordedContentWhileWaiting() {
        _lock.lock();
        try {
            if (_startupDone == null) {
                throw new IllegalStateException("The startup is still in process.");
            }
            return _recordedContentWhileWaiting.toString();
        } finally {
            _lock.unlock();
        }
    }

    @Override
    public void notifyLineOutput(@Nonnull GeneratedProcess process, @Nonnull String line, @Nonnull StreamType streamType) {
        if (_startupDone == null) {
            _lock.lock();
            try {
                notifyLineOutputWhileStartup(process, line, streamType);
                _recordedContentWhileWaiting.append(line);
                if (!line.endsWith("\n")) {
                    _recordedContentWhileWaiting.append('\n');
                }
            } finally {
                _lock.unlock();
            }
        }
    }

    @Nullable
    @Override
    public Throwable getStartupProblem() {
        _lock.lock();
        try {
            final Throwable result;
            if (_startupDone == null) {
                throw new IllegalStateException("The startup is still in process.");
            } else if (!_startupDone) {
                result = new CouldNotStartProcessException("Could not start process. Lines while starting:\n" + _recordedContentWhileWaiting);
            } else {
                result = null;
            }
            return result;
        } finally {
            _lock.unlock();
        }
    }

    protected void notifyLineOutputWhileStartup(@Nonnull GeneratedProcess process, @Nonnull String line, @Nonnull StreamType streamType) {}

    @SuppressWarnings("UnusedParameters")
    protected void notifyStartupDone(@Nonnull GeneratedProcess process, @Nonnull boolean success) {
        _lock.lock();
        try {
            if (_startupDone == null) {
                _startupDone = success;
                _condition.signalAll();
            }
        } finally {
            _lock.unlock();
        }
    }

    @Nonnull
    public L whichWaitsForTheStartupUntil(@Nonnull Duration duration) {
        setMaxWaitTimeForStartupOfApplication(duration);
        return thisListener();
    }

    @Nonnull
    public L whichWaitsForTheStartupUntil(@Nonnegative long duration) {
        return whichWaitsForTheStartupUntil(new Duration(duration));
    }

    @Nonnull
    public L whichWaitsForTheStartupUntil(@Nonnull String duration) {
        return whichWaitsForTheStartupUntil(new Duration(duration));
    }

    @Nonnull
    public Duration getMaxWaitTimeForStartupOfApplication() {
        return _maxWaitTimeForStartupOfApplication;
    }

    public void setMaxWaitTimeForStartupOfApplication(Duration maxWaitTimeForStartupOfApplication) {
        _maxWaitTimeForStartupOfApplication = maxWaitTimeForStartupOfApplication != null ? maxWaitTimeForStartupOfApplication : new Duration("1m");
    }

    @Nonnull
    protected L thisListener() {
        //noinspection unchecked
        return (L) this;
    }
}

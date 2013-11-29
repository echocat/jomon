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

package org.echocat.jomon.process.listeners.startup;

import org.echocat.jomon.process.CouldNotStartException;
import org.echocat.jomon.process.GeneratedProcess;
import org.echocat.jomon.process.listeners.stream.LineBasedStreamListenerSupport;
import org.echocat.jomon.runtime.io.StreamType;
import org.echocat.jomon.runtime.util.Duration;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.ByteBuffer;

import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.echocat.jomon.runtime.util.ByteCount.allocateBuffer;

public abstract class LineBasedStartupListenerSupport<P extends GeneratedProcess<?, ?>, L extends LineBasedStartupListenerSupport<P, L>> extends LineBasedStreamListenerSupport<P, L> implements StartupListener<P> {

    @Nonnull
    private final ByteBuffer _recordedContentWhileWaiting = allocateBuffer("5m");

    @Nonnull
    private Duration _maxWaitTimeForStartupOfApplication = new Duration("1m");

    private volatile Boolean _startupDone;

    @Override
    public boolean waitForSuccessfulStart() throws InterruptedException {
        while (!currentThread().isInterrupted() && _startupDone == null) {
            lock().lockInterruptibly();
            try {
                if (!condition().await(_maxWaitTimeForStartupOfApplication.toMilliSeconds(), MILLISECONDS)) {
                    _startupDone = false;
                }
            } catch (final InterruptedException ignored) {
                currentThread().interrupt();
            } finally {
                lock().unlock();
            }
        }
        return _startupDone;
    }

    @Override
    public void notifyProcessTerminated(@Nonnull P process, boolean regular) {
        lock().lock();
        try {
            super.notifyProcessTerminated(process, regular);
            if (_startupDone == null) {
                _startupDone = regular;
                condition().signalAll();
            }
        } finally {
            lock().unlock();
        }
    }

    @Override
    public Boolean isSuccessfulStarted() {
        return _startupDone;
    }

    @Nullable
    @Override
    public ByteBuffer getRecordedContentWhileWaiting() {
        lock().lock();
        try {
            if (_startupDone == null) {
                throw new IllegalStateException("The startup is still in process.");
            }
            return _recordedContentWhileWaiting;
        } finally {
            lock().unlock();
        }
    }

    @Override
    @Nullable
    public String getRecordedContentWhileWaitingAsString() {
        lock().lock();
        try {
            if (_startupDone == null) {
                throw new IllegalStateException("The startup is still in process.");
            }
            final int limit = _recordedContentWhileWaiting.limit();
            final int position = _recordedContentWhileWaiting.position();
            _recordedContentWhileWaiting.flip();
            try {
                return getCharset().decode(_recordedContentWhileWaiting).toString();
            } finally {
                _recordedContentWhileWaiting.limit(limit);
                _recordedContentWhileWaiting.position(position);
            }
        } finally {
            lock().unlock();
        }
    }

    @Override
    public void notifyOutput(@Nonnull P process, @Nonnull byte[] data, @Nonnegative int offset, @Nonnegative int length, @Nonnull StreamType streamType) {
        if (_startupDone == null) {
            lock().lock();
            try {
                _recordedContentWhileWaiting.put(data, offset, length);
                super.notifyOutput(process, data, offset, length, streamType);
            } finally {
                lock().unlock();
            }
        }
    }

    @Nullable
    @Override
    public Throwable getStartupProblem() {
        lock().lock();
        try {
            final Throwable result;
            if (_startupDone == null) {
                throw new IllegalStateException("The startup is still in process.");
            } else if (!_startupDone) {
                result = new CouldNotStartException("Could not start process. Contents while starting:\n" + getRecordedContentWhileWaitingAsString());
            } else {
                result = null;
            }
            return result;
        } finally {
            lock().unlock();
        }
    }

    @Override
    protected void write(@Nonnull P process, @Nonnull String content, @Nonnull StreamType streamType) {
        notifyOutputWhileStartup(process, content, streamType);
    }

    protected void notifyOutputWhileStartup(@Nonnull P process, @Nonnull String line, @Nonnull StreamType streamType) {}

    protected void notifyProcessStartupDone(@SuppressWarnings("UnusedParameters") @Nonnull P process, @Nonnull boolean success) {
        lock().lock();
        try {
            if (_startupDone == null) {
                _startupDone = success;
                condition().signalAll();
            }
        } finally {
            lock().unlock();
        }
    }

    @Nonnull
    public L whichWaitsForTheStartupUntil(@Nonnull Duration duration) {
        setMaxWaitTimeForStartupOfApplication(duration);
        return thisObject();
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

}

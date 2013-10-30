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

package org.echocat.jomon.process.listeners.stream;

import org.echocat.jomon.process.GeneratedProcess;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.echocat.jomon.runtime.io.StreamType.stderr;
import static org.echocat.jomon.runtime.io.StreamType.system;

public abstract class LineBasedAndStateEnabledStreamListenerSupport<P extends GeneratedProcess<?, ?>, T extends LineBasedAndStateEnabledStreamListenerSupport<P, T>> extends LineBasedStreamListenerSupport<P, T> {

    private boolean _started;
    private boolean _terminated;
    private boolean _startupDone;

    private boolean _recordProcessStarted;
    private boolean _recordProcessTerminated;
    private boolean _recordProcessStartupSuccessful;

    @Override
    public void notifyProcessStarted(@Nonnull P process) {
        lock().lock();
        try {
            if (!_started) {
                if (_recordProcessStarted) {
                    formatAndWrite(process, process + " started.", system);
                }
                _started = true;
            }
        } finally {
            lock().unlock();
        }
    }

    @Override
    public void notifyProcessStartupDone(@Nonnull P process) {
        lock().lock();
        try {
            if (!_startupDone) {
                if (_recordProcessStartupSuccessful) {
                    formatAndWrite(process, process + " startup done.", system);
                }
                _startupDone = true;
            }
        } finally {
            lock().unlock();
        }

    }

    @Override
    public void notifyProcessTerminated(@Nonnull P process, boolean regular) {
        lock().lock();
        try {
            if (!_terminated) {
                if (_recordProcessTerminated) {
                    final Integer exitCode = getExitCode(process);
                    formatAndWrite(process, process + " ended " + (regular ? "regular" : "irregular") + "." + (exitCode != null ? " ExitCode: " + exitCode : ""), regular ? system : stderr);
                }
                _terminated = true;
            }
        } finally {
            lock().unlock();
        }
    }

    @Nullable
    protected Integer getExitCode(P process) {
        Integer result;
        try {
            result = process.exitValue();
        } catch (IllegalStateException ignored) {
            result = null;
        }
        return result;
    }

    @Nonnull
    public T whichRecordsProcessStart(boolean record) {
        _recordProcessStarted = record;
        return thisObject();
    }

    @Nonnull
    public T whichRecordsProcessStart() {
        return whichRecordsProcessStart(true);
    }

    @Nonnull
    public T whichNotRecordsProcessStart() {
        return whichRecordsProcessStart(false);
    }

    @Nonnull
    public T whichRecordsProcessTermination(boolean record) {
        _recordProcessTerminated = record;
        return thisObject();
    }

    @Nonnull
    public T whichRecordsProcessTermination() {
        return whichRecordsProcessTermination(true);
    }

    @Nonnull
    public T whichNotRecordsProcessTermination() {
        return whichRecordsProcessTermination(false);
    }

    @Nonnull
    public T whichRecordsProcessStartupSuccessful(boolean record) {
        _recordProcessStartupSuccessful = record;
        return thisObject();
    }

    @Nonnull
    public T whichRecordsProcessStartupSuccessful() {
        return whichRecordsProcessStartupSuccessful(true);
    }

    @Nonnull
    public T whichNotRecordsProcessStartupSuccessful() {
        return whichRecordsProcessStartupSuccessful(false);
    }

    public boolean isRecordProcessStarted() {
        return _recordProcessStarted;
    }

    public boolean isRecordProcessTerminated() {
        return _recordProcessTerminated;
    }

    public boolean isRecordProcessStartupSuccessful() {
        return _recordProcessStartupSuccessful;
    }

}

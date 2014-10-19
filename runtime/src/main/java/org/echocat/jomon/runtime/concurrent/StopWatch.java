/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012-2014 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.runtime.concurrent;

import org.echocat.jomon.runtime.util.Duration;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import static java.lang.System.nanoTime;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

@ThreadSafe
public class StopWatch {

    @Nonnull
    public static StopWatch startStopWatch() {
        return startStopWatch(null);
    }

    @Nonnull
    public static StopWatch startStopWatch(@Nullable Duration base) {
        return new StopWatch(base);
    }

    @Nonnegative
    private long _startedNanos;
    @Nonnegative
    private long _baseNanos;

    public StopWatch() {
        this(true, null);
    }

    public StopWatch(boolean start) {
        this(start, null);
    }

    public StopWatch(@Nullable Duration base) {
        this(true, base);
    }

    public StopWatch(boolean start, @Nullable Duration base) {
        _startedNanos = start ? nanoTime() : 0;
        _baseNanos = base != null ? base.in(NANOSECONDS) : 0;
    }

    @Nonnull
    public Duration getCurrentDuration() {
        synchronized (this) {
            final long durationInNanos = (_startedNanos > 0 ? nanoTime() - _startedNanos : 0) + _baseNanos;
            final long milliSeconds = durationInNanos / 1000000;
            final int nanoSeconds = (int) (durationInNanos - (milliSeconds * 1000000));
            return new Duration(milliSeconds, nanoSeconds);
        }
    }

    @Nonnull
    public StopWatch reset() {
        synchronized (this) {
            if (_startedNanos > 0) {
                _startedNanos = nanoTime();
            }
            _baseNanos = 0;
        }
        return this;
    }

    @Nonnull
    public StopWatch pause() {
        synchronized (this) {
            _baseNanos += nanoTime() - _startedNanos;
            _startedNanos = 0;
        }
        return this;
    }

    @Nonnull
    public StopWatch start() {
        synchronized (this) {
            _startedNanos = nanoTime();
        }
        return this;
    }

    public boolean isRunning() {
        synchronized (this) {
            return _startedNanos > 0;
        }
    }

    @Override
    public String toString() {
        synchronized (this) {
            return isRunning() ? getCurrentDuration().toString() : "paused";
        }
    }

}

/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.runtime.concurrent;

import org.echocat.jomon.runtime.util.Duration;

import javax.annotation.Nonnull;

import static java.lang.System.currentTimeMillis;

public class StopWatch {

    private volatile long _started;

    public StopWatch() {
        _started = currentTimeMillis();
    }

    @Nonnull
    public Duration getCurrentDuration() {
        return new Duration(currentTimeMillis() - _started);
    }

    public void reset() {
        _started = currentTimeMillis();
    }

    @Override
    public String toString() {
        return getCurrentDuration().toString();
    }
}

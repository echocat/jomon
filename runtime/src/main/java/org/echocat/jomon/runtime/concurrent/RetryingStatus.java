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

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
@Immutable
public class RetryingStatus {

    private final long _currentTry;
    private final Duration _durationSinceStart;

    public RetryingStatus(@Nonnegative long currentTry, @Nonnull Duration durationSinceStart) {
        _durationSinceStart = durationSinceStart;
        _currentTry = currentTry;
    }

    @Nonnegative
    public long getCurrentTry() {
        return _currentTry;
    }

    @Nonnull
    public Duration getDurationSinceStart() {
        return _durationSinceStart;
    }
}

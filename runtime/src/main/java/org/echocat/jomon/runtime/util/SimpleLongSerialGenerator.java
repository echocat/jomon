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

package org.echocat.jomon.runtime.util;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public class SimpleLongSerialGenerator implements SerialGenerator<Long> {

    private long _next;
    private boolean _firstValueTaken;
    private long _incrementBy = 1;

    public void setInitialValue(long initialValue) {
        synchronized (this) {
            if (!_firstValueTaken) {
                _next = initialValue;
            }
        }
    }

    @Nonnull
    public SimpleLongSerialGenerator withInitialValue(long initialValue) {
        setInitialValue(initialValue);
        return this;
    }

    public void setIncrementBy(long incrementBy) {
        synchronized (this) {
            _incrementBy = incrementBy;
        }
    }

    @Nonnull
    public SimpleLongSerialGenerator whichIncrementsBy(long incrementBy) {
        setIncrementBy(incrementBy);
        return this;
    }

    @Override
    @Nonnegative
    public Long next() {
        synchronized (this) {
            _next = _next + _incrementBy;
            final long result = _next;
            _firstValueTaken = true;
            return result;
        }
    }

    @Nonnull
    @Override
    public Class<Long> getGeneratedType() {
        return Long.class;
    }
}

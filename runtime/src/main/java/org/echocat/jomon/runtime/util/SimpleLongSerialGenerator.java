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

package org.echocat.jomon.runtime.util;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicLong;

public class SimpleLongSerialGenerator implements SerialGenerator<Long> {

    private final AtomicLong _next = new AtomicLong();

    private boolean _firstValueTaken;

    public void setInitialValue(long initialValue) {
        synchronized (this) {
            if (!_firstValueTaken) {
                _next.set(initialValue);
            }
        }
    }

    @Override
    @Nonnegative
    public Long next() {
        synchronized (this) {
            final long result = _next.incrementAndGet();
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

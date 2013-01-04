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

import org.echocat.jomon.runtime.annotations.Excluding;
import org.echocat.jomon.runtime.annotations.Including;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
@Immutable
public abstract class RangeSupport<V> implements Range<V> {

    private final V _from;
    private final V _to;

    public RangeSupport(@Nullable @Including V from, @Nullable @Excluding V to) {
        _from = from;
        _to = to;
    }

    @Override
    @Nullable
    public V getFrom() {
        return _from;
    }

    @Nonnull
    public V getFrom(@Nonnull V defValue) {
        return _from != null ? _from : defValue;
    }

    @Override
    @Nullable
    public V getTo() {
        return _to;
    }

    @Nonnull
    public V getTo(@Nonnull V defValue) {
        return _to != null ? _to : defValue;
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else  if (!(o instanceof Range)) {
            result = false;
        } else {
            final Range<?> that = (Range) o;
            final V from = getFrom();
            final V to = getTo();
            result = (from != null ? from.equals(that.getFrom()) : that.getFrom() == null) &&
                (to != null ? to.equals(that.getTo()) : that.getTo() == null);
        }
        return result;
    }

    @Override
    public int hashCode() {
        final V from = getFrom();
        final V to = getTo();
        int result = from != null ? from.hashCode() : 0;
        result = 31 * result + (to != null ? to.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return _from + " to " + _to;
    }
}

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

package org.echocat.jomon.runtime.numbers;

import org.echocat.jomon.runtime.annotations.Excluding;
import org.echocat.jomon.runtime.annotations.Including;
import org.echocat.jomon.runtime.numbers.LongRange.Adapter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@ThreadSafe
@Immutable
@XmlJavaTypeAdapter(Adapter.class)
public class LongRange extends NumberRange<Long> {

    public LongRange(@Nullable @Including Long from, @Nullable @Excluding Long to) {
        super(from, to);
    }

    @Override
    protected boolean isGreaterThan(@Nonnull Long what, @Nonnull Long as) {
        return what > as;
    }

    @Override
    public boolean isSignificant(@Nonnull Long minValue, @Nonnull Long maxValue, @Nonnull Long base) {
        final Long from = getFrom();
        final Long to = getTo();
        final long range = (to != null ? to : maxValue) - (from != null ? from : minValue);
        return range <= base;
    }

    @XmlRootElement(name = "longRange")
    @XmlType(name = "longRange")
    public static class Container extends NumberRange.Container<Long> {}

    public static class Adapter extends NumberRange.Adapter<Long, Container, LongRange> {

        @Nonnull
        @Override
        protected Container newContainer() {
            return new Container();
        }

        @Nonnull
        @Override
        protected LongRange newNumberRange(@Nullable @Including Long from, @Nullable @Excluding Long to) {
            return new LongRange(from, to);
        }

    }
}

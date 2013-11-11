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
import org.echocat.jomon.runtime.numbers.ShortRange.Adapter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@ThreadSafe
@Immutable
@XmlJavaTypeAdapter(Adapter.class)
public class ShortRange extends NumberRange<Short> {

    public ShortRange(@Nullable @Including Short from, @Nullable @Excluding Short to) {
        super(from, to);
    }

    @Override
    protected boolean isGreaterThan(@Nonnull Short what, @Nonnull Short as) {
        return what > as;
    }

    @Override
    public boolean isSignificant(@Nonnull Short minValue, @Nonnull Short maxValue, @Nonnull Short base) {
        final Short from = getFrom();
        final Short to = getTo();
        final int range = (to != null ? to : maxValue) - (from != null ? from : minValue);
        return range <= base;
    }

    @XmlRootElement(name = "shortRange")
    @XmlType(name = "shortRangeType")
    public static class Container extends NumberRange.Container<Short> {

        @Override
        @XmlAttribute(name = "from")
        public Short getFrom() {
            return super.getFrom();
        }

        @Override
        public void setFrom(Short from) {
            super.setFrom(from);
        }

        @Override
        @XmlAttribute(name = "to")
        public Short getTo() {
            return super.getTo();
        }

        @Override
        public void setTo(Short to) {
            super.setTo(to);
        }

    }

    public static class Adapter extends NumberRange.Adapter<Short, Container, ShortRange> {

        @Nonnull
        @Override
        protected Container newContainer() {
            return new Container();
        }

        @Nonnull
        @Override
        protected ShortRange newNumberRange(@Nullable @Including Short from, @Nullable @Excluding Short to) {
            return new ShortRange(from, to);
        }

    }
}

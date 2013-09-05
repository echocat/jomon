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
import org.echocat.jomon.runtime.numbers.IntegerRange.Adapter;

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
public class IntegerRange extends NumberRange<Integer> {

    public IntegerRange(@Nullable @Including Integer from, @Nullable @Excluding Integer to) {
        super(from, to);
    }

    @Override
    protected boolean isGreaterThan(@Nonnull Integer what, @Nonnull Integer as) {
        return what > as;
    }

    @Override
    public boolean isSignificant(@Nonnull Integer minValue, @Nonnull Integer maxValue, @Nonnull Integer base) {
        final Integer from = getFrom();
        final Integer to = getTo();
        final int range = (to != null ? to : maxValue) - (from != null ? from : minValue);
        return range <= base;
    }

    @XmlRootElement(name = "integerRange")
    @XmlType(name = "integerRange")
    public static class Container extends NumberRange.Container<Integer> {}

    public static class Adapter extends NumberRange.Adapter<Integer, Container, IntegerRange> {

        @Nonnull
        @Override
        protected Container newContainer() {
            return new Container();
        }

        @Nonnull
        @Override
        protected IntegerRange newNumberRange(@Nullable @Including Integer from, @Nullable @Excluding Integer to) {
            return new IntegerRange(from, to);
        }

    }
}

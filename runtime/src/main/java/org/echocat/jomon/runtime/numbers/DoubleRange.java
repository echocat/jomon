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

package org.echocat.jomon.runtime.numbers;

import org.echocat.jomon.runtime.annotations.Excluding;
import org.echocat.jomon.runtime.annotations.Including;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@ThreadSafe
@Immutable
@XmlJavaTypeAdapter(DoubleRange.Adapter.class)
public class DoubleRange extends NumberRange<Double> {

    public DoubleRange(@Nullable @Including Double from, @Nullable @Excluding Double to) {
        super(from, to);
    }

    @Override
    protected boolean isGreaterThan(@Nonnull Double what, @Nonnull Double as) {
        return what > as;
    }

    @Override
    public boolean isSignificant(@Nonnull Double minValue, @Nonnull Double maxValue, @Nonnull Double base) {
        final Double from = getFrom();
        final Double to = getTo();
        final double range = (to != null ? to : maxValue) - (from != null ? from : minValue);
        return range <= base;
    }

    @XmlRootElement(name = "doubleRange")
    @XmlType(name = "doubleRange")
    public static class Container extends NumberRange.Container<Double> {}

    public static class Adapter extends NumberRange.Adapter<Double, Container, DoubleRange> {

        @Nonnull
        @Override
        protected Container newContainer() {
            return new Container();
        }

        @Nonnull
        @Override
        protected DoubleRange newNumberRange(@Nullable @Including Double from, @Nullable @Excluding Double to) {
            return new DoubleRange(from, to);
        }

    }
}

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

import org.echocat.jomon.runtime.annotations.Excluding;
import org.echocat.jomon.runtime.annotations.Including;
import org.echocat.jomon.runtime.generation.IncreasingRequirement;
import org.echocat.jomon.runtime.numbers.DoubleRangeRequirement;
import org.echocat.jomon.runtime.numbers.ExactDoubleRequirement;
import org.echocat.jomon.runtime.numbers.NumberRequirement;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import static java.lang.Math.round;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.echocat.jomon.runtime.numbers.DoubleGenerator.generateDouble;

@ThreadSafe
public class IncreasingDurationRequirement implements DurationRequirement, IncreasingRequirement<Duration> {

    private final Duration _initialValue;
    private NumberRequirement<Double> _factor = new ExactDoubleRequirement(1.1d);

    private Duration _next;

    public IncreasingDurationRequirement(@Nonnull String initialValue) {
        this(new Duration(initialValue));
    }

    public IncreasingDurationRequirement(@Nonnegative long initialValue) {
        this(new Duration(initialValue));
    }

    public IncreasingDurationRequirement(@Nonnull Duration initialValue) {
        _initialValue = initialValue;
        _next = initialValue;
    }

    @Nonnull
    public IncreasingDurationRequirement withFactor(@Nonnegative double factor) {
        return withFactor(new ExactDoubleRequirement(factor));
    }

    @Nonnull
    public IncreasingDurationRequirement withFactor(@Nonnegative @Including double from, @Nonnegative @Excluding double to) {
        return withFactor(new DoubleRangeRequirement(from, to));
    }

    @Nonnull
    public IncreasingDurationRequirement withFactor(@Nonnull NumberRequirement<Double> factor) {
        _factor = factor;
        return this;
    }

    @Nonnull
    @Override
    public Duration next() {
        synchronized (this) {
            final Duration now = _next;
            _next = generateNext(now, _factor);
            return now;
        }
    }

    @Nonnull
    protected Duration generateNext(@Nonnull Duration current, @Nonnull NumberRequirement<Double> factorRequirement) {
        final Double factor = generateDouble(factorRequirement);
        final long millis = current.in(MILLISECONDS);
        final long toAdd = round((double) millis * factor);
        return current.plus(toAdd);
    }

    @Nonnull
    public Duration getInitialValue() {
        return _initialValue;
    }

    @Nonnull
    public NumberRequirement<Double> getFactor() {
        return _factor;
    }
}

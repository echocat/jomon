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

package org.echocat.jomon.runtime.math;

import org.echocat.jomon.runtime.util.Duration;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
@Immutable
public class OverPeriodAverageDoubleCounter extends OverPeriodAverageCounter<Double> {

    public OverPeriodAverageDoubleCounter(@Nonnull Duration measurePeriod, @Nonnull Duration resolution) {
        super(measurePeriod, resolution);
    }

    @Nonnull
    @Override
    protected Double getZeroValue() {
        return 0D;
    }

    @Override
    @Nonnull
    protected Double add(@Nonnull Double value, @Nonnull Double to) {
        return value + to;
    }

    @Nonnull
    @Override
    protected Double averageFor(@Nonnull Double totalSumOfValues, @Nonnegative long totalSumOfValueCount) {
        return totalSumOfValueCount > 0 ? totalSumOfValues / totalSumOfValueCount : 0d;
    }
}

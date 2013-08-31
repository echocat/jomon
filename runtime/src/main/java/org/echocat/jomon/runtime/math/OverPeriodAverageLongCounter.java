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
public class OverPeriodAverageLongCounter extends OverPeriodAverageCounter<Long> {

    public OverPeriodAverageLongCounter(@Nonnull Duration measurePeriod, @Nonnull Duration resolution) {
        super(measurePeriod, resolution);
    }

    @Nonnull
    @Override
    protected Long getZeroValue() {
        return 0L;
    }

    @Override
    @Nonnull
    protected Long add(@Nonnull Long value, @Nonnull Long to) {
        return value + to;
    }

    @Nonnull
    @Override
    protected Long averageFor(@Nonnull Long totalSumOfValues, @Nonnegative long totalSumOfValueCount) {
        return totalSumOfValueCount > 0 ? totalSumOfValues / totalSumOfValueCount : 0L;
    }
}

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

import static java.lang.System.currentTimeMillis;

public class OverPeriodCounter {

    protected static final int MAX_NUMBER_OF_MEASURE_POINTS = 10000;

    private final long _measurePeriod;
    private final long _resolution;

    private final Long[] _measuredCounts;
    private final Long[] _measuredBaseTimes;

    public OverPeriodCounter(@Nonnull Duration measurePeriod, @Nonnull Duration resolution) {
        if (measurePeriod.isLessThan(resolution)) {
            throw new IllegalArgumentException("The given measure period have to be larger or equal than the resolution.");
        }
        _measurePeriod = measurePeriod.toMilliSeconds();
        _resolution = resolution.toMilliSeconds() - (measurePeriod.toMilliSeconds() % resolution.toMilliSeconds());
        final long measurePointsCount = _measurePeriod / _resolution;
        if (measurePointsCount > MAX_NUMBER_OF_MEASURE_POINTS) {
            throw new IllegalArgumentException("The difference between measurePeriod and resolution is to high. Do not reach measurePeriod/resolution > " + MAX_NUMBER_OF_MEASURE_POINTS + ".");
        }
        _measuredCounts = new Long[(int)measurePointsCount];
        _measuredBaseTimes = new Long[(int)measurePointsCount];
    }

    public void record() {
        final long currentTime = currentTimeMillis();
        final long currentPositionInPeriod = currentTime % _measurePeriod;
        final int i = (int) (currentPositionInPeriod / _resolution);
        final long measuredPeriodBaseTime = (currentTime / _measurePeriod) * _measurePeriod;
        final long measuredBaseTime = measuredPeriodBaseTime + (i * _resolution);
        synchronized (this) {
            if (_measuredCounts[i] == null || _measuredBaseTimes[i] == null
                || (_measuredBaseTimes[i] != measuredBaseTime && _measuredBaseTimes[i] + _measurePeriod < currentTime)) {
                _measuredCounts[i] = 0L;
                _measuredBaseTimes[i] = measuredBaseTime;
            }
            _measuredCounts[i]++;
        }
    }

    @Nonnegative
    public long getAllOfMeasurePeriod() {
        final long currentTime = currentTimeMillis();
        long sumOfMeasuredValueCounts = 0;
        synchronized (this) {
            for (int i = 0; i < _measuredCounts.length; i++) {
                if (_measuredBaseTimes[i] != null && _measuredBaseTimes[i] + _measurePeriod >= currentTime) {
                    sumOfMeasuredValueCounts += _measuredCounts[i];
                }
            }
        }
        return sumOfMeasuredValueCounts;
    }

    @Nonnegative
    public long get() {
        return getAllOfMeasurePeriod() / _measuredCounts.length;
    }

    @Nonnegative
    public double getAsDouble() {
        return (double) getAllOfMeasurePeriod() / (double) _measuredCounts.length;
    }

    @Nonnull
    public Duration getMeasurePeriod() {
        return new Duration(_measurePeriod);
    }

    @Nonnull
    public Duration getResolution() {
        return new Duration(_resolution);
    }

    @Nonnegative
    public int getNumberOfMeasureSlots() {
        return _measuredCounts.length;
    }

    @Override
    public String toString() {
        return get() + "/" + getResolution();
    }
}

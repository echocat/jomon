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

import static java.lang.System.currentTimeMillis;

@ThreadSafe
@Immutable
public abstract class OverPeriodAverageCounter<T> {

    protected static final int MAX_NUMBER_OF_MEASURE_POINTS = 10000;

    private final long _measurePeriod;
    private final long _resolution;

    private final Object[] _measuredValues;
    private final Long[] _measuredCounts;
    private final Long[] _measuredBaseTimes;

    private final T _zeroValue;

    protected OverPeriodAverageCounter(@Nonnull Duration measurePeriod, @Nonnull Duration resolution) {
        if (measurePeriod.isLessThan(resolution)) {
            throw new IllegalArgumentException("The given measure period have to be larger or equal than the resolution.");
        }
        _measurePeriod = measurePeriod.toMilliSeconds();
        _resolution = resolution.toMilliSeconds() - (measurePeriod.toMilliSeconds() % resolution.toMilliSeconds());
        final long measurePointsCount = _measurePeriod / _resolution;
        if (measurePointsCount > MAX_NUMBER_OF_MEASURE_POINTS) {
            throw new IllegalArgumentException("The difference between measurePeriod and resolution is to high. Do not reach measurePeriod/resolution > " + MAX_NUMBER_OF_MEASURE_POINTS + ".");
        }
        _measuredValues = new Object[(int)measurePointsCount];
        _measuredCounts = new Long[(int)measurePointsCount];
        _measuredBaseTimes = new Long[(int)measurePointsCount];
        _zeroValue = getZeroValue();
    }

    public void record(@Nonnull T value) {
        final long currentTime = currentTimeMillis();
        final long currentPositionInPeriod = currentTime % _measurePeriod;
        final int i = (int) (currentPositionInPeriod / _resolution);
        final long measuredPeriodBaseTime = (currentTime / _measurePeriod) * _measurePeriod;
        final long measuredBaseTime = measuredPeriodBaseTime + (i * _resolution);
        synchronized (this) {
            if (_measuredValues[i] == null || _measuredCounts[i] == null || _measuredBaseTimes[i] == null
                || (_measuredBaseTimes[i] != measuredBaseTime && _measuredBaseTimes[i] + _measurePeriod < currentTime)) {
                _measuredValues[i] = _zeroValue;
                _measuredCounts[i] = 0L;
                _measuredBaseTimes[i] = measuredBaseTime;
            }
            // noinspection unchecked
            _measuredValues[i] = add(value, (T) _measuredValues[i]);
            _measuredCounts[i]++;
        }
    }

    @Nonnull
    public T get() {
        final long currentTime = currentTimeMillis();
        T sumOfMeasuredValues = _zeroValue;
        long sumOfMeasuredValueCounts = 0;
        synchronized (this) {
            for (int i = 0; i < _measuredCounts.length; i++) {
                if (_measuredBaseTimes[i] != null && _measuredBaseTimes[i] + _measurePeriod >= currentTime && _measuredCounts[i] > 0) {
                    // noinspection unchecked
                    sumOfMeasuredValues = add((T) _measuredValues[i], sumOfMeasuredValues);
                    sumOfMeasuredValueCounts += _measuredCounts[i];
                }
            }
        }
        return averageFor(sumOfMeasuredValues, sumOfMeasuredValueCounts);
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
        return _measuredValues.length;
    }

    @Nonnull
    protected abstract T getZeroValue();

    @Nonnull
    protected abstract T add(@Nonnull T value, @Nonnull T to);

    @Nonnull
    protected abstract T averageFor(@Nonnull T totalSumOfValues, @Nonnegative long totalSumOfValueCount);

    @Override
    public String toString() {
        return "Average of " + get() + " measured in " + getMeasurePeriod();
    }
}

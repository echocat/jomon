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

package org.echocat.jomon.runtime.math;

import org.echocat.jomon.runtime.concurrent.StopWatch;
import org.echocat.jomon.runtime.util.Duration;
import org.junit.Test;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class OverPeriodAverageCounterIntegrationTest {

    protected static final int STEPS = 100;
    protected static final Duration SLEEP = new Duration("50ms");

    @Test
    public void testWithStaticValue() throws Exception {
        final Impl counter = new Impl(new Duration("5s"), new Duration("1s"));
        assertThat(counter.getNumberOfMeasureSlots(), is(5));
        final StopWatch stopWatch = new StopWatch();
        while (stopWatch.getCurrentDuration().isLessThan(counter.getMeasurePeriod())) {
            counter.record(2L);
            SLEEP.sleepUnchecked();
        }
        final Long result = counter.get();
        assertThat(result, is(2L));
    }

    @Test
    public void testWithIncreasingValue() throws Exception {
        long value = 0;
        long sum = 0;
        long steps = 0;
        final Impl counter = new Impl(new Duration("5s"), new Duration("1s"));
        assertThat(counter.getNumberOfMeasureSlots(), is(5));
        final StopWatch stopWatch = new StopWatch();
        while (stopWatch.getCurrentDuration().isLessThan(counter.getMeasurePeriod())) {
            counter.record(++value);
            sum+= value;
            steps++;
            SLEEP.sleepUnchecked();
        }
        final Long result = counter.get();
        final long expected = sum / steps;
        assertThat(result >= (expected - 15) && result <= (expected + 15) , is(true));
    }

    @Test
    public void testWithChaningValueAfterMeasurePeriod() throws Exception {
        final Impl counter = new Impl(new Duration("5s"), new Duration("1s"));
        final Duration timeOfRecord = new Duration(counter.getMeasurePeriod().toMilliSeconds() * 2);
        assertThat(counter.getNumberOfMeasureSlots(), is(5));
        final StopWatch stopWatch = new StopWatch();
        while (stopWatch.getCurrentDuration().isLessThan(timeOfRecord)) {
            if (stopWatch.getCurrentDuration().isLessThan(counter.getMeasurePeriod())) {
                counter.record(666L);
            } else {
                counter.record(2L);
            }
            SLEEP.sleepUnchecked();
        }
        final Long result = counter.get();
        assertThat(result, is(2L));
    }

    @Test
    public void testWithChangingValueInMeasurePeriod() throws Exception {
        final Impl counter = new Impl(new Duration("5s"), new Duration("1s"));
        final Duration timeOfRecord = new Duration(counter.getMeasurePeriod().toMilliSeconds() * 2);
        final Duration timeAfterChangeBehavior = new Duration((long) ((double)counter.getMeasurePeriod().toMilliSeconds() * 1.5d));
        assertThat(counter.getNumberOfMeasureSlots(), is(5));
        final StopWatch stopWatch = new StopWatch();
        while (stopWatch.getCurrentDuration().isLessThan(timeOfRecord)) {
            if (stopWatch.getCurrentDuration().isLessThan(timeAfterChangeBehavior)) {
                counter.record(40L);
            } else {
                counter.record(20L);
            }
            SLEEP.sleepUnchecked();
        }
        final Long result = counter.get();
        assertThat(result >= 26L && result <= 34L, is(true));
    }

    protected static class Impl extends OverPeriodAverageCounter<Long> {

        public Impl(@Nonnull Duration measurePeriod, @Nonnull Duration resolution) {
            super(measurePeriod, resolution);
        }

        @Nonnull
        @Override
        protected Long getZeroValue() {
            return 0L;
        }

        @Nonnull
        @Override
        protected Long add(@Nonnull Long value, @Nonnull Long to) {
            return value + to;
        }

        @Nonnull
        @Override
        protected Long averageFor(@Nonnull Long totalSumOfValues, @Nonnegative long totalSumOfValueCount) {
            return totalSumOfValueCount > 0 ? totalSumOfValues / totalSumOfValueCount : 0L;
        }
    }
}

/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012-2014 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.runtime.util;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.*;
import static org.echocat.jomon.runtime.util.Duration.duration;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class DurationUnitTest {

    @Nonnull
    protected static <T> Matcher<T> is(@Nonnull T value) {
        return CoreMatchers.is(value);
    }

    @Nonnull
    protected static Matcher<Duration> is(@Nonnegative long milliSeconds) {
        return is(milliSeconds, 0);
    }

    @Nonnull
    protected static Matcher<Duration> is(@Nonnegative final long milliSeconds, @Nonnegative final int nanoSeconds) {
        return new TypeSafeMatcher<Duration>() {

            @Override
            protected boolean matchesSafely(@Nonnull Duration item) {
                return item.getMilliSeconds() == milliSeconds && item.getNanoSeconds() == nanoSeconds;
            }

            @Override
            public void describeTo(@Nonnull Description description) {
                description.appendText("is ").appendValue(new Duration(milliSeconds, nanoSeconds));
            }

        };
    }

    @Nonnull
    protected static Matcher<Map<TimeUnit, Long>> hasSameEntriesAs(@Nullable final Object... expected) {
        if (expected.length % 2 != 0) {
            throw new IllegalArgumentException("Provide key and value pairs.");
        }
        return new TypeSafeMatcher<Map<TimeUnit, Long>>() {
            @Override
            public boolean matchesSafely(Map<TimeUnit, Long> item) {
                boolean result;
                if (item == null && expected == null) {
                    result = true;
                } else if (item == null || expected == null || item.size() != (expected.length / 2)) {
                    result = false;
                } else {
                    result = true;
                    for (int i = 0; i < expected.length; i+= 2) {
                        final TimeUnit expectedKey = (TimeUnit) expected[i];
                        final Long expectedValue = ((Number) expected[i + 1]).longValue();
                        final Long foundValue = item.get(expectedKey);
                        if (expectedValue != null ? expectedValue.equals(foundValue) : foundValue == null) {
                            result = true;
                        } else {
                            result = false;
                            break;
                        }
                    }
                }
                return result;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("has same entries as ").appendValue(expected);
            }
        };
    }
    @Test
    public void newInstanceWithParse() {
        assertThat(new Duration("666ns"), is(0L, 666));
        assertThat(new Duration("333µs 666ns"), is(0L, (333 * 1000) + 666));
        assertThat(new Duration("555ms 666ns 333µs"), is(555, (333 * 1000) + 666));
        assertThat(new Duration("0ms"), is(0L));
        assertThat(new Duration("2000ms"), is(2000L));
        assertThat(new Duration("0s"), is(0L));
        assertThat(new Duration("3S"), is(3L));
        assertThat(new Duration("3ms"), is(3L));
        assertThat(new Duration("3s"), is(3 * 1000L));
        assertThat(new Duration("3m"), is(3 * 60 * 1000L));
        assertThat(new Duration("3h"), is(3 * 60 * 60 * 1000L));
        assertThat(new Duration("3d"), is(3 * 24 * 60 * 60 * 1000L));
    }

    @Test
    public void toPattern() {
        assertThat(new Duration("666ns").toPattern(), is("666ns"));
        assertThat(new Duration("333µs 666ns").toPattern(), is("333µs 666ns"));
        assertThat(new Duration("555ms 666ns 333µs").toPattern(), is("555ms 333µs 666ns"));
        assertThat(new Duration("0ms").toPattern(), is("0ms"));
        assertThat(new Duration("0s").toPattern(), is("0ms"));
        assertThat(new Duration("3S").toPattern(), is("3ms"));
        assertThat(new Duration("3s").toPattern(), is("3s"));
        assertThat(new Duration("3m").toPattern(), is("3m"));
        assertThat(new Duration("3h").toPattern(), is("3h"));
        assertThat(new Duration("3d").toPattern(), is("3d"));
    }

    @Test
    public void multiplyBy() {
        assertThat(duration("100ms 200ns").multiplyBy(1.1d), is(110, 220));
        assertThat(duration("100ms 500µs").multiplyBy(2.1d), is(211, 50 * 1000));
        assertThat(duration("100ms 200ns").multiplyBy(2L), is(200, 400));
        assertThat(duration("100ms 500µs").multiplyBy(2L), is(201, 0));
    }

    @Test
    public void dividedBy() {
        assertThat(duration("100ms 200ns").dividedBy(2d), is(50, 100));
        assertThat(duration("100ms 500µs").dividedBy(2d), is(50, 250 * 1000));
        assertThat(duration("100ms 200ns").dividedBy(2L), is(50, 100));
        assertThat(duration("100ms 500µs").dividedBy(2L), is(50, 250 * 1000));
    }

    @Test
    public void isLessThan() {
        assertThat(duration("10ms 10ns").isLessThan("10ms 10ns"), is(false));
        assertThat(duration("10ms 10µs").isLessThan("10ms 10µs"), is(false));

        assertThat(duration("10ms 10ns").isLessThan("11ms 10ns"), is(true));
        assertThat(duration("10ms 10ns").isLessThan("9ms 10ns"), is(false));
        assertThat(duration("10ms 10µs").isLessThan("11ms 10µs"), is(true));
        assertThat(duration("10ms 10µs").isLessThan("9ms 10µs"), is(false));

        assertThat(duration("10ms 10ns").isLessThan("10ms 11ns"), is(true));
        assertThat(duration("10ms 10ns").isLessThan("10ms 9ns"), is(false));
        assertThat(duration("10ms 10µs").isLessThan("10ms 11µs"), is(true));
        assertThat(duration("10ms 10µs").isLessThan("10ms 9µs"), is(false));
    }

    @Test
    public void isLessThanOrEqualTo() {
        assertThat(duration("10ms 10ns").isLessThanOrEqualTo("10ms 10ns"), is(true));
        assertThat(duration("10ms 10µs").isLessThanOrEqualTo("10ms 10µs"), is(true));

        assertThat(duration("10ms 10ns").isLessThanOrEqualTo("11ms 10ns"), is(true));
        assertThat(duration("10ms 10ns").isLessThanOrEqualTo("9ms 10ns"), is(false));
        assertThat(duration("10ms 10µs").isLessThanOrEqualTo("11ms 10µs"), is(true));
        assertThat(duration("10ms 10µs").isLessThanOrEqualTo("9ms 10µs"), is(false));

        assertThat(duration("10ms 10ns").isLessThanOrEqualTo("10ms 11ns"), is(true));
        assertThat(duration("10ms 10ns").isLessThanOrEqualTo("10ms 9ns"), is(false));
        assertThat(duration("10ms 10µs").isLessThanOrEqualTo("10ms 11µs"), is(true));
        assertThat(duration("10ms 10µs").isLessThanOrEqualTo("10ms 9µs"), is(false));
    }

    @Test
    public void isGreaterThan() {
        assertThat(duration("10ms 10ns").isGreaterThan("10ms 10ns"), is(false));
        assertThat(duration("10ms 10µs").isGreaterThan("10ms 10µs"), is(false));

        assertThat(duration("10ms 10ns").isGreaterThan("11ms 10ns"), is(false));
        assertThat(duration("10ms 10ns").isGreaterThan("9ms 10ns"), is(true));
        assertThat(duration("10ms 10µs").isGreaterThan("11ms 10µs"), is(false));
        assertThat(duration("10ms 10µs").isGreaterThan("9ms 10µs"), is(true));

        assertThat(duration("10ms 10ns").isGreaterThan("10ms 11ns"), is(false));
        assertThat(duration("10ms 10ns").isGreaterThan("10ms 9ns"), is(true));
        assertThat(duration("10ms 10µs").isGreaterThan("10ms 11µs"), is(false));
        assertThat(duration("10ms 10µs").isGreaterThan("10ms 9µs"), is(true));
    }

    @Test
    public void isGreaterThanOrEqualTo() {
        assertThat(duration("10ms 10ns").isGreaterThanOrEqualTo("10ms 10ns"), is(true));
        assertThat(duration("10ms 10µs").isGreaterThanOrEqualTo("10ms 10µs"), is(true));

        assertThat(duration("10ms 10ns").isGreaterThanOrEqualTo("11ms 10ns"), is(false));
        assertThat(duration("10ms 10ns").isGreaterThanOrEqualTo("9ms 10ns"), is(true));
        assertThat(duration("10ms 10µs").isGreaterThanOrEqualTo("11ms 10µs"), is(false));
        assertThat(duration("10ms 10µs").isGreaterThanOrEqualTo("9ms 10µs"), is(true));

        assertThat(duration("10ms 10ns").isGreaterThanOrEqualTo("10ms 11ns"), is(false));
        assertThat(duration("10ms 10ns").isGreaterThanOrEqualTo("10ms 9ns"), is(true));
        assertThat(duration("10ms 10µs").isGreaterThanOrEqualTo("10ms 11µs"), is(false));
        assertThat(duration("10ms 10µs").isGreaterThanOrEqualTo("10ms 9µs"), is(true));
    }

    @Test
    public void in() {
        final Duration oneHour = new Duration("1h");
        assertThat(oneHour.in(DAYS), equalTo(0L));
        assertThat(oneHour.in(HOURS), equalTo(1L));
        assertThat(oneHour.in(MINUTES), equalTo(60L));
        assertThat(oneHour.in(SECONDS), equalTo(60L * 60L));
        assertThat(oneHour.in(MILLISECONDS), equalTo(60L * 60L * 1000L));
        assertThat(oneHour.in(NANOSECONDS), equalTo(60L * 60L * 1000L * 1000L * 1000L));
    }

    @Test
    public void isEmpty() {
        assertThat(duration("0").isEmpty(), is(true));
        assertThat(duration("1ns").isEmpty(), is(false));
        assertThat(duration("1ms").isEmpty(), is(false));
    }

    @Test
    public void hasContent() {
        assertThat(duration("0").hasContent(), is(false));
        assertThat(duration("1ns").hasContent(), is(true));
        assertThat(duration("1ms").hasContent(), is(true));
    }

    @Test
    public void toUnitToValue() {
        assertThat(duration("0").toUnitToValue(), hasSameEntriesAs());
        assertThat(duration("1").toUnitToValue(), hasSameEntriesAs(
            MILLISECONDS, 1
        ));
        assertThat(duration("4s 3ms 2µs 1ns").toUnitToValue(), hasSameEntriesAs(
            SECONDS, 4,
            MILLISECONDS, 3,
            MICROSECONDS, 2,
            NANOSECONDS, 1
        ));
    }

    @Test
    public void equals() {
        assertThat(duration(100, 100).equals(duration(100, 100)), is(true));
        assertThat(duration("66s 66µs").equals(duration("66s 66µs")), is(true));

        assertThat(duration(101, 100).equals(duration(100, 100)), is(false));
        assertThat(duration(100, 101).equals(duration(100, 100)), is(false));
        assertThat(duration("67s 66µs").equals(duration("66s 66µs")), is(false));
        assertThat(duration("66s 67µs").equals(duration("66s 66µs")), is(false));
    }

    @Test
    public void generateHashCode() {
        assertThat(duration(100, 100).hashCode(), equalTo(duration(100, 100).hashCode()));
        assertThat(duration("66s 66µs").hashCode(), equalTo(duration("66s 66µs").hashCode()));

        assertThat(duration(101, 100).hashCode(), not(equalTo(duration(100, 100).hashCode())));
        assertThat(duration(100, 101).hashCode(), not(equalTo(duration(100, 100).hashCode())));
        assertThat(duration("67s 66µs").hashCode(), not(equalTo(duration("66s 66µs").hashCode())));
        assertThat(duration("66s 67µs").hashCode(), not(equalTo(duration("66s 66µs").hashCode())));
    }

    @Test
    public void plus() {
        assertThat(duration("900ms 900µs").plus("50ms 50µs"), is(950, 950000));
        assertThat(duration("900ms 900µs").plus("50ms 150µs"), is(951, 50000));
    }

    @Test
    public void minus() {
        assertThat(duration("900ms 900µs").minus("50ms 50µs"), is(850, 850000));
        assertThat(duration("900ms 700µs").minus("50ms 750µs"), is(849, 950000));
    }

}

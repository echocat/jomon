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

package org.echocat.jomon.runtime.date;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static java.util.Calendar.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CalendarDateUnitTest {

    @Test
    public void testCreation() throws Exception {
        assertThat(new CalendarDate((String)null), is(new CalendarDate(null, null, null)));
        assertThat(new CalendarDate(""), is(new CalendarDate(null, null, null)));
        assertThat(new CalendarDate("?"), is(new CalendarDate(null, null, null)));

        assertThat(new CalendarDate("1"), is(new CalendarDate(1, null, null)));
        assertThat(new CalendarDate("1-*"), is(new CalendarDate(1, null, null)));
        assertThat(new CalendarDate("1-*-*"), is(new CalendarDate(1, null, null)));
        assertThat(new CalendarDate("1-??-?"), is(new CalendarDate(1, null, null)));
        assertThat(new CalendarDate("-1"), is(new CalendarDate(-1, null, null)));

        assertThat(new CalendarDate("*-2"), is(new CalendarDate(null, 2, null)));
        assertThat(parsingOf("*--2"), throwsIllegalArgumentException());
        assertThat(parsingOf("*-?2"), throwsIllegalArgumentException());

        assertThat(new CalendarDate("*-*-3"), is(new CalendarDate(null, null, 3)));
        assertThat(parsingOf("*-*--3"), throwsIllegalArgumentException());
        assertThat(parsingOf("*-*-?3"), throwsIllegalArgumentException());

        assertThat(new CalendarDate("1-2-3"), is(new CalendarDate(1, 2, 3)));
        assertThat(new CalendarDate("1900-01-02"), is(new CalendarDate(1900, 1, 2)));
        assertThat(new CalendarDate("1900-??-02"), is(new CalendarDate(1900, null, 2)));
    }

    @Test
    public void testToString() throws Exception {
        assertThat(new CalendarDate(1, 2, 3).toString(), is("0001-02-03"));
        assertThat(new CalendarDate(null, 2, 3).toString(), is("????-02-03"));
        assertThat(new CalendarDate(1, null, 3).toString(), is("0001-??-03"));
        assertThat(new CalendarDate(1, 2, null).toString(), is("0001-02-??"));
    }

    @Test
    public void testCreationWithDirectValues() throws Exception {
        assertThat(creationOf(1, 2, 3), produces(new CalendarDate(1, 2, 3)));
        assertThat(creationOf(null, 2, 3), produces(new CalendarDate(null, 2, 3)));
        assertThat(creationOf(1, null, 3), produces(new CalendarDate(1, null, 3)));
        assertThat(creationOf(1, 2, null), produces(new CalendarDate(1, 2, null)));

        assertThat(creationOf(-1, 2, 3), produces(new CalendarDate(-1, 2, 3)));
        assertThat(creationOf(100000, 2, 3), produces(new CalendarDate(100000, 2, 3)));
        assertThat(creationOf(-100000, 2, 3), produces(new CalendarDate(-100000, 2, 3)));

        assertThat(creationOf(1, 0, 3), throwsIllegalArgumentException());
        assertThat(creationOf(1, 2, 0), throwsIllegalArgumentException());
        assertThat(creationOf(1, 13, 3), throwsIllegalArgumentException());
        assertThat(creationOf(1, 2, 32), throwsIllegalArgumentException());
    }

    @Test
    public void testAsCalendarWithAllValues() throws Exception {
        final Calendar base = new GregorianCalendar();
        base.set(YEAR, 666);
        base.set(HOUR_OF_DAY, 6);
        base.set(MINUTE, 6);
        base.set(SECOND, 6);
        base.set(MILLISECOND, 6);
        final Calendar expected = new GregorianCalendar();
        expected.set(YEAR, 1);
        expected.set(MONTH, 1);
        expected.set(DAY_OF_MONTH, 3);
        expected.set(HOUR_OF_DAY, 6);
        expected.set(MINUTE, 6);
        expected.set(SECOND, 6);
        expected.set(MILLISECOND, 6);
        assertThat(toCalendarOf(1, 2, 3, base), is(expected));
    }

    @Test
    public void testAsCalendarWithOnlyYear() throws Exception {
        final Calendar base = new GregorianCalendar();
        base.set(YEAR, 666);
        base.set(MONTH, 6);
        base.set(DAY_OF_MONTH, 6);
        base.set(HOUR_OF_DAY, 6);
        base.set(MINUTE, 6);
        base.set(SECOND, 6);
        base.set(MILLISECOND, 6);
        final Calendar expected = new GregorianCalendar();
        expected.set(YEAR, 1);
        expected.set(MONTH, 6);
        expected.set(DAY_OF_MONTH, 6);
        expected.set(HOUR_OF_DAY, 6);
        expected.set(MINUTE, 6);
        expected.set(SECOND, 6);
        expected.set(MILLISECOND, 6);
        assertThat(toCalendarOf(1, null, null, base), is(expected));
    }

    @Test
    public void testAsCalendarWithOnlyMonth() throws Exception {
        final Calendar base = new GregorianCalendar();
        base.set(YEAR, 666);
        base.set(MONTH, 6);
        base.set(DAY_OF_MONTH, 6);
        base.set(HOUR_OF_DAY, 6);
        base.set(MINUTE, 6);
        base.set(SECOND, 6);
        base.set(MILLISECOND, 6);
        final Calendar expected = new GregorianCalendar();
        expected.set(YEAR, 666);
        expected.set(MONTH, 1);
        expected.set(DAY_OF_MONTH, 6);
        expected.set(HOUR_OF_DAY, 6);
        expected.set(MINUTE, 6);
        expected.set(SECOND, 6);
        expected.set(MILLISECOND, 6);
        assertThat(toCalendarOf(null, 2, null, base), is(expected));
    }

    @Test
    public void testAsCalendarWithOnlyDay() throws Exception {
        final Calendar base = new GregorianCalendar();
        base.set(YEAR, 666);
        base.set(MONTH, 6);
        base.set(DAY_OF_MONTH, 6);
        base.set(HOUR_OF_DAY, 6);
        base.set(MINUTE, 6);
        base.set(SECOND, 6);
        base.set(MILLISECOND, 6);
        final Calendar expected = new GregorianCalendar();
        expected.set(YEAR, 666);
        expected.set(MONTH, 6);
        expected.set(DAY_OF_MONTH, 3);
        expected.set(HOUR_OF_DAY, 6);
        expected.set(MINUTE, 6);
        expected.set(SECOND, 6);
        expected.set(MILLISECOND, 6);
        assertThat(toCalendarOf(null, null, 3, base), is(expected));
    }

    @Nullable
    protected static String parsingOf(@Nullable String pattern) {
        return pattern;
    }

    @Nonnull
    protected static Matcher<Object> throwsIllegalArgumentException() {
        return new TypeSafeMatcher<Object>() {
            @Override
            public boolean matchesSafely(Object item) {
                boolean exceptionThrown;
                try {
                    if (item instanceof String) {
                        new CalendarDate(item.toString());
                    } else if (item instanceof Integer[]) {
                        final Integer[] values = (Integer[]) item;
                        new CalendarDate(values[0], values[1], values[2]);
                    }
                    exceptionThrown = false;
                } catch (IllegalArgumentException ignored) {
                    exceptionThrown = true;
                }
                return exceptionThrown;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("throws IllegalArgumentException");
            }
        };
    }

    @Nonnull
    protected static Matcher<Integer[]> produces(@Nonnull final CalendarDate expected) {
        return new TypeSafeMatcher<Integer[]>() {
            @Override
            public boolean matchesSafely(Integer[] values) {
                return new CalendarDate(values[0], values[1], values[2]).equals(expected);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("produces ").appendValue(expected);
            }
        };
    }

    @Nonnull
    protected static Calendar toCalendarOf(@Nullable Integer year, @Nullable Integer month, @Nullable Integer day, @Nonnull Calendar withBase) {
        return new CalendarDate(year, month, day).toCalendar(withBase);
    }

    @Nonnull
    protected static Integer[] creationOf(@Nullable Integer year, @Nullable Integer month, @Nullable Integer day) {
        return new Integer[]{year, month, day};
    }

}

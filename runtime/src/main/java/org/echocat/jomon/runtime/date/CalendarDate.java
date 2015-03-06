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

import com.google.common.base.Predicate;
import org.echocat.jomon.runtime.date.CalendarDate.Adapter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.util.Calendar.*;
import static java.util.regex.Pattern.MULTILINE;
import static java.util.regex.Pattern.compile;

@ThreadSafe
@Immutable
@XmlJavaTypeAdapter(Adapter.class)
public class CalendarDate implements Predicate<CalendarDate> {

    private static final Pattern PATTERN_PARSER = compile("((?:-?\\d{1,4}|\\?+|\\*))(?:|-(\\d{1,2}|\\?+|\\*)(?:|-(\\d{1,2}|\\?+|\\*)))");

    private final Integer _year;
    private final Integer _month;
    private final Integer _day;

    public CalendarDate(@Nullable String string) throws IllegalArgumentException {
        if (string != null && !string.trim().isEmpty()) {
            final Matcher matcher = PATTERN_PARSER.matcher(string);
            if (matcher.matches()) {
                _year = parsePart(string, matcher.group(1), "year");
                _month = validateMonth(parsePart(string, matcher.group(2), "month"));
                _day = validateDay(parsePart(string, matcher.group(3), "day"));
            } else {
                throw new IllegalArgumentException("Invalid pattern: " + string);
            }
        } else {
            _year = null;
            _month = null;
            _day = null;
        }
    }

    public CalendarDate(@Nullable Date date) throws IllegalArgumentException {
        this(calendarFromDate(date));
    }

    public CalendarDate(@Nullable Calendar calendar) throws IllegalArgumentException {
        this(
            calendar != null ? calendar.get(YEAR) : null,
            calendar != null ? calendar.get(MONTH) + 1 : null,
            calendar != null ? calendar.get(DAY_OF_MONTH) : null
        );
    }

    public CalendarDate(@Nullable Integer year, @Nullable Integer month, @Nullable Integer day) throws IllegalArgumentException {
        _year = year;
        _month = validateMonth(month);
        _day = validateDay(day);
    }

    @Nullable
    public Integer getYear() {
        return _year;
    }

    @Nullable
    public Integer getMonth() {
        return _month;
    }

    @Nullable
    public Integer getDay() {
        return _day;
    }

    public boolean hasYear() {
        return _year != null;
    }

    public boolean hasMonth() {
        return _month != null;
    }

    public boolean hasDay() {
        return _day != null;
    }

    public boolean hasValue() {
        return hasYear() || hasMonth() || hasDay();
    }

    @Nonnull
    public Calendar toCalendar(@Nonnull Calendar base) {
        if (_year != null) {
            base.set(YEAR, _year);
        }
        if (_month != null) {
            base.set(MONTH, _month - 1);
        }
        if (_day != null) {
            base.set(DAY_OF_MONTH, _day);
        }
        return base;
    }

    @Nonnull
    public Date toDate(@Nonnull Date base) {
        final GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(base);
        return toCalendar(calendar).getTime();
    }

    @Nonnull
    public CalendarDate asMonthAndDayOnly() {
        return new CalendarDate(null, _month, _day);
    }

    @Nonnull
    public CalendarDate asDayOnly() {
        return new CalendarDate(null, null, _day);
    }

    @Nonnull
    public CalendarDate asMonthOnly() {
        return new CalendarDate(null, _month, null);
    }

    @Nonnull
    public CalendarDate asYearOnly() {
        return new CalendarDate(_year, null, null);
    }

    @Override
    public boolean apply(@Nullable CalendarDate input) {
        final boolean result;
        if (input != null) {
            result = (_year == null || _year.equals(input.getYear())) && (_month == null || _month.equals(input.getMonth())) && (_day == null || _day.equals(input.getDay()));
        } else {
            result = _year == null && _month == null && _day == null;
        }
        return result;
    }

    public boolean matches(@Nullable CalendarDate input) {
        return apply(input);
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else if (!(o instanceof CalendarDate)) {
            result = false;
        } else {
            final CalendarDate that = (CalendarDate) o;
            result = (_day != null ? _day.equals(that.getDay()) : that.getDay() == null)
                && (_month != null ? _month.equals(that.getMonth()) : that.getMonth() == null)
                && (_year != null ? _year.equals(that.getYear()) : that.getYear() == null);
        }
        return result;
    }

    @Override
    public int hashCode() {
        int result = _year != null ? _year.hashCode() : 0;
        result = 31 * result + (_month != null ? _month.hashCode() : 0);
        result = 31 * result + (_day != null ? _day.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(_year != null ? format("%04d", _year) : "????");
        sb.append('-').append(_month != null ? format("%02d", _month) : "??");
        sb.append('-').append(_day != null ? format("%02d", _day) : "??");
        return sb.toString();
    }

    @Nonnull
    public Pattern toPattern() {
        final StringBuilder sb = new StringBuilder();
        sb.append('^');
        sb.append(_year != null ? format("%04d", _year) : "[\\d?*]+");
        sb.append('-').append(_month != null ? format("%02d", _month) : "[\\d?*]+");
        sb.append('-').append(_day != null ? format("%02d", _day) : "[\\d?*]+");
        sb.append('$');
        return compile(sb.toString(), MULTILINE);
    }

    @Nullable
    protected static Calendar calendarFromDate(@Nullable Date date) {
        final GregorianCalendar calendar;
        if (date != null) {
            calendar = new GregorianCalendar();
            calendar.setTime(date);
        } else {
            calendar = null;
        }
        return calendar;
    }

    @Nullable
    protected static Integer parsePart(@Nonnull String string, @Nonnull String plain, @Nonnull String name) throws IllegalArgumentException {
        final Integer result;
        if (plain == null || plain.startsWith("?") || plain.equals("*")) {
            result = null;
        } else {
            try {
                result = parseInt(plain);
            } catch (final NumberFormatException e) {
                throw new IllegalArgumentException("Could not parse " + name + " of pattern: " + string, e);
            }
        }
        return result;
    }

    @Nullable
    protected static Integer validateMonth(@Nullable Integer month) throws IllegalArgumentException {
        if (month != null && (month < 1 || month > 12)) {
            throw new IllegalArgumentException("Month " + month + " is not valid.");
        }
        return month;
    }

    @Nullable
    protected static Integer validateDay(@Nullable Integer day) throws IllegalArgumentException {
        if (day != null && (day < 1 || day > 31)) {
            throw new IllegalArgumentException("Day " + day + " is not valid.");
        }
        return day;
    }

    public static class Adapter extends XmlAdapter<String, CalendarDate> {

        @Override
        public CalendarDate unmarshal(String v) throws Exception {
            return v != null ? new CalendarDate(v) : null;
        }

        @Override
        public String marshal(CalendarDate v) throws Exception {
            return v != null ? v.toString() : null;
        }
    }

}

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

import org.echocat.jomon.runtime.util.Duration.Adapter;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.lang.Character.isDigit;
import static java.lang.Character.isWhitespace;
import static java.lang.Long.valueOf;
import static java.lang.Math.round;
import static java.lang.Thread.currentThread;
import static java.util.Collections.unmodifiableMap;
import static java.util.concurrent.TimeUnit.*;
import static org.echocat.jomon.runtime.StringUtils.addElement;

/**
 * <h1>Synopsis</h1>
 * <p>A {@link Duration} represents a duration of time. The minimum unit are milliseconds.</p>
 *
 * <p>It could be defined by an amount of milliseconds, a combination of amount and {@link TimeUnit} and a string pattern.</p>
 *
 * <h1>Pattern</h1>
 *
 * <h2>Syntax</h2>
 * <p><code>&lt;amount&gt;[&lt;unit&gt;][[ ]...]</code></p>
 *
 * <h2>Units</h2>
 * <ul>
 *     <li><code>S</code>/<code>ms</code>: milliseconds</li>
 *     <li><code>s</code>: seconds</li>
 *     <li><code>m</code>: minutes</li>
 *     <li><code>h</code>: hours</li>
 *     <li><code>d</code>: days</li>
 *     <li><code>w</code>: weeks</li>
 * </ul>
 *
 * <h2>Examples</h2>
 * <p>
 *     <code>new Duration("1ms").toMilliSeconds() == 1</code><br>
 *     <code>new Duration("1s").toMilliSeconds() == 1,000</code><br>
 *     <code>new Duration("1m").toMilliSeconds() == 60,000</code><br>
 *     <code>new Duration("1m10s").toMilliSeconds() == 70,000</code><br>
 *     <code>new Duration("1m 10s").toMilliSeconds() == 70,000</code><br>
 *     <code>new Duration("15").toMilliSeconds() == 15</code><br>
 *     <code>new Duration("2s 15").toMilliSeconds() == 2,015</code><br>
 * </p>
 *
 */
@Immutable
@ThreadSafe
@XmlJavaTypeAdapter(Adapter.class)
public class Duration implements Comparable<Duration>, Serializable {

    private static final long serialVersionUID = 3L;
    private static final int MAXIMUM_NANOSECONDS_VALUE = 999999;

    public static void sleep(@Nonnull Duration duration) throws InterruptedException {
        duration.sleep();
    }

    public static void sleep(@Nonnull String duration) throws InterruptedException {
        sleep(new Duration(duration));
    }

    public static void sleep(@Nonnegative long amount, @Nonnull TimeUnit unit) throws InterruptedException {
        sleep(new Duration(amount, unit));
    }

    public static void sleep(@Nonnegative long milliSeconds) throws InterruptedException {
        sleep(new Duration(milliSeconds));
    }

    public static void sleep(@Nonnegative long milliSeconds, @Nonnegative int nanoSeconds) throws InterruptedException {
        sleep(new Duration(milliSeconds, nanoSeconds));
    }

    public static void sleepSafe(@Nonnull Duration duration) throws GotInterruptedException {
        duration.sleepSafe();
    }

    public static void sleepSafe(@Nonnull String duration) throws GotInterruptedException {
        sleepSafe(new Duration(duration));
    }

    public static void sleepSafe(@Nonnegative long amount, @Nonnull TimeUnit unit) throws GotInterruptedException {
        sleepSafe(new Duration(amount, unit));
    }

    public static void sleepSafe(@Nonnegative long milliSeconds) throws GotInterruptedException {
        sleepSafe(new Duration(milliSeconds));
    }

    public static void sleepSafe(@Nonnegative long milliSeconds, @Nonnegative int nanoSeconds) throws GotInterruptedException {
        sleepSafe(new Duration(milliSeconds, nanoSeconds));
    }

    @Nonnull
    public static Duration duration(@Nonnull Duration duration) {
        return duration;
    }

    @Nullable
    public static Duration duration(@Nullable String duration) {
        return duration != null ? new Duration(duration) : null;
    }

    @Nullable
    public static Duration duration(@Nonnegative long milliSeconds) {
        return duration(milliSeconds, 0);
    }

    @Nullable
    public static Duration duration(@Nonnegative long milliSeconds, @Nonnegative int nanoSeconds) {
        return new Duration(milliSeconds, nanoSeconds);
    }

    @Nonnull
    public static Duration durationOf(@Nonnull Duration duration) {
        return duration(duration);
    }

    @Nullable
    public static Duration durationOf(@Nullable String duration) {
        return duration(duration);
    }

    @Nullable
    public static Duration durationOf(@Nonnegative long duration) {
        return duration(duration);
    }

    @Nullable
    public static Duration durationOf(@Nonnegative long milliSeconds, @Nonnegative int nanoSeconds) {
        return duration(milliSeconds, nanoSeconds);
    }

    @Nonnegative
    private final long _milliSeconds;
    @Nonnegative
    private final int _nanoSeconds;

    public Duration(@Nonnegative long milliSeconds) {
        this(milliSeconds, 0);
    }

    /**
     * @param plain See {@link Duration}
     */
    public Duration(@Nonnull String plain) throws IllegalArgumentException {
        this(parsePattern(plain));
    }

    public Duration(@Nonnegative long duration, @Nonnull TimeUnit unit) {
        this(new MilliAndNanoSeconds(duration, unit));
    }

    public Duration(@Nonnegative long milliSeconds, @Nonnegative int nanoSeconds) {
        this(new MilliAndNanoSeconds(milliSeconds, nanoSeconds));
    }

    public Duration(@Nonnull Date from, @Nonnull Date to) {
        if (from.after(to)) {
            throw new IllegalArgumentException("From " + from + " is after to " + to + "?");
        }
        _milliSeconds = to.getTime() - from.getTime();
        _nanoSeconds = 0;
    }

    protected Duration(@Nonnull MilliAndNanoSeconds seconds) {
        _milliSeconds = seconds.getMilliSeconds();
        _nanoSeconds = seconds.getNanoSeconds();
        if (_milliSeconds < 0) {
            throw new IllegalArgumentException("MilliSeconds value is negative.");
        }
        if (_nanoSeconds < 0) {
            throw new IllegalArgumentException("NanoSeconds value is negative.");
        }
        if (_nanoSeconds > MAXIMUM_NANOSECONDS_VALUE) {
            throw new IllegalArgumentException("NanoSecond value out of range: is " + _nanoSeconds + "; maximum " + MAXIMUM_NANOSECONDS_VALUE);
        }
    }

    @Nonnull
    public Duration plus(@Nullable Duration duration) {
        final long calculatedNanos = getNanoSeconds() + duration.getNanoSeconds();
        final long additionalMillis = calculatedNanos / 1000000L;
        final int nanos = (int) (calculatedNanos - (additionalMillis * 1000000L));
        return new Duration(getMilliSeconds() + duration.getMilliSeconds() + additionalMillis, nanos);
    }

    @Nonnull
    public Duration plus(@Nullable String duration) {
        return plus(duration != null ? new Duration(duration) : null);
    }

    @Nonnull
    public Duration plus(@Nonnegative long amount, @Nonnull TimeUnit unit) {
        return plus(new Duration(amount, unit));
    }

    @Nonnull
    public Duration plus(@Nonnegative long milliSeconds) {
        return plus(new Duration(milliSeconds));
    }

    @Nonnull
    public Duration minus(@Nullable Duration duration) {
        final long calculatedMillis = getMilliSeconds() - duration.getMilliSeconds();
        final int calculatedNanos = getNanoSeconds() - duration.getNanoSeconds();
        final int nanos = calculatedNanos >= 0 ? calculatedNanos : 1000000 + calculatedNanos;
        final long millis = calculatedMillis - (calculatedNanos >= 0 ? 0 : 1);
        if (millis < 0) {
            throw new IllegalArgumentException("The result of " + this + " minus " + duration + " is negative.");
        }
        return new Duration(millis, nanos);
    }

    @Nonnull
    public Duration minus(@Nullable String duration) {
        return minus(duration != null ? new Duration(duration) : null);
    }

    @Nonnull
    public Duration minus(@Nonnegative long amount, @Nonnull TimeUnit unit) {
        return minus(new Duration(amount, unit));
    }

    @Nonnull
    public Duration minus(@Nonnegative long milliSeconds) {
        return minus(new Duration(milliSeconds));
    }

    @Nonnull
    public Duration multiplyBy(@Nonnegative double what) {
        if (what < 0) {
            throw new IllegalArgumentException();
        }
        final double calculatedMillis = getMilliSeconds() * what;
        final long millis = (long) calculatedMillis;
        final long additionalNanos = round((calculatedMillis - (double) millis) * 1000000d);
        final long calculatedNanos = round(getNanoSeconds() * what) + additionalNanos;
        final long additionalMillis = calculatedNanos / 1000000L;
        final int nanos = (int) (calculatedNanos - (additionalMillis * 1000000L));
        return new Duration(millis + additionalMillis, nanos);
    }

    @Nonnull
    public Duration dividedBy(@Nonnegative double what) {
        if (what < 0) {
            throw new IllegalArgumentException();
        }
        final double calculatedMillis = getMilliSeconds() / what;
        final long millis = round(calculatedMillis);
        final long additionalNanos = round((calculatedMillis - (double) millis) * 1000000d);
        final long calculatedNanos = round(getNanoSeconds() / what) + additionalNanos;
        final long additionalMillis = calculatedNanos / 1000000L;
        final int nanos = (int) (calculatedNanos - (additionalMillis * 1000000L));
        return new Duration(millis + additionalMillis, nanos);
    }

    @Nonnull
    public Duration multiplyBy(@Nonnegative long what) {
        if (what < 0) {
            throw new IllegalArgumentException();
        }
        final long millis = getMilliSeconds() * what;
        final long calculatedNanos = getNanoSeconds() * what;
        final long additionalMillis = calculatedNanos / 1000000L;
        final int nanos = (int) (calculatedNanos - (additionalMillis * 1000000L));
        return new Duration(millis + additionalMillis, nanos);
    }

    @Nonnull
    public Duration dividedBy(@Nonnegative long what) {
        if (what < 0) {
            throw new IllegalArgumentException();
        }
        final long millis = getMilliSeconds() / what;
        final long calculatedNanos = getNanoSeconds() / what;
        final long additionalMillis = calculatedNanos / 1000000L;
        final int nanos = (int) (calculatedNanos - (additionalMillis * 1000000L));
        return new Duration(millis + additionalMillis, nanos);
    }

    @Nonnull
    public Duration trim(@Nonnull TimeUnit toUnit) {
        final Duration result;
        if (toUnit == NANOSECONDS) {
            result = this;
        } else if (toUnit == MICROSECONDS) {
            result = new Duration(getMilliSeconds(), (getNanoSeconds() / 1000) * 1000);
        } else {
            result = new Duration(toUnit.toMillis(toUnit.convert(getMilliSeconds(), MILLISECONDS)));
        }
        return result;
    }

    /**
     * @return the duration in milli seconds.
     * @deprecated Please use {@link #in(TimeUnit)} in the future.
     */
    @Nonnegative
    @Deprecated
    public long toMilliSeconds() {
        return in(MILLISECONDS);
    }

    @Nonnegative
    protected long getMilliSeconds() {
        return _milliSeconds;
    }

    @Nonnegative
    protected int getNanoSeconds() {
        return _nanoSeconds;
    }

    @Nonnegative
    public long in(@Nonnull TimeUnit unit) {
        final long milliSeconds = getMilliSeconds();
        final int nanoSeconds = getNanoSeconds();
        final long result;
        if (unit != MICROSECONDS && unit != NANOSECONDS && milliSeconds > 0 && nanoSeconds > 0) {
            result = unit.convert((milliSeconds * 1000L) + (long) nanoSeconds, NANOSECONDS);
        } else if (milliSeconds > 0) {
            result = unit.convert(milliSeconds, MILLISECONDS);
        } else {
            result = unit.convert(nanoSeconds, NANOSECONDS);
        }
        return result;
    }

    /**
     * @return See {@link Duration pattern}.
     */
    @Nonnull
    public String toPattern() {
        return toPattern(getMilliSeconds(), getNanoSeconds());
    }

    @Nonnull
    public Map<TimeUnit, Long> toUnitToValue() {
        return toUnitToValue(getMilliSeconds(), getNanoSeconds());
    }

    public boolean isEmpty() {
        return getMilliSeconds() <= 0 && getNanoSeconds() <= 0;
    }

    public boolean hasContent() {
        return getMilliSeconds() > 0 || getNanoSeconds() > 0;
    }

    public boolean isLessThan(@Nullable String other) {
        return isLessThan(other != null ? new Duration(other) : null);
    }

    public boolean isLessThan(@Nullable Duration other) {
        return other != null ? isLessThan(other.getMilliSeconds(), other.getNanoSeconds()) : isLessThan(0);
    }

    public boolean isLessThan(@Nonnegative long amount, @Nonnull TimeUnit unit) {
        return isLessThan(new Duration(amount, unit));
    }

    public boolean isLessThan(@Nonnegative long milliSeconds) {
        return isLessThan(milliSeconds, 0);
    }

    protected boolean isLessThan(@Nonnegative long milliSeconds, @Nonnegative int nanoSeconds) {
        final long localMilliSeconds = getMilliSeconds();
        return localMilliSeconds < milliSeconds || (localMilliSeconds == milliSeconds && getNanoSeconds() < nanoSeconds);
    }

    public boolean isLessThanOrEqualTo(@Nullable String other) {
        return isLessThanOrEqualTo(other != null ? new Duration(other) : null);
    }

    public boolean isLessThanOrEqualTo(@Nullable Duration other) {
        return other != null ? isLessThanOrEqualTo(other.getMilliSeconds(), other.getNanoSeconds()) : isLessThanOrEqualTo(0);
    }

    public boolean isLessThanOrEqualTo(@Nonnegative long amount, @Nonnull TimeUnit unit) {
        return isLessThanOrEqualTo(new Duration(amount, unit));
    }

    public boolean isLessThanOrEqualTo(@Nonnegative long milliSeconds) {
        return isLessThanOrEqualTo(milliSeconds, 0);
    }

    protected boolean isLessThanOrEqualTo(@Nonnegative long milliSeconds, @Nonnegative int nanoSeconds) {
        final long localMilliSeconds = getMilliSeconds();
        return localMilliSeconds < milliSeconds || (localMilliSeconds == milliSeconds && getNanoSeconds() <= nanoSeconds);
    }

    public boolean isGreaterThan(@Nullable String other) {
        return isGreaterThan(other != null ? new Duration(other) : null);
    }

    public boolean isGreaterThan(@Nullable Duration other) {
        return other != null ? isGreaterThan(other.getMilliSeconds(), other.getNanoSeconds()) : isGreaterThan(0);
    }

    public boolean isGreaterThan(@Nonnegative long amount, @Nonnull TimeUnit unit) {
        return isGreaterThan(new Duration(amount, unit));
    }

    public boolean isGreaterThan(@Nonnegative long milliSeconds) {
        return isGreaterThan(milliSeconds, 0);
    }

    protected boolean isGreaterThan(@Nonnegative long milliSeconds, @Nonnegative int nanoSeconds) {
        final long localMilliSeconds = getMilliSeconds();
        return localMilliSeconds > milliSeconds || (localMilliSeconds == milliSeconds && getNanoSeconds() > nanoSeconds);
    }

    public boolean isGreaterThanOrEqualTo(@Nullable String other) {
        return isGreaterThanOrEqualTo(other != null ? new Duration(other) : null);
    }

    public boolean isGreaterThanOrEqualTo(@Nullable Duration other) {
        return other != null ? isGreaterThanOrEqualTo(other.getMilliSeconds(), other.getNanoSeconds()) : isGreaterThanOrEqualTo(0);
    }

    public boolean isGreaterThanOrEqualTo(@Nonnegative long amount, @Nonnull TimeUnit unit) {
        return isGreaterThanOrEqualTo(new Duration(amount, unit));
    }

    public boolean isGreaterThanOrEqualTo(@Nonnegative long milliSeconds) {
        return isGreaterThanOrEqualTo(milliSeconds, 0);
    }

    protected boolean isGreaterThanOrEqualTo(@Nonnegative long milliSeconds, @Nonnegative int nanoSeconds) {
        final long localMilliSeconds = getMilliSeconds();
        return localMilliSeconds > milliSeconds || (localMilliSeconds == milliSeconds && getNanoSeconds() >= nanoSeconds);
    }

    public void sleep() throws InterruptedException {
        Thread.sleep(getMilliSeconds(), getNanoSeconds());
    }

    public void sleepSafe() throws GotInterruptedException {
        try {
            sleep();
        } catch (final InterruptedException e) {
            currentThread().interrupt();
            throw new GotInterruptedException(e);
        }
    }

    @Override
    public int compareTo(Duration other) {
        final int resultOnMilliSeconds = compare(getMilliSeconds(), other != null ? other.getMilliSeconds() : 0);
        final int result;
        if (resultOnMilliSeconds == 0) {
            result =  compare(getNanoSeconds(), other != null ? other.getNanoSeconds() : 0);
        } else {
            result = resultOnMilliSeconds;
        }
        return result;
    }

    private static int compare(@Nonnegative long self, @Nonnegative long other) {
        // noinspection NestedConditionalExpression
        final int result = ((self < other) ? -1 : ((self == other) ? 0 : 1));
        return result;
    }

    @Override
    public int hashCode() {
        final long milliSeconds = getMilliSeconds();
        final int nanoSeconds = getNanoSeconds();
        return (int) (milliSeconds ^ (milliSeconds >>> 32)) * nanoSeconds;
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else if (o instanceof Duration) {
            final Duration other = (Duration) o;
            result = (getMilliSeconds() == other.getMilliSeconds())
                && (getNanoSeconds() == other.getNanoSeconds());
        } else {
            result = false;
        }
        return result;
    }

    @Override
    public String toString() {
        return toPattern();
    }

    @Nonnegative
    protected static long oneUncheckedIntervalToMilliSeconds(@Nonnull String interval) {
        final String trimmedInterval = interval.trim();
        return trimmedInterval.isEmpty() ? 0 : oneIntervalToMilliSeconds(interval);
    }

    @Nonnegative
    protected static long oneUncheckedIntervalToNanoSeconds(@Nonnull String interval) {
        final String trimmedInterval = interval.trim();
        return trimmedInterval.isEmpty() ? 0 : oneIntervalToNanoSeconds(interval);
    }

    @Nonnegative
    protected static long oneIntervalToMilliSeconds(@Nonnull String interval) {
        long value;
        try {
            value = valueOf(interval);
        } catch (final NumberFormatException ignored) {
            if (interval.length() >= 2) {
                final long plainValue;
                try {
                    plainValue = valueOf(interval.substring(0, interval.length() - 1));
                } catch (final NumberFormatException e) {
                    throw new IllegalArgumentException("Don't know how to convert: " + interval, e);
                }
                final String mode = interval.substring(interval.length() - 1);
                if (mode.equals("S")) {
                    value = plainValue;
                } else if (mode.equals("s")) {
                    value = SECONDS.toMillis(plainValue);
                } else if (mode.equals("m")) {
                    value = MINUTES.toMillis(plainValue);
                } else if (mode.equals("h")) {
                    value = HOURS.toMillis(plainValue);
                } else if (mode.equals("d")) {
                    value = DAYS.toMillis(plainValue);
                } else if (mode.equals("w")) {
                    value = DAYS.toMillis(plainValue) * 7;
                } else {
                    throw new IllegalArgumentException("Don't know how to convert: " + interval);
                }
            } else {
                throw new IllegalArgumentException("Don't know how to convert: " + interval);
            }
        }
        return value;
    }

    @Nonnegative
    protected static long oneIntervalToNanoSeconds(@Nonnull String interval) {
        long value;
        try {
            value = valueOf(interval);
        } catch (final NumberFormatException ignored) {
            if (interval.length() >= 2) {
                final long plainValue;
                try {
                    plainValue = valueOf(interval.substring(0, interval.length() - 1));
                } catch (final NumberFormatException e) {
                    throw new IllegalArgumentException("Don't know how to convert: " + interval, e);
                }
                final String mode = interval.substring(interval.length() - 1);
                if (mode.equals("n")) {
                    value = plainValue;
                } else if (mode.equals("µ")) {
                    value = MICROSECONDS.toNanos(plainValue);
                } else {
                    throw new IllegalArgumentException("Don't know how to convert: " + interval);
                }
            } else {
                throw new IllegalArgumentException("Don't know how to convert: " + interval);
            }
        }
        return value;
    }

    @Nonnegative
    protected static MilliAndNanoSeconds parsePattern(@Nonnull String pattern) throws IllegalArgumentException {
        StringBuilder sb = new StringBuilder();
        final char[] chars = pattern.replace("ms", "S").replace("µs", "µ").replace("ns", "n").toCharArray();
        long milli = 0;
        long nano = 0;
        for (final char c : chars) {
            if (isWhitespace(c)) {
                milli += oneUncheckedIntervalToMilliSeconds(sb.toString());
                sb = new StringBuilder();
            } else if (isDigit(c)) {
                sb.append(c);
            } else if (c == 'w' || c == 'd' || c == 'h' || c == 'm' || c == 's' || c == 'S') {
                sb.append(c);
                milli += oneUncheckedIntervalToMilliSeconds(sb.toString());
                sb = new StringBuilder();
            } else if (c == '\u00B5' || c == 'n') {
                sb.append(c);
                nano += oneUncheckedIntervalToNanoSeconds(sb.toString());
                sb = new StringBuilder();
            } else {
                throw new IllegalArgumentException("Don't know how to convert: " + pattern);
            }
        }
        milli += oneUncheckedIntervalToMilliSeconds(sb.toString());
        final long toMuchMillis = nano / 1000000L;
        milli += toMuchMillis;
        nano -= toMuchMillis * 1000000L;
        return new MilliAndNanoSeconds(milli, (int) nano);
    }

    protected static class MilliAndNanoSeconds {

        @Nonnegative
        private final long _milliSeconds;
        @Nonnegative
        private final int _nanoSeconds;

        public MilliAndNanoSeconds(@Nonnegative long milliSeconds, @Nonnegative int nanoSeconds) {
            _milliSeconds = milliSeconds;
            _nanoSeconds = nanoSeconds;
        }

        public MilliAndNanoSeconds(@Nonnegative long duration, @Nonnull TimeUnit unit) {
            if (unit == NANOSECONDS || unit == MICROSECONDS) {
                final long fullNanoSeconds = unit.toNanos(duration);
                _milliSeconds = fullNanoSeconds / 1000000L;
                _nanoSeconds = (int) (fullNanoSeconds - (_milliSeconds * 1000000L));
            } else {
                _milliSeconds = unit.toMillis(duration);
                _nanoSeconds = 0;
            }
        }

        @Nonnegative
        public long getMilliSeconds() {
            return _milliSeconds;
        }

        @Nonnegative
        public int getNanoSeconds() {
            return _nanoSeconds;
        }
    }

    @Nonnull
    protected static String toPattern(@Nonnegative long milliseconds, @Nonnegative int nanoSeconds) {
        final StringBuilder sb = new StringBuilder();
        if (milliseconds > 0) {
            appendPatternOf(milliseconds, sb);
        }
        if (nanoSeconds > 0) {
            appendPatternOf(nanoSeconds, sb);
        }
        if (sb.length() == 0) {
            sb.append("0ms");
        }
        return sb.toString();
    }

    protected static void appendPatternOf(@Nonnegative long milliseconds, @Nonnull StringBuilder to) {
        final long days = milliseconds / 1000 / 60 / 60 / 24;
        final long hours = (milliseconds / 1000 / 60 / 60) - (days * 24);
        final long minutes = (milliseconds / 1000 / 60) - (days * 24 * 60) - (hours * 60);
        final long seconds = (milliseconds / 1000) - (minutes * 60) - (hours * 60 * 60) - (days * 24 * 60 * 60);
        final long ms = milliseconds - (seconds * 1000) - (minutes * 60 * 1000) - (hours * 1000 * 60 * 60) - (days * 24 * 60 * 60 * 1000);
        if (days > 0) {
            addElement(to, " ", days + "d");
        }
        if (hours > 0) {
            addElement(to, " ", hours + "h");
        }
        if (minutes > 0) {
            addElement(to, " ", minutes + "m");
        }
        if (seconds > 0) {
            addElement(to, " ", seconds + "s");
        }
        if (ms > 0) {
            addElement(to, " ", ms + "ms");
        }
    }

    protected static void appendPatternOf(@Nonnegative int nanoSeconds, @Nonnull StringBuilder to) {
        final int µs = nanoSeconds / 1000;
        final int ns = nanoSeconds - (µs * 1000);
        if (µs > 0) {
            addElement(to, " ", µs + "µs");
        }
        if (ns > 0) {
            addElement(to, " ", ns + "ns");
        }
    }

    @Nonnull
    protected static Map<TimeUnit, Long> toUnitToValue(@Nonnegative long milliSeconds, @Nonnegative int nanoSeconds) {
        final Map<TimeUnit, Long> result = new LinkedHashMap<>();
        appendUnitToValueOf(milliSeconds, result);
        appendUnitToValueOf(nanoSeconds, result);
        return unmodifiableMap(result);
    }

    protected static void appendUnitToValueOf(@Nonnegative long milliSeconds, @Nonnull Map<TimeUnit, Long> to) {
        final long days = milliSeconds / 1000 / 60 / 60 / 24;
        final long hours = (milliSeconds / 1000 / 60 / 60) - (days * 24);
        final long minutes = (milliSeconds / 1000 / 60) - (days * 24 * 60) - (hours * 60);
        final long seconds = (milliSeconds / 1000) - (minutes * 60) - (hours * 60 * 60) - (days * 24 * 60 * 60);
        final long ms = milliSeconds - (seconds * 1000) - (minutes * 60 * 1000) - (hours * 1000 * 60 * 60) - (days * 24 * 60 * 60 * 1000);
        if (days > 0) {
            to.put(DAYS, days);
        }
        if (hours > 0) {
            to.put(HOURS, hours);
        }
        if (minutes > 0) {
            to.put(MINUTES, minutes);
        }
        if (seconds > 0) {
            to.put(SECONDS, seconds);
        }
        if (ms > 0) {
            to.put(MILLISECONDS, ms);
        }
    }

    protected static void appendUnitToValueOf(@Nonnegative int nanoSeconds, @Nonnull Map<TimeUnit, Long> to) {
        final long µs = nanoSeconds / 1000;
        final long ns = nanoSeconds - (µs * 1000);
        if (µs > 0) {
            to.put(MICROSECONDS, µs);
        }
        if (ns > 0) {
            to.put(NANOSECONDS, ns);
        }
    }

    public static class Adapter extends XmlAdapter<String, Duration> {

        @Override
        public Duration unmarshal(String v) throws Exception {
            return v != null ? new Duration(v) : null;
        }

        @Override
        public String marshal(Duration v) throws Exception {
            return v != null ? v.toPattern() : null;
        }

    }
}

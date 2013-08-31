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
import static java.lang.Thread.interrupted;
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
 *     <code>new Duration("1ms").toMilliSeconds() == 1</code><br/>
 *     <code>new Duration("1s").toMilliSeconds() == 1,000</code><br/>
 *     <code>new Duration("1m").toMilliSeconds() == 60,000</code><br/>
 *     <code>new Duration("1m10s").toMilliSeconds() == 70,000</code><br/>
 *     <code>new Duration("1m 10s").toMilliSeconds() == 70,000</code><br/>
 *     <code>new Duration("15").toMilliSeconds() == 15</code><br/>
 *     <code>new Duration("2s 15").toMilliSeconds() == 2,015</code><br/>
 * </p>
 *
 */
@Immutable
@ThreadSafe
@XmlJavaTypeAdapter(Adapter.class)
public class Duration implements Comparable<Duration>, Serializable {

    private static final long serialVersionUID = 3L;

    public static void sleep(@Nonnull String duration) throws InterruptedException {
        sleep(new Duration(duration));
    }

    public static void sleep(@Nonnull Duration duration) throws InterruptedException {
        sleep(duration.toMilliSeconds());
    }

    public static void sleep(@Nonnegative long amount, @Nonnull TimeUnit unit) throws InterruptedException {
        sleep(unit.toMillis(amount));
    }

    public static void sleep(@Nonnegative long milliSeconds) throws InterruptedException {
        if (milliSeconds > 0) {
            Thread.sleep(milliSeconds);
        }
    }

    public static void sleepSafe(@Nonnull String duration) throws GotInterruptedException {
        sleepSafe(new Duration(duration));
    }

    public static void sleepSafe(@Nonnull Duration duration) throws GotInterruptedException {
        sleepSafe(duration.toMilliSeconds());
    }

    public static void sleepSafe(@Nonnegative long amount, @Nonnull TimeUnit unit) throws GotInterruptedException {
        sleepSafe(unit.toMillis(amount));
    }

    public static void sleepSafe(@Nonnegative long milliSeconds) throws GotInterruptedException {
        try {
            sleep(milliSeconds);
        } catch (InterruptedException e) {
            interrupted();
            throw new GotInterruptedException(e);
        }
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
    public static Duration duration(@Nonnegative long duration) {
        return new Duration(duration);
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

    /**
     * Returns a new <code>Duration</code> object for the time
     * between {@link System#currentTimeMillis() now} and the
     * given <code>startTime</code>.
     *
     * @param startTime the return value of a previous call to {@link System#currentTimeMillis()}
     */
    public static Duration since(long startTime) {
        final long now = System.currentTimeMillis();
        return new Duration(now - startTime);
    }

    private final long _milliSeconds;

    public Duration(@Nonnegative long milliSeconds) {
        _milliSeconds = milliSeconds;
    }

    public Duration(@Nonnegative long duration, TimeUnit durationUnit) {
        _milliSeconds = durationUnit.toMillis(duration);
    }

    /**
     * @param plain See {@link Duration}
     */
    public Duration(@Nonnull String plain) throws IllegalArgumentException {
        _milliSeconds = parsePattern(plain);
    }

    public Duration(@Nonnull Date from, @Nonnull Date to) {
        if (from.after(to)) {
            throw new IllegalArgumentException("From " + from + " is after to " + to + "?");
        }
        _milliSeconds = to.getTime() - from.getTime();
    }

    @Nonnull
    public Duration plus(@Nullable String duration) {
        return plus(duration != null ? new Duration(duration) : null);
    }

    @Nonnull
    public Duration plus(@Nullable Duration duration) {
        return plus(duration != null ? duration.toMilliSeconds() : 0);
    }

    @Nonnull
    public Duration plus(@Nonnegative long amount, @Nonnull TimeUnit unit) {
        return plus(unit.toMillis(amount));
    }

    @Nonnull
    public Duration plus(@Nonnegative long milliSeconds) {
        return new Duration(toMilliSeconds() + milliSeconds);
    }

    @Nonnull
    public Duration minus(@Nullable String duration) {
        return minus(duration != null ? new Duration(duration) : null);
    }

    @Nonnull
    public Duration minus(@Nullable Duration duration) {
        return minus(duration != null ? duration.toMilliSeconds() : 0);
    }

    @Nonnull
    public Duration minus(@Nonnegative long amount, @Nonnull TimeUnit unit) {
        return minus(unit.toMillis(amount));
    }

    @Nonnull
    public Duration minus(@Nonnegative long milliSeconds) {
        return plus(milliSeconds * -1);
    }

    @Nonnull
    public Duration multiplyBy(double what) {
        return new Duration(Math.round(_milliSeconds * what));
    }

    @Nonnull
    public Duration dividedBy(double what) {
        return new Duration(Math.round(_milliSeconds / what));
    }

    @Nonnull
    public Duration multiplyBy(long what) {
        return new Duration(Math.round(_milliSeconds * what));
    }

    @Nonnull
    public Duration dividedBy(long what) {
        return new Duration(Math.round(_milliSeconds / what));
    }

    /**
     * @return the duration in milli seconds.
     */
    @Nonnegative
    public long toMilliSeconds() {
        return _milliSeconds;
    }

    @Nonnegative
    public long in(@Nonnull TimeUnit timeUnit) {
        return timeUnit.convert(toMilliSeconds(), MILLISECONDS);
    }

    /**
     * @return See {@link Duration pattern}.
     */
    @Nonnull
    public String toPattern() {
        return toPattern(toMilliSeconds());
    }

    @Nonnull
    public Map<TimeUnit, Long> toUnitToValue() {
        return toUnitToValue(toMilliSeconds());
    }

    public boolean isEmpty() {
        return toMilliSeconds() <= 0;
    }

    public boolean hasContent() {
        return toMilliSeconds() > 0;
    }

    public boolean isLessThan(@Nullable String other) {
        return isLessThan(other != null ? new Duration(other) : null);
    }

    public boolean isLessThan(@Nullable Duration other) {
        return isLessThan(other != null ? other.toMilliSeconds() : 0);
    }

    public boolean isLessThan(@Nonnegative long amount, @Nonnull TimeUnit unit) {
        return isLessThan(unit.toMillis(amount));
    }

    public boolean isLessThan(@Nonnegative long milliSeconds) {
        return toMilliSeconds() < milliSeconds;
    }

    public boolean isLessThanOrEqualTo(@Nullable String other) {
        return isLessThanOrEqualTo(other != null ? new Duration(other) : null);
    }

    public boolean isLessThanOrEqualTo(@Nullable Duration other) {
        return isLessThanOrEqualTo(other != null ? other.toMilliSeconds() : 0);
    }

    public boolean isLessThanOrEqualTo(@Nonnegative long amount, @Nonnull TimeUnit unit) {
        return isLessThanOrEqualTo(unit.toMillis(amount));
    }

    public boolean isLessThanOrEqualTo(@Nonnegative long milliSeconds) {
        return toMilliSeconds() <= milliSeconds;
    }

    public boolean isGreaterThan(@Nullable String other) {
        return isGreaterThan(other != null ? new Duration(other) : null);
    }

    public boolean isGreaterThan(@Nullable Duration other) {
        return isGreaterThan(other != null ? other.toMilliSeconds() : 0);
    }

    public boolean isGreaterThan(@Nonnegative long amount, @Nonnull TimeUnit unit) {
        return isGreaterThan(unit.toMillis(amount));
    }

    public boolean isGreaterThan(@Nonnegative long milliSeconds) {
        return toMilliSeconds() > milliSeconds;
    }

    public boolean isGreaterThanOrEqualTo(@Nullable String other) {
        return isGreaterThanOrEqualTo(other != null ? new Duration(other) : null);
    }

    public boolean isGreaterThanOrEqualTo(@Nullable Duration other) {
        return isGreaterThanOrEqualTo(other != null ? other.toMilliSeconds() : 0);
    }

    public boolean isGreaterThanOrEqualTo(@Nonnegative long amount, @Nonnull TimeUnit unit) {
        return isGreaterThanOrEqualTo(unit.toMillis(amount));
    }

    public boolean isGreaterThanOrEqualTo(@Nonnegative long milliSeconds) {
        return toMilliSeconds() >= milliSeconds;
    }

    public void sleep() throws InterruptedException {
        sleep(this);
    }

    public void sleepUnchecked() throws GotInterruptedException {
        sleepSafe(this);
    }

    @Override
    public int compareTo(Duration other) {
        final int result = compare(toMilliSeconds(), other != null ? other.toMilliSeconds() : 0);
        return result;
    }

    private static int compare(long self, long other) {
        // noinspection NestedConditionalExpression
        final int result = ((self < other) ? -1 : ((self == other) ? 0 : 1));
        return result;
    }

    @Override
    public int hashCode() {
        final long milliSeconds = toMilliSeconds();
        return (int) (milliSeconds ^ (milliSeconds >>> 32));
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else if (o instanceof Duration) {
            final Duration other = (Duration) o;
            result = (toMilliSeconds() == other.toMilliSeconds());
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
    protected static long oneIntervalToMilliSeconds(@Nonnull String interval) {
        long value;
        try {
            value = valueOf(interval);
        } catch (NumberFormatException ignored) {
            if (interval.length() >= 2) {
                final long plainValue;
                try {
                    plainValue = valueOf(interval.substring(0, interval.length() - 1));
                } catch (NumberFormatException e) {
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
    protected static long parsePattern(@Nonnull String pattern) throws IllegalArgumentException {
        StringBuilder sb = new StringBuilder();
        final char[] chars = pattern.replace("ms", "S").toCharArray();
        long result = 0;
        for (char c : chars) {
            if (isWhitespace(c)) {
                result += oneUncheckedIntervalToMilliSeconds(sb.toString());
                sb = new StringBuilder();
            } else if (isDigit(c)) {
                sb.append(c);
            } else if (c == 'w' || c == 'd' || c == 'h' || c == 'm' || c == 's' || c == 'S') {
                sb.append(c);
                result += oneUncheckedIntervalToMilliSeconds(sb.toString());
                sb = new StringBuilder();
            } else {
                throw new IllegalArgumentException("Don't know how to convert: " + pattern);
            }
        }
        return result;
    }

    @Nonnull
    protected static String toPattern(@Nonnegative long milliseconds) {
        final long days = milliseconds / 1000 / 60 / 60 / 24;
        final long hours = (milliseconds / 1000 / 60 / 60) - (days * 24);
        final long minutes = (milliseconds / 1000 / 60) - (days * 24 * 60) - (hours * 60);
        final long seconds = (milliseconds / 1000) - (minutes * 60) - (hours * 60 * 60) - (days * 24 * 60 * 60);
        final long ms = milliseconds - (seconds * 1000) - (minutes * 60 * 1000) - (hours * 1000 * 60 * 60) - (days * 24 * 60 * 60 * 1000);
        final StringBuilder sb = new StringBuilder();
        if (days > 0) {
            addElement(sb, " ", days + "d");
        }
        if (hours > 0) {
            addElement(sb, " ", hours + "h");
        }
        if (minutes > 0) {
            addElement(sb, " ", minutes + "m");
        }
        if (seconds > 0) {
            addElement(sb, " ", seconds + "s");
        }
        if (ms > 0) {
            addElement(sb, " ", ms + "ms");
        }
        if (sb.length() == 0) {
            sb.append("0ms");
        }
        return sb.toString();
    }

    @Nonnull
    protected static Map<TimeUnit, Long> toUnitToValue(@Nonnegative long milliseconds) {
        final long days = milliseconds / 1000 / 60 / 60 / 24;
        final long hours = (milliseconds / 1000 / 60 / 60) - (days * 24);
        final long minutes = (milliseconds / 1000 / 60) - (days * 24 * 60) - (hours * 60);
        final long seconds = (milliseconds / 1000) - (minutes * 60) - (hours * 60 * 60) - (days * 24 * 60 * 60);
        final long ms = milliseconds - (seconds * 1000) - (minutes * 60 * 1000) - (hours * 1000 * 60 * 60) - (days * 24 * 60 * 60 * 1000);
        final Map<TimeUnit, Long> result = new LinkedHashMap<>(5, 1.0f);
        if (days > 0) {
            result.put(DAYS, days);
        }
        if (hours > 0) {
            result.put(HOURS, hours);
        }
        if (minutes > 0) {
            result.put(MINUTES, minutes);
        }
        if (seconds > 0) {
            result.put(SECONDS, seconds);
        }
        if (ms > 0) {
            result.put(MILLISECONDS, ms);
        }
        return unmodifiableMap(result);
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

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

package org.echocat.jomon.runtime.util;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Locale.US;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;
import static org.echocat.jomon.runtime.util.ByteUnit.BYTE;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@ThreadSafe
@Immutable
public class ByteCount implements Comparable<ByteCount>, Serializable {

    public static final int DEFAULT_FORMAT_PRECISION = 2;

    private static final ByteUnit[] BYTE_UNITS = ByteUnit.values();
    private static final Pattern SPLIT_PATTERN = createSplitPattern();

    private static final long serialVersionUID = 1L;

    @Nonnegative
    public static long toByteCount(@Nonnull String combinedByteCount) throws IllegalArgumentException {
        long result = 0;
        if (!combinedByteCount.trim().equals("0")) {
            final Matcher matcher = SPLIT_PATTERN.matcher(combinedByteCount);
            int lastEnd = 0;
            while (matcher.find()) {
                if (matcher.start() != lastEnd) {
                    throw new IllegalArgumentException("Could not parse: " + combinedByteCount);
                }
                lastEnd = matcher.end();
                result += parsePartValueOf(matcher);
            }
            if (lastEnd != combinedByteCount.length()) {
                throw new IllegalArgumentException("Could not parse: " + combinedByteCount);
            }
        }

        return result;
    }

    @Nonnegative
    protected static long parsePartValueOf(@Nonnull Matcher matcher) {
        Long partValue = null;
        for (int i = 0; i < BYTE_UNITS.length; i++) {
            final String group = matcher.group(i + 1);
            if (!isEmpty(group)) {
                partValue = BYTE_UNITS[i].toBytes(Long.parseLong(group));
            }
        }
        if (partValue == null || partValue < 0) {
            throw new IllegalArgumentException("Could not parse part: " + matcher.group());
        }
        return partValue;
    }

    @Nonnull
    public static String toCombinedByteCount(@Nonnegative long byteCount) {
        final StringBuilder sb = new StringBuilder();
        long rest = byteCount;
        for (int i=BYTE_UNITS.length - 1; i >= 0; i--) {
            final ByteUnit unit = BYTE_UNITS[i];
            final long value = unit.convert(rest, BYTE);
            if (value > 0) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(value).append(unit.getDisplay());
                rest = rest - unit.toBytes(value);
            }
        }
        if (sb.length() == 0) {
            sb.append('0');
        }
        return sb.toString();
    }

    @Nonnull
    public static ByteCount byteCount(@Nonnull ByteCount byteCount) {
        return byteCount;
    }

    @Nullable
    public static ByteCount byteCount(@Nullable String byteCount) {
        return byteCount != null ? new ByteCount(byteCount) : null;
    }

    @Nullable
    public static ByteCount byteCount(@Nonnegative long byteCount) {
        return new ByteCount(byteCount);
    }

    @Nonnull
    public static ByteCount byteCountOf(@Nonnull ByteCount byteCount) {
        return byteCount(byteCount);
    }

    @Nullable
    public static ByteCount byteCountOf(@Nullable String byteCount) {
        return byteCount(byteCount);
    }

    @Nullable
    public static ByteCount byteCountOf(@Nonnegative long byteCount) {
        return byteCount(byteCount);
    }

    private final long _count;

    public ByteCount(@Nonnegative long byteCount) {
        _count = byteCount;
    }

    public ByteCount(@Nonnegative long count, @Nonnull ByteUnit byteUnit) {
        _count = byteUnit.toBytes(count);
    }

    public ByteCount(@Nonnull String combinedByteCount) throws IllegalArgumentException {
        _count = toByteCount(combinedByteCount);
    }

    /**
     * @return the count of all bytes.
     */
    @Nonnegative
    public long toByteCount() {
        return _count;
    }

    @Nonnegative
    public long in(@Nonnull ByteUnit byteUnit) {
        return byteUnit.convert(_count, BYTE);
    }

    @Nonnull
    public String toCombinedByteCount() {
        return toCombinedByteCount(_count);
    }

    @Nonnull
    public String toFormattedByteCountOf(@Nonnull ByteUnit unit) {
        return toFormattedByteCountOf(unit, null, DEFAULT_FORMAT_PRECISION);
    }

    @Nonnull
    public String toFormattedByteCountOf(@Nonnull ByteUnit unit, @Nonnegative int precision) {
        return toFormattedByteCountOf(unit, null, precision);
    }

    @Nonnull
    public String toFormattedByteCountOf(@Nonnull ByteUnit unit, @Nullable Locale locale) {
        return toFormattedByteCountOf(unit, locale, DEFAULT_FORMAT_PRECISION);
    }

    @Nonnull
    public String toFormattedByteCountOf(@Nonnull ByteUnit unit, @Nullable Locale locale, @Nonnegative int precision) {
        final double value = unit.convert((double) _count, BYTE);
        final NumberFormat format = DecimalFormat.getNumberInstance(locale != null ? locale : US);
        format.setMaximumFractionDigits(precision);
        return format.format(value) + unit.getDisplay();
    }

    @Nonnull
    public String toFormattedByteCount() {
        return toFormattedByteCount(null, DEFAULT_FORMAT_PRECISION);
    }

    @Nonnull
    public String toFormattedByteCount(@Nonnegative int precision) {
        return toFormattedByteCount(null, precision);
    }

    @Nonnull
    public String toFormattedByteCount(@Nullable Locale locale) {
        return toFormattedByteCount(locale, DEFAULT_FORMAT_PRECISION);
    }

    @Nonnull
    public String toFormattedByteCount(@Nullable Locale locale, @Nonnegative int precision) {
        return toFormattedByteCountOf(getBestFittingUnit(), locale, precision);
    }

    @Nonnull
    public ByteUnit getBestFittingUnit() {
        ByteUnit result = BYTE;
        for (int i=BYTE_UNITS.length - 1; i >= 0; i--) {
            final ByteUnit unit = BYTE_UNITS[i];
            if (unit.convert(_count, BYTE) > 0) {
                result = unit;
                break;
            }
        }
        return result;
    }

    @Nonnull
    public ByteCount plus(long byteCount) {
        return new ByteCount(_count + byteCount);
    }

    @Nonnull
    public ByteCount plus(@Nonnull ByteCount byteCount) {
        return plus(byteCount._count);
    }

    @Nonnull
    public ByteCount plus(long byteCount, @Nonnull ByteUnit unit) {
        final long countToTransform = byteCount >= 0 ? byteCount : (byteCount * -1);
        final long transformedCount = unit.toBytes(countToTransform);
        final long correctedCount = byteCount >= 0 ? transformedCount : (transformedCount * -1);
        return plus(correctedCount);
    }

    @Nonnull
    public ByteCount minus(@Nonnull ByteCount byteCount) {
        return minus(byteCount._count);
    }

    @Nonnull
    public ByteCount minus(long byteCount) {
        return new ByteCount(_count - byteCount);
    }

    @Nonnull
    public ByteCount minus(long byteCount, @Nonnull ByteUnit unit) {
        final long countToTransform = byteCount >= 0 ? byteCount : (byteCount * -1);
        final long transformedCount = unit.toBytes(countToTransform);
        final long correctedCount = byteCount >= 0 ? transformedCount : (transformedCount * -1);
        return minus(correctedCount);
    }

    @Nonnegative
    public double getProcessInRelationTo(@Nonnull ByteCount current) {
        return ((double) current._count) / ((double) _count);
    }

    public boolean isLessThan(@Nonnull ByteCount other) {
        return toByteCount() < other.toByteCount();
    }

    public boolean isLessThanOrEqualTo(@Nonnull ByteCount other) {
        return toByteCount() <= other.toByteCount();
    }

    public boolean isGreaterThan(@Nonnull ByteCount other) {
        return toByteCount() > other.toByteCount();
    }

    public boolean isGreaterThanOrEqualTo(@Nonnull ByteCount other) {
        return toByteCount() >= other.toByteCount();
    }

    @Override
    public int compareTo(@Nonnull ByteCount other) {
        final int result = compare(toByteCount(), other.toByteCount());
        return result;
    }

    private static int compare(long self, long other) {
        // noinspection NestedConditionalExpression
        final int result = ((self < other) ? -1 : ((self == other) ? 0 : 1));
        return result;
    }

    @Override
    public int hashCode() {
        return (int) (_count ^ (_count >>> 32));
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else if (o instanceof ByteCount) {
            final ByteCount other = (ByteCount) o;
            result = (toByteCount() == other.toByteCount());
        } else {
            result = false;
        }
        return result;
    }

    @Override
    public String toString() {
        return toCombinedByteCount();
    }

    @Nonnull
    private static Pattern createSplitPattern() {
        final StringBuilder sb = new StringBuilder();
        sb.append("\\s*(?:");
        boolean first = true;
        for (ByteUnit byteUnit : BYTE_UNITS) {
            if (first) {
                first = false;
            } else {
                sb.append("|");
            }
            sb.append("(\\d+)\\s*(?:").append(byteUnit.getDisplay()).append('|').append(byteUnit.getShortDisplay()).append(')');
        }
        sb.append(")\\s*");
        return compile(sb.toString(), CASE_INSENSITIVE);
    }

}

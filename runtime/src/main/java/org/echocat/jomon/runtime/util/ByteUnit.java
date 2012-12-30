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

public enum ByteUnit {
    BYTE("B", "B") {
        @Override public long toBytes(@Nonnegative long count) { return count; }
        @Override public long toKiloBytes(@Nonnegative long count) { return toBytes(count) / 1024; }
        @Override public long toMegaBytes(@Nonnegative long count) { return toKiloBytes(count) / 1024; }
        @Override public long toGigaBytes(@Nonnegative long count) { return toMegaBytes(count) / 1024; }
        @Override public long toTeraBytes(@Nonnegative long count) { return toGigaBytes(count) / 1024; }
        @Override public long toPetaBytes(@Nonnegative long count) { return toTeraBytes(count) / 1024; }
        @Override public long toExaBytes(@Nonnegative long count) { return toPetaBytes(count) / 1024; }
        @Override public long convert(@Nonnegative long count, @Nonnull ByteUnit sourceUnit) { return sourceUnit.toBytes(count); }

        @Override public double toBytes(@Nonnegative double count) { return count; }
        @Override public double toKiloBytes(@Nonnegative double count) { return toBytes(count) / 1024.0; }
        @Override public double toMegaBytes(@Nonnegative double count) { return toKiloBytes(count) / 1024.0; }
        @Override public double toGigaBytes(@Nonnegative double count) { return toMegaBytes(count) / 1024.0; }
        @Override public double toTeraBytes(@Nonnegative double count) { return toGigaBytes(count) / 1024.0; }
        @Override public double toPetaBytes(@Nonnegative double count) { return toTeraBytes(count) / 1024.0; }
        @Override public double toExaBytes(@Nonnegative double count) { return toPetaBytes(count) / 1024.0; }
        @Override public double convert(@Nonnegative double count, @Nonnull ByteUnit sourceUnit) { return sourceUnit.toBytes(count); }
    },
    KILO_BYTE("kB", "k") {
        @Override public long toBytes(@Nonnegative long count) { return toKiloBytes(count) * 1024; }
        @Override public long toKiloBytes(@Nonnegative long count) { return count; }
        @Override public long toMegaBytes(@Nonnegative long count) { return toKiloBytes(count) / 1024; }
        @Override public long toGigaBytes(@Nonnegative long count) { return toMegaBytes(count) / 1024; }
        @Override public long toTeraBytes(@Nonnegative long count) { return toGigaBytes(count) / 1024; }
        @Override public long toPetaBytes(@Nonnegative long count) { return toTeraBytes(count) / 1024; }
        @Override public long toExaBytes(@Nonnegative long count) { return toPetaBytes(count) / 1024; }
        @Override public long convert(@Nonnegative long count, @Nonnull ByteUnit sourceUnit) { return sourceUnit.toKiloBytes(count); }

        @Override public double toBytes(@Nonnegative double count) { return toKiloBytes(count) * 1024.0; }
        @Override public double toKiloBytes(@Nonnegative double count) { return count; }
        @Override public double toMegaBytes(@Nonnegative double count) { return toKiloBytes(count) / 1024.0; }
        @Override public double toGigaBytes(@Nonnegative double count) { return toMegaBytes(count) / 1024.0; }
        @Override public double toTeraBytes(@Nonnegative double count) { return toGigaBytes(count) / 1024.0; }
        @Override public double toPetaBytes(@Nonnegative double count) { return toTeraBytes(count) / 1024.0; }
        @Override public double toExaBytes(@Nonnegative double count) { return toPetaBytes(count) / 1024.0; }
        @Override public double convert(@Nonnegative double count, @Nonnull ByteUnit sourceUnit) { return sourceUnit.toKiloBytes(count); }
    },
    MEGA_BYTE("MB", "M") {
        @Override public long toBytes(@Nonnegative long count) { return toKiloBytes(count) * 1024; }
        @Override public long toKiloBytes(@Nonnegative long count) { return toMegaBytes(count) * 1024; }
        @Override public long toMegaBytes(@Nonnegative long count) { return count; }
        @Override public long toGigaBytes(@Nonnegative long count) { return toMegaBytes(count) / 1024; }
        @Override public long toTeraBytes(@Nonnegative long count) { return toGigaBytes(count) / 1024; }
        @Override public long toPetaBytes(@Nonnegative long count) { return toTeraBytes(count) / 1024; }
        @Override public long toExaBytes(@Nonnegative long count) { return toPetaBytes(count) / 1024; }
        @Override public long convert(@Nonnegative long count, @Nonnull ByteUnit sourceUnit) { return sourceUnit.toMegaBytes(count); }

        @Override public double toBytes(@Nonnegative double count) { return toKiloBytes(count) * 1024.0; }
        @Override public double toKiloBytes(@Nonnegative double count) { return toMegaBytes(count) * 1024.0; }
        @Override public double toMegaBytes(@Nonnegative double count) { return count; }
        @Override public double toGigaBytes(@Nonnegative double count) { return toMegaBytes(count) / 1024.0; }
        @Override public double toTeraBytes(@Nonnegative double count) { return toGigaBytes(count) / 1024.0; }
        @Override public double toPetaBytes(@Nonnegative double count) { return toTeraBytes(count) / 1024.0; }
        @Override public double toExaBytes(@Nonnegative double count) { return toPetaBytes(count) / 1024.0; }
        @Override public double convert(@Nonnegative double count, @Nonnull ByteUnit sourceUnit) { return sourceUnit.toMegaBytes(count); }
    },
    GIGA_BYTE("GB", "G") {
        @Override public long toBytes(@Nonnegative long count) { return toKiloBytes(count) * 1024; }
        @Override public long toKiloBytes(@Nonnegative long count) { return toMegaBytes(count) * 1024; }
        @Override public long toMegaBytes(@Nonnegative long count) { return toGigaBytes(count) * 1024; }
        @Override public long toGigaBytes(@Nonnegative long count) { return count; }
        @Override public long toTeraBytes(@Nonnegative long count) { return toGigaBytes(count) / 1024; }
        @Override public long toPetaBytes(@Nonnegative long count) { return toTeraBytes(count) / 1024; }
        @Override public long toExaBytes(@Nonnegative long count) { return toPetaBytes(count) / 1024; }
        @Override public long convert(@Nonnegative long count, @Nonnull ByteUnit sourceUnit) { return sourceUnit.toGigaBytes(count); }

        @Override public double toBytes(@Nonnegative double count) { return toKiloBytes(count) * 1024.0; }
        @Override public double toKiloBytes(@Nonnegative double count) { return toMegaBytes(count) * 1024.0; }
        @Override public double toMegaBytes(@Nonnegative double count) { return toGigaBytes(count) * 1024.0; }
        @Override public double toGigaBytes(@Nonnegative double count) { return count; }
        @Override public double toTeraBytes(@Nonnegative double count) { return toGigaBytes(count) / 1024.0; }
        @Override public double toPetaBytes(@Nonnegative double count) { return toTeraBytes(count) / 1024.0; }
        @Override public double toExaBytes(@Nonnegative double count) { return toPetaBytes(count) / 1024.0; }
        @Override public double convert(@Nonnegative double count, @Nonnull ByteUnit sourceUnit) { return sourceUnit.toGigaBytes(count); }
    },
    TERA_BYTE("TB", "T") {
        @Override public long toBytes(@Nonnegative long count) { return toKiloBytes(count) * 1024; }
        @Override public long toKiloBytes(@Nonnegative long count) { return toMegaBytes(count) * 1024; }
        @Override public long toMegaBytes(@Nonnegative long count) { return toGigaBytes(count) * 1024; }
        @Override public long toGigaBytes(@Nonnegative long count) { return toTeraBytes(count) * 1024; }
        @Override public long toTeraBytes(@Nonnegative long count) { return count; }
        @Override public long toPetaBytes(@Nonnegative long count) { return toTeraBytes(count) / 1024; }
        @Override public long toExaBytes(@Nonnegative long count) { return toPetaBytes(count) / 1024; }
        @Override public long convert(@Nonnegative long count, @Nonnull ByteUnit sourceUnit) { return sourceUnit.toTeraBytes(count); }

        @Override public double toBytes(@Nonnegative double count) { return toKiloBytes(count) * 1024.0; }
        @Override public double toKiloBytes(@Nonnegative double count) { return toMegaBytes(count) * 1024.0; }
        @Override public double toMegaBytes(@Nonnegative double count) { return toGigaBytes(count) * 1024.0; }
        @Override public double toGigaBytes(@Nonnegative double count) { return toTeraBytes(count) * 1024.0; }
        @Override public double toTeraBytes(@Nonnegative double count) { return count; }
        @Override public double toPetaBytes(@Nonnegative double count) { return toTeraBytes(count) / 1024.0; }
        @Override public double toExaBytes(@Nonnegative double count) { return toPetaBytes(count) / 1024.0; }
        @Override public double convert(@Nonnegative double count, @Nonnull ByteUnit sourceUnit) { return sourceUnit.toTeraBytes(count); }
    },
    PETA_BYTE("PB", "P") {
        @Override public long toBytes(@Nonnegative long count) { return toKiloBytes(count) * 1024; }
        @Override public long toKiloBytes(@Nonnegative long count) { return toMegaBytes(count) * 1024; }
        @Override public long toMegaBytes(@Nonnegative long count) { return toGigaBytes(count) * 1024; }
        @Override public long toGigaBytes(@Nonnegative long count) { return toTeraBytes(count) * 1024; }
        @Override public long toTeraBytes(@Nonnegative long count) { return toPetaBytes(count) * 1024; }
        @Override public long toPetaBytes(@Nonnegative long count) { return count; }
        @Override public long toExaBytes(@Nonnegative long count) { return toPetaBytes(count) / 1024; }
        @Override public long convert(@Nonnegative long count, @Nonnull ByteUnit sourceUnit) { return sourceUnit.toPetaBytes(count); }

        @Override public double toBytes(@Nonnegative double count) { return toKiloBytes(count) * 1024.0; }
        @Override public double toKiloBytes(@Nonnegative double count) { return toMegaBytes(count) * 1024.0; }
        @Override public double toMegaBytes(@Nonnegative double count) { return toGigaBytes(count) * 1024.0; }
        @Override public double toGigaBytes(@Nonnegative double count) { return toTeraBytes(count) * 1024.0; }
        @Override public double toTeraBytes(@Nonnegative double count) { return toPetaBytes(count) * 1024.0; }
        @Override public double toPetaBytes(@Nonnegative double count) { return count; }
        @Override public double toExaBytes(@Nonnegative double count) { return toPetaBytes(count) / 1024.0; }
        @Override public double convert(@Nonnegative double count, @Nonnull ByteUnit sourceUnit) { return sourceUnit.toPetaBytes(count); }
    },
    EXA_BYTE("EB", "E") {
        @Override public long toBytes(@Nonnegative long count) { return toKiloBytes(count) * 1024; }
        @Override public long toKiloBytes(@Nonnegative long count) { return toMegaBytes(count) * 1024; }
        @Override public long toMegaBytes(@Nonnegative long count) { return toGigaBytes(count) * 1024; }
        @Override public long toGigaBytes(@Nonnegative long count) { return toTeraBytes(count) * 1024; }
        @Override public long toTeraBytes(@Nonnegative long count) { return toPetaBytes(count) * 1024; }
        @Override public long toPetaBytes(@Nonnegative long count) { return toExaBytes(count) * 1024; }
        @Override public long toExaBytes(@Nonnegative long count) { return count; }
        @Override public long convert(@Nonnegative long count, @Nonnull ByteUnit sourceUnit) { return sourceUnit.toExaBytes(count); }

        @Override public double toBytes(@Nonnegative double count) { return toKiloBytes(count) * 1024.0; }
        @Override public double toKiloBytes(@Nonnegative double count) { return toMegaBytes(count) * 1024.0; }
        @Override public double toMegaBytes(@Nonnegative double count) { return toGigaBytes(count) * 1024.0; }
        @Override public double toGigaBytes(@Nonnegative double count) { return toTeraBytes(count) * 1024.0; }
        @Override public double toTeraBytes(@Nonnegative double count) { return toPetaBytes(count) * 1024.0; }
        @Override public double toPetaBytes(@Nonnegative double count) { return toExaBytes(count) * 1024.0; }
        @Override public double toExaBytes(@Nonnegative double count) { return count; }
        @Override public double convert(@Nonnegative double count, @Nonnull ByteUnit sourceUnit) { return sourceUnit.toExaBytes(count); }
    };

    @Nonnull
    public static ByteUnit getByDisplay(@Nonnull String display) throws IllegalArgumentException {
        for (ByteUnit unit : values()) {
            if (unit.getDisplay().equals(display) || (unit == BYTE && "".equals(display))) {
                return unit;
            }
        }
        throw new IllegalArgumentException();
    }

    @Nonnull
    public static ByteUnit getByShortDisplay(@Nonnull String shortDisplay) throws IllegalArgumentException {
        for (ByteUnit unit : values()) {
            if (unit.getShortDisplay().equals(shortDisplay) || (unit == BYTE && "".equals(shortDisplay))) {
                return unit;
            }
        }
        throw new IllegalArgumentException();
    }

    private final String _display;
    private final String _shortDisplay;

    private ByteUnit(@Nonnull String display, @Nonnull String shortDisplay) {
        _display = display;
        _shortDisplay = shortDisplay;
    }

    @Nonnegative public abstract long convert(@Nonnegative long count, @Nonnull ByteUnit sourceUnit);
    @Nonnegative public abstract double convert(@Nonnegative double count, @Nonnull ByteUnit sourceUnit);

    @Nonnegative public abstract long toBytes(@Nonnegative long count);
    @Nonnegative public abstract long toKiloBytes(@Nonnegative long count);
    @Nonnegative public abstract long toMegaBytes(@Nonnegative long count);
    @Nonnegative public abstract long toGigaBytes(@Nonnegative long count);
    @Nonnegative public abstract long toTeraBytes(@Nonnegative long count);
    @Nonnegative public abstract long toPetaBytes(@Nonnegative long count);
    @Nonnegative public abstract long toExaBytes(@Nonnegative long count);

    @Nonnegative public abstract double toBytes(@Nonnegative double count);
    @Nonnegative public abstract double toKiloBytes(@Nonnegative double count);
    @Nonnegative public abstract double toMegaBytes(@Nonnegative double count);
    @Nonnegative public abstract double toGigaBytes(@Nonnegative double count);
    @Nonnegative public abstract double toTeraBytes(@Nonnegative double count);
    @Nonnegative public abstract double toPetaBytes(@Nonnegative double count);
    @Nonnegative public abstract double toExaBytes(@Nonnegative double count);

    @Nonnull
    public String getDisplay() {
        return _display;
    }

    @Nonnull
    public String getShortDisplay() {
        return _shortDisplay;
    }

    @Override
    public String toString() {
        return _display;
    }
}

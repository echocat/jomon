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

import org.hamcrest.Matcher;
import org.junit.Test;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import static java.lang.StrictMath.pow;
import static org.echocat.jomon.runtime.util.ByteUnit.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ByteUnitUnitTest {

    @Test
    public void testToBytes() throws Exception {
        assertThat(BYTE.toBytes(666), is(666L));
        assertThat(KILO_BYTE.toBytes(666), isMultipliedBy(666, 1));
        assertThat(MEGA_BYTE.toBytes(666), isMultipliedBy(666, 2));
        assertThat(GIGA_BYTE.toBytes(666), isMultipliedBy(666, 3));
        assertThat(TERA_BYTE.toBytes(666), isMultipliedBy(666, 4));
        assertThat(PETA_BYTE.toBytes(666), isMultipliedBy(666, 5));
        assertThat(EXA_BYTE.toBytes(666), isMultipliedBy(666, 6));
    }

    @Test
    public void testToKiloBytes() throws Exception {
        assertThat(BYTE.toKiloBytes(666), isDividedBy(666, 1));
        assertThat(KILO_BYTE.toKiloBytes(666), is(666L));
        assertThat(MEGA_BYTE.toKiloBytes(666), isMultipliedBy(666, 1));
        assertThat(GIGA_BYTE.toKiloBytes(666), isMultipliedBy(666, 2));
        assertThat(TERA_BYTE.toKiloBytes(666), isMultipliedBy(666, 3));
        assertThat(PETA_BYTE.toKiloBytes(666), isMultipliedBy(666, 4));
        assertThat(EXA_BYTE.toKiloBytes(666), isMultipliedBy(666, 5));
    }

    @Test
    public void testToMegaBytes() throws Exception {
        assertThat(BYTE.toMegaBytes(666), isDividedBy(666, 2));
        assertThat(KILO_BYTE.toMegaBytes(666), isDividedBy(666, 1));
        assertThat(MEGA_BYTE.toMegaBytes(666), is(666L));
        assertThat(GIGA_BYTE.toMegaBytes(666), isMultipliedBy(666, 1));
        assertThat(TERA_BYTE.toMegaBytes(666), isMultipliedBy(666, 2));
        assertThat(PETA_BYTE.toMegaBytes(666), isMultipliedBy(666, 3));
        assertThat(EXA_BYTE.toMegaBytes(666), isMultipliedBy(666, 4));
    }

    @Test
    public void testToGigaBytes() throws Exception {
        assertThat(BYTE.toGigaBytes(666), isDividedBy(666, 3));
        assertThat(KILO_BYTE.toGigaBytes(666), isDividedBy(666, 2));
        assertThat(MEGA_BYTE.toGigaBytes(666), isDividedBy(666, 1));
        assertThat(GIGA_BYTE.toGigaBytes(666), is(666L));
        assertThat(TERA_BYTE.toGigaBytes(666), isMultipliedBy(666, 1));
        assertThat(PETA_BYTE.toGigaBytes(666), isMultipliedBy(666, 2));
        assertThat(EXA_BYTE.toGigaBytes(666), isMultipliedBy(666, 3));
    }

    @Test
    public void testToTeraBytes() throws Exception {
        assertThat(BYTE.toTeraBytes(666), isDividedBy(666, 4));
        assertThat(KILO_BYTE.toTeraBytes(666), isDividedBy(666, 3));
        assertThat(MEGA_BYTE.toTeraBytes(666), isDividedBy(666, 2));
        assertThat(GIGA_BYTE.toTeraBytes(666), isDividedBy(666, 1));
        assertThat(TERA_BYTE.toTeraBytes(666), is(666L));
        assertThat(PETA_BYTE.toTeraBytes(666), isMultipliedBy(666, 1));
        assertThat(EXA_BYTE.toTeraBytes(666), isMultipliedBy(666, 2));
    }

    @Test
    public void testToPetaBytes() throws Exception {
        assertThat(BYTE.toPetaBytes(666), isDividedBy(666, 5));
        assertThat(KILO_BYTE.toPetaBytes(666), isDividedBy(666, 4));
        assertThat(MEGA_BYTE.toPetaBytes(666), isDividedBy(666, 3));
        assertThat(GIGA_BYTE.toPetaBytes(666), isDividedBy(666, 2));
        assertThat(TERA_BYTE.toPetaBytes(666), isDividedBy(666, 1));
        assertThat(PETA_BYTE.toPetaBytes(666), is(666L));
        assertThat(EXA_BYTE.toPetaBytes(666), isMultipliedBy(666, 1));
    }

    @Test
    public void testToExaBytes() throws Exception {
        assertThat(BYTE.toExaBytes(666), isDividedBy(666, 6));
        assertThat(KILO_BYTE.toExaBytes(666), isDividedBy(666, 5));
        assertThat(MEGA_BYTE.toExaBytes(666), isDividedBy(666, 4));
        assertThat(GIGA_BYTE.toExaBytes(666), isDividedBy(666, 3));
        assertThat(TERA_BYTE.toExaBytes(666), isDividedBy(666, 2));
        assertThat(PETA_BYTE.toExaBytes(666), isDividedBy(666, 1));
        assertThat(EXA_BYTE.toExaBytes(666), is(666L));
    }

    @Nonnull
    protected static Matcher<Long> isMultipliedBy(@Nonnegative long value, @Nonnegative long step) {
        return is(multipliedBy(value, step));
    }

    @Nonnegative
    protected static long multipliedBy(@Nonnegative long value, @Nonnegative long step) {
        return value * (long)pow(2, step * 10);
    }

    @Nonnull
    protected static Matcher<Long> isDividedBy(@Nonnegative long value, @Nonnegative long step) {
        return is(dividedBy(value, step));
    }

    @Nonnegative
    protected static long dividedBy(@Nonnegative long value, @Nonnegative long step) {
        return value / (long)pow(2, step * 10);
    }
}

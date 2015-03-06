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

import org.junit.Test;

import static org.echocat.jomon.runtime.util.ByteCount.*;
import static org.echocat.jomon.runtime.util.ByteUnit.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ByteCountUnitTest {

    @Test
    public void testToByteCount() throws Exception {
        for (final ByteUnit unit : values()) {
            assertThat(toByteCount("7" + unit.getDisplay()), is(unit.toBytes(7)));
            assertThat(toByteCount("7" + unit.getDisplay().toLowerCase()), is(unit.toBytes(7)));
            assertThat(new ByteCount("7" + unit.getDisplay()), is(new ByteCount(7, unit)));
            assertThat(new ByteCount("7" + unit.getDisplay().toLowerCase()), is(new ByteCount(7, unit)));
        }
        assertThat(toByteCount("66mb 1024kb 2097152b"), is(MEGA_BYTE.toBytes(69)));
        assertThat(new ByteCount("66mb 1024kb 2097152b"), is(new ByteCount(69, MEGA_BYTE)));
        assertThat(toByteCount("0b"), is(MEGA_BYTE.toBytes(0)));
        assertThat(new ByteCount("0b"), is(new ByteCount(0)));
        assertThat(toByteCount("0"), is(MEGA_BYTE.toBytes(0)));
        assertThat(new ByteCount("0"), is(new ByteCount(0)));
        try { toByteCount("1mb 2s"); } catch (final IllegalArgumentException ignored) {}
        try { toByteCount("2v"); } catch (final IllegalArgumentException ignored) {}
    }

    @Test
    public void testToCombinedByteCount() throws Exception {
        for (final ByteUnit unit : values()) {
            assertThat(toCombinedByteCount(unit.toBytes(7)), is("7" + unit.getDisplay()));
            assertThat(new ByteCount(7, unit).toCombinedByteCount(), is("7" + unit.getDisplay()));
        }
        assertThat(toCombinedByteCount(EXA_BYTE.toBytes(5) + MEGA_BYTE.toBytes(100)), is("5EB 100MB"));
        assertThat(toCombinedByteCount(PETA_BYTE.toBytes(666) + GIGA_BYTE.toBytes(666) + TERA_BYTE.toBytes(666) + 1), is("666PB 666TB 666GB 1B"));
        assertThat(toCombinedByteCount(1), is("1B"));
        assertThat(toCombinedByteCount(0), is("0"));
    }
}

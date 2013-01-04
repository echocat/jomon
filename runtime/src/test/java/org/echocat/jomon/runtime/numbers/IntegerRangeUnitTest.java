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

package org.echocat.jomon.runtime.numbers;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class IntegerRangeUnitTest {

    @Test
    public void testIllegal() throws Exception {
        try {
            new IntegerRange(2, 1);
            fail("Expected exception missing");
        } catch (IllegalArgumentException expected) {}
    }

    @Test
    public void testNullNull() throws Exception {
        final IntegerRange range = new IntegerRange(null, null);
        assertThat(range.getFrom(), is((Object)null));
        assertThat(range.getFrom(2), is(2));
        assertThat(range.getTo(), is((Object)null));
        assertThat(range.getTo(667), is(667));

        assertThat(range.apply(-666), is(true));
        assertThat(range.apply(0), is(true));
        assertThat(range.apply(666), is(true));
    }

    @Test
    public void testSetNull() throws Exception {
        final IntegerRange range = new IntegerRange(1, null);
        assertThat(range.getFrom(), is(1));
        assertThat(range.getFrom(2), is(1));
        assertThat(range.getTo(), is((Object)null));
        assertThat(range.getTo(667), is(667));

        assertThat(range.apply(-666), is(false));
        assertThat(range.apply(0), is(false));
        assertThat(range.apply(1), is(true));
        assertThat(range.apply(666), is(true));
    }

    @Test
    public void testNullSet() throws Exception {
        final IntegerRange range = new IntegerRange(null, 666);
        assertThat(range.getFrom(), is((Object) null));
        assertThat(range.getFrom(2), is(2));
        assertThat(range.getTo(), is(666));
        assertThat(range.getTo(667), is(666));

        assertThat(range.apply(-666), is(true));
        assertThat(range.apply(0), is(true));
        assertThat(range.apply(665), is(true));
        assertThat(range.apply(666), is(false));
    }

    @Test
    public void testSetSet() throws Exception {
        final IntegerRange range = new IntegerRange(1, 666);
        assertThat(range.getFrom(), is(1));
        assertThat(range.getFrom(2), is(1));
        assertThat(range.getTo(), is(666));
        assertThat(range.getTo(667), is(666));

        assertThat(range.apply(-666), is(false));
        assertThat(range.apply(0), is(false));
        assertThat(range.apply(1), is(true));
        assertThat(range.apply(665), is(true));
        assertThat(range.apply(666), is(false));
    }

}

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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static java.util.concurrent.TimeUnit.*;

public class DurationUnitTest {

    @Test
    public void testConstruction() {
        assertThat(new Duration("0ms").toMilliSeconds(), is(0L));
        assertThat(new Duration("0s").toMilliSeconds(), is(0L));
        assertThat(new Duration("3S").toMilliSeconds(), is(3L));
        assertThat(new Duration("3ms").toMilliSeconds(), is(3L));
        assertThat(new Duration("3s").toMilliSeconds(), is(3 * 1000L));
        assertThat(new Duration("3m").toMilliSeconds(), is(3 * 60 * 1000L));
        assertThat(new Duration("3h").toMilliSeconds(), is(3 * 60 * 60 * 1000L));
        assertThat(new Duration("3d").toMilliSeconds(), is(3 * 24 * 60 * 60 * 1000L));
    }

    @Test
    public void testIn() {
        final Duration oneHour = new Duration("1h");
        assertThat(oneHour.in(DAYS), is(0L));
        assertThat(oneHour.in(HOURS), is(1L));
        assertThat(oneHour.in(MINUTES), is(60L));
        assertThat(oneHour.in(SECONDS), is(60L * 60L));
        assertThat(oneHour.in(MILLISECONDS), is(60L * 60L * 1000L));
        assertThat(oneHour.in(NANOSECONDS), is(60L * 60L * 1000L * 1000L * 1000L));
    }
}

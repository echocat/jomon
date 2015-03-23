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

package org.echocat.jomon.runtime.concurrent;

import org.echocat.jomon.runtime.util.Duration;
import org.junit.Test;

import static org.echocat.jomon.runtime.concurrent.StopWatch.startStopWatch;
import static org.echocat.jomon.runtime.util.Duration.durationOf;
import static org.echocat.jomon.runtime.util.Duration.sleepSafe;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class StopWatchUnitTest {

    @Test
    public void testGetCurrentDuration() throws Exception {
        final StopWatch stopWatch = startStopWatch();
        sleepSafe("9ms");
        final Duration duration = stopWatch.getCurrentDuration();
        assertThat(duration.toPattern(), duration.isGreaterThanOrEqualTo("8ms"), is(true));
        assertThat(duration.toPattern(), duration.isLessThanOrEqualTo("25ms"), is(true));
    }

    @Test
    public void testResetWithoutPausedBefore() throws Exception {
        final StopWatch stopWatch = startStopWatch();
        sleepSafe("9ms");
        final Duration beforeReset = stopWatch.getCurrentDuration();
        assertThat(beforeReset.toPattern(), beforeReset.isGreaterThanOrEqualTo("8ms"), is(true));
        assertThat(beforeReset.toPattern(), beforeReset.isLessThanOrEqualTo("15ms"), is(true));
        stopWatch.reset();
        final Duration afterReset = stopWatch.getCurrentDuration();
        assertThat(afterReset.toPattern(), afterReset.isGreaterThanOrEqualTo("0ms"), is(true));
        assertThat(afterReset.toPattern(), afterReset.isLessThanOrEqualTo("5ms"), is(true));
    }

    @Test
    public void testResetWithPausedBefore() throws Exception {
        final StopWatch stopWatch = startStopWatch();
        sleepSafe("9ms");
        final Duration beforeReset = stopWatch.getCurrentDuration();
        assertThat(beforeReset.toPattern(), beforeReset.isGreaterThanOrEqualTo("8ms"), is(true));
        assertThat(beforeReset.toPattern(), beforeReset.isLessThanOrEqualTo("15ms"), is(true));
        stopWatch.pause().reset();
        final Duration afterReset = stopWatch.getCurrentDuration();
        assertThat(afterReset.toPattern(), afterReset, is(durationOf(0)));
    }

    @Test
    public void testPause() throws Exception {
        final StopWatch stopWatch = startStopWatch();
        sleepSafe("9ms");
        stopWatch.pause();
        sleepSafe("9ms");
        final Duration duration = stopWatch.getCurrentDuration();
        assertThat(duration.toPattern(), duration.isGreaterThanOrEqualTo("8ms"), is(true));
        assertThat(duration.toPattern(), duration.isLessThanOrEqualTo("15ms"), is(true));
    }

    @Test
    public void testStart() throws Exception {
        final StopWatch stopWatch = startStopWatch();
        sleepSafe("9ms");
        stopWatch.pause();
        sleepSafe("9ms");
        stopWatch.start();
        sleepSafe("9ms");
        final Duration duration = stopWatch.getCurrentDuration();
        assertThat(duration.toPattern(), duration.isGreaterThanOrEqualTo("17ms"), is(true));
        assertThat(duration.toPattern(), duration.isLessThanOrEqualTo("23ms"), is(true));
    }
}

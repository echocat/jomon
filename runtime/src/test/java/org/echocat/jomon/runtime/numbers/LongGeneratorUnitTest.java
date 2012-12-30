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

package org.echocat.jomon.runtime.numbers;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class LongGeneratorUnitTest {

    @Test
    public void testGenerateExactLong() throws Exception {
        final LongGenerator LongGenerator = new LongGenerator();
        final Long long1 = 100000L;
        final ExactLongRequirement exactLongRequirement = new ExactLongRequirement(long1);
        Long generatedLong = LongGenerator.generateExact(exactLongRequirement);
        assertThat(generatedLong, is(long1));
        generatedLong = LongGenerator.generate(exactLongRequirement);
        assertThat(generatedLong, is(long1));
    }

    @Test
    public void testGenerateLongRange() throws Exception {
        final LongGenerator LongGenerator = new LongGenerator();
        final Long long1 = 100000L;
        final Long long2 = 200000L;
        final LongRangeRequirement LongRangeRequirement = new LongRangeRequirement(long1, long2);
        Long generatedLong = LongGenerator.generateInRange(LongRangeRequirement);
        assertThat(isInRange(generatedLong, long1, long2), is(true));
        generatedLong = LongGenerator.generate(LongRangeRequirement);
        assertThat(isInRange(generatedLong, long1, long2), is(true));
    }

    private boolean isInRange(Long generatedLong, Long Long1, Long Long2) {
        return generatedLong >= Long1 && generatedLong < Long2;
    }
}

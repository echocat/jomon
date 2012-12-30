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

public class ShortGeneratorUnitTest {

    @Test
    public void testGenerateExactShort() throws Exception {
        final ShortGenerator ShortGenerator = new ShortGenerator();
        final Short short1 = 10000;
        final ExactShortRequirement exactShortRequirement = new ExactShortRequirement(short1);
        Short generatedShort = ShortGenerator.generateExact(exactShortRequirement);
        assertThat(generatedShort, is(short1));
        generatedShort = ShortGenerator.generate(exactShortRequirement);
        assertThat(generatedShort, is(short1));
    }

    @Test
    public void testGenerateShortRange() throws Exception {
        final ShortGenerator ShortGenerator = new ShortGenerator();
        final Short short1 = 10000;
        final Short short2 = 20000;
        final ShortRangeRequirement ShortRangeRequirement = new ShortRangeRequirement(short1, short2);
        Short generatedShort = ShortGenerator.generateInRange(ShortRangeRequirement);
        assertThat(isInRange(generatedShort, short1, short2), is(true));
        generatedShort = ShortGenerator.generate(ShortRangeRequirement);
        assertThat(isInRange(generatedShort, short1, short2), is(true));
    }

    private boolean isInRange(Short generatedShort, Short Short1, Short Short2) {
        return generatedShort >= Short1 && generatedShort < Short2;
    }
}

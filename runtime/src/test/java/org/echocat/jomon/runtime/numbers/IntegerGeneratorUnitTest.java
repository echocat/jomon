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

public class IntegerGeneratorUnitTest {

    @Test
    public void testGenerateExactInteger() throws Exception {
        final IntegerGenerator integerGenerator = new IntegerGenerator();
        final Integer integer1 = 100000;
        final ExactIntegerRequirement exactIntegerRequirement = new ExactIntegerRequirement(integer1);
        Integer generatedInteger = integerGenerator.generateExact(exactIntegerRequirement);
        assertThat(generatedInteger, is(integer1));
        generatedInteger = integerGenerator.generate(exactIntegerRequirement);
        assertThat(generatedInteger, is(integer1));
    }

    @Test
    public void testGenerateIntegerRange() throws Exception {
        final IntegerGenerator integerGenerator = new IntegerGenerator();
        final Integer integer1 = 100000;
        final Integer integer2 = 200000;
        final IntegerRangeRequirement integerRangeRequirement = new IntegerRangeRequirement(integer1, integer2);
        Integer generatedInteger = integerGenerator.generateInRange(integerRangeRequirement);
        assertThat(isInRange(generatedInteger, integer1, integer2), is(true));
        generatedInteger = integerGenerator.generate(integerRangeRequirement);
        assertThat(isInRange(generatedInteger, integer1, integer2), is(true));
    }

    private boolean isInRange(Integer generatedInteger, Integer integer1, Integer integer2) {
        return generatedInteger >= integer1 && generatedInteger < integer2;
    }
}

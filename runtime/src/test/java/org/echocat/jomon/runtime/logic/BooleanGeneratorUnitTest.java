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

package org.echocat.jomon.runtime.logic;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class BooleanGeneratorUnitTest {

    @Test
    public void testGenerateExactBoolean() throws Exception {
        final BooleanGenerator booleanGenerator = new BooleanGenerator();
        final Boolean boolean1 = true;
        final ExactBooleanRequirement exactBooleanRequirement = new ExactBooleanRequirement(boolean1);
        Boolean generatedBoolean = booleanGenerator.generateExact(exactBooleanRequirement);
        assertThat(generatedBoolean, is(boolean1));
        generatedBoolean = booleanGenerator.generate(exactBooleanRequirement);
        assertThat(generatedBoolean, is(boolean1));
    }
}

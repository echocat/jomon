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

package org.echocat.jomon.runtime.generation;

import org.echocat.jomon.runtime.util.SerialGenerator;
import org.junit.Test;

import static org.echocat.jomon.runtime.generation.StringRequirement.UNIQUE_VALUE_PLACE_HOLDER;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class StringGeneratorUnitTest {

    @Test
    public void testGenerate() throws Exception {
        final SerialGenerator<?> serialGenerator = mock(SerialGenerator.class);
        doReturn("666").when(serialGenerator).next();
        final StringGenerator generator = new StringGenerator(serialGenerator);
        final String generated = generator.generate(new StringRequirement("foo" + UNIQUE_VALUE_PLACE_HOLDER + "bar"));
        assertThat(generated, is("foo666bar"));
    }
}

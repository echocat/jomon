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

package org.echocat.jomon.runtime.iterators;

import org.junit.Test;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ConvertingIteratorUnitTest {

    private static final int MAX_VALUES = 100;

    @Test
    public void testAll() throws Exception {
        final AtomicInteger integer = new AtomicInteger();
        integer.set(0);

        final Iterator<String> originalInput = new Iterator<String>() {

            @Override
            public boolean hasNext() {
                return integer.get() < MAX_VALUES;
            }

            @Override
            public String next() {
                return "" + integer.getAndIncrement();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };

        final Iterator<String> convertingIterator = new ConvertingIterator<String, String>(originalInput) {
            @Override
            protected String convert(String input) {
                return input + "x";
            }
        };

        int i = 0;
        while(convertingIterator.hasNext()) {
            assertThat(convertingIterator.next(), equalTo(i + "x"));
            i++;
        }
        assertThat(i, equalTo(MAX_VALUES));
    }

}

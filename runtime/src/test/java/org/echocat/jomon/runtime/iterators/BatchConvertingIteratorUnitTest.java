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

import java.util.*;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class BatchConvertingIteratorUnitTest {

    @Test
    public void testIterator() throws Exception {
        final List<Long> input = asList(1L, 2L, 3L);
        final MyBatchConvertingIterator outputIterator = new MyBatchConvertingIterator(input.iterator(), 2);
        // Not a valid operation when iteration is not even started, yet.
        try {
            outputIterator.remove();
        } catch (NoSuchElementException ignore) {}

        assertThat(outputIterator.hasNext(), is(true));
        for (int a = 1; a < 3 && outputIterator.hasNext() ; a++) {
            assertThat(outputIterator.next(), is("" + a));
        }
        assertThat(outputIterator.getInvokes(), is(1));
        assertThat(outputIterator.hasNext(), is(true));
        assertThat(outputIterator.hasNext(), is(true));
        assertThat(outputIterator.next(), is("3"));
        assertThat(outputIterator.hasNext(), is(false));
        assertThat(outputIterator.hasNext(), is(false));
        assertThat(outputIterator.getInvokes(), is(2));

        // Not a valid operation when iteration is empty
        try {
            outputIterator.next();
        } catch (NoSuchElementException ignore) {}
    }

    @Test
    public void testNextBehavior() throws Exception {
        {
            final List<Long> input = new ArrayList<>();
            final MyBatchConvertingIterator outputIterator = new MyBatchConvertingIterator(input.iterator(), 2);
            // Not a valid operation when iteration is empty
            try {
                outputIterator.next();
            } catch (NoSuchElementException ignore) {}
        }
        {
            final List<Long> input = asList(1L);
            final MyBatchConvertingIterator outputIterator = new MyBatchConvertingIterator(input.iterator(), 2);
            // Bad pattern but ok as long as the iterator has further entries
            assertThat(outputIterator.next(), is("1"));
            assertThat(outputIterator.hasNext(), is(false));
        }

    }

    private static class MyBatchConvertingIterator extends BatchConvertingIterator<Long, String> {

        private int _invokes;

        protected MyBatchConvertingIterator(Iterator<Long> input, int batchSize) {
            super(input, batchSize);
        }

        @Override
        protected Iterator<String> convert(Collection<Long> input) {
            _invokes++;
            final List<String> results = new ArrayList<>();
            for (Long aLong : input) {
                results.add("" + aLong);
            }
            return results.iterator();
        }

        public int getInvokes() {
            return _invokes;
        }
    }
}

package org.echocat.jomon.runtime.iterators;

import org.echocat.jomon.runtime.repository.Limited;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class LimitedIteratorUnitTest {
    private static final int MAX_VALUES = 100;
    public static final Integer MAXIMUM_ENTRIES = 10;

    @Rule
    public ExpectedException _exception = ExpectedException.none();

    @Test
    public void testAll() throws Exception {
        final AtomicInteger integer = new AtomicInteger();
        integer.set(0);
        final Iterator<Integer> originalInput = new Iterator<Integer>() {

            @Override
            public boolean hasNext() {
                return integer.get() < MAX_VALUES;
            }

            @Override
            public Integer next() {
                return integer.getAndIncrement();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };

        final Limited limited = new Limited() {
            @Override
            public Long getMaximumOfEntriesToReturn() {
                return MAXIMUM_ENTRIES.longValue();
            }
        };

        final LimitedIterator<Integer> limitedIterator = new LimitedIterator<>(originalInput,limited);
        for(int i=0;i<MAXIMUM_ENTRIES;i++) {
            assertThat(limitedIterator.next(), is(i));
        }

        assertThat(limitedIterator.hasNext(),is(false));

        _exception.expect(NoSuchElementException.class);
        limitedIterator.next();
    }

}

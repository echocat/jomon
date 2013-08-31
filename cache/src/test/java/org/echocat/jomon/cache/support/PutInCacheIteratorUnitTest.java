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

package org.echocat.jomon.cache.support;

import org.echocat.jomon.cache.Cache;
import org.echocat.jomon.cache.LruCache;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import static org.echocat.jomon.testing.Assert.assertThat;

public class PutInCacheIteratorUnitTest {

    private static final int MAX_VALUES = 100;

    @Test
    public void testCacheAccess() throws Exception {
        final Iterator<SimpleIdEnabled> originalInput = getIncreasingIterator();
        final Cache<Integer, SimpleIdEnabled> cache = new LruCache<>(Integer.class, SimpleIdEnabled.class);
        final PutInCacheIterator<Integer, SimpleIdEnabled> iterator = new PutInCacheIterator<>(originalInput,cache);
        iterator.next();
        assertThat(cache.get(0).equals(new SimpleIdEnabled(0)));
        iterator.close();
    }

    @Nonnull
    private Iterator<SimpleIdEnabled> getIncreasingIterator() {
        final AtomicInteger integer = new AtomicInteger();
        integer.set(0);
        return new Iterator<SimpleIdEnabled>() {

            @Override
            public boolean hasNext() {
                return integer.get() < MAX_VALUES;
            }

            @Override
            public SimpleIdEnabled next() {
                return new SimpleIdEnabled(integer.getAndIncrement());
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

}

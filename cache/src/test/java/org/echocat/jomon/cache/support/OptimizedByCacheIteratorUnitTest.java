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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.mockito.Mockito.*;

public class OptimizedByCacheIteratorUnitTest {

    @Test
    public void testDelegateAccess() {
        final Cache<Integer, SimpleIdEnabled> cache = new LruCache<>(Integer.class, SimpleIdEnabled.class);
        final List<Integer> ids = Arrays.asList(0,1);
        cache.put(0,new SimpleIdEnabled(0));
        // noinspection unchecked
        final Iterator<SimpleIdEnabled> mockIterator = mock(Iterator.class);
        when(mockIterator.hasNext()).thenReturn(true);
        final OptimizedByCacheIterator<Integer, SimpleIdEnabled> iterator = new OptimizedByCacheIterator<Integer, SimpleIdEnabled>(cache,ids) {
            @Override
            protected Iterator<SimpleIdEnabled> getDelegateFor(@Nonnull List<Integer> idsNotInCache) {
                return mockIterator;
            }
        };
        iterator.next();
        verify(mockIterator, never()).next();
        iterator.next();
        verify(mockIterator).next();
        iterator.close();
    }
}

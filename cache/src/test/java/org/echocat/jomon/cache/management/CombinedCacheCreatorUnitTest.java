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

package org.echocat.jomon.cache.management;

import org.echocat.jomon.cache.*;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.echocat.jomon.cache.management.CombinedCacheDefinition.combinedCacheOf;
import static org.echocat.jomon.cache.management.CombinedCacheDefinition.with;
import static org.echocat.jomon.cache.management.DefaultCacheDefinition.lfuCache;
import static org.echocat.jomon.cache.management.DefaultCacheDefinition.lruCache;
import static org.echocat.jomon.testing.ArrayMatchers.hasSize;
import static org.echocat.jomon.testing.Assert.assertThat;
import static org.echocat.jomon.testing.BaseMatchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class CombinedCacheCreatorUnitTest {

    @Test
    public void testCreate() throws Exception {
        final CombinedCacheCreator creator = new CombinedCacheCreator();
        final CacheProvider cacheProvider = mock(CacheProvider.class);
        final Answer<Cache<?, ?>> answer = new Answer<Cache<?, ?>>() { @Override public Cache<?, ?> answer(InvocationOnMock invocation) throws Throwable {
            return creator.create(cacheProvider, creator, (CacheDefinition<?, ?, ?>) invocation.getArguments()[1]);
        }};
        //noinspection unchecked
        doAnswer(answer).when(cacheProvider).provide(eq("a"), any(CacheDefinition.class));
        //noinspection unchecked
        doAnswer(answer).when(cacheProvider).provide(eq("b"), any(CacheDefinition.class));

        final CombinedCache<String, Number> cache = (CombinedCache<String, Number>) creator.create(cacheProvider, creator, combinedCacheOf(String.class, Number.class,
            with("a", lruCache(String.class, Integer.class).withCapacity(10)),
            with("b", lfuCache(String.class, Integer.class).withCapacity(100))
        ));
        assertThat(cache, isNot(null));
        final Cache<String, Number>[] delegates = cache.getDelegates();
        assertThat(delegates, hasSize(2));
        assertThat(delegates[0], isInstanceOf(LruCache.class));
        assertThat(((LimitedCache) delegates[0]).getCapacity(), is(10L));

        assertThat(delegates[1], isInstanceOf(LfuCache.class));
        assertThat(((LimitedCache) delegates[1]).getCapacity(), is(100L));
    }

}

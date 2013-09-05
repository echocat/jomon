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

package org.echocat.jomon.cache.management;

import org.echocat.jomon.cache.Cache;
import org.echocat.jomon.cache.LruCache;
import org.echocat.jomon.cache.ServletRequestBasedCache;
import org.echocat.jomon.cache.ServletRequestBasedCache.Resolver;
import org.junit.Test;

import static org.echocat.jomon.cache.management.ServletRequestBasedCacheDefinition.servletRequestBasedCache;
import static org.echocat.jomon.testing.Assert.assertThat;
import static org.echocat.jomon.testing.BaseMatchers.is;
import static org.echocat.jomon.testing.BaseMatchers.isInstanceOf;
import static org.mockito.Mockito.mock;

public class ServletRequestBasedCacheCreatorUnitTest {

    @Test
    public void testCreate() throws Exception {
        final CacheCreator creator = new ServletRequestBasedCacheCreator();

        final CacheCreator master = mock(CacheCreator.class);
        final Resolver resolver = mock(Resolver.class);

        final Cache<Integer, String> cache = creator.create(null, master, servletRequestBasedCache(Integer.class, String.class, resolver));

        assertThat(cache, isInstanceOf(ServletRequestBasedCache.class));
        final ServletRequestBasedCache<Integer, String> requestCache = (ServletRequestBasedCache<Integer, String>) cache;
        assertThat(requestCache.getCacheCreator(), is(master));
        assertThat(requestCache.getResolver(), is(resolver));
        assertThat(requestCache.getDelegateDefinition().getRequiredType(), is((Object) LruCache.class));

    }
}

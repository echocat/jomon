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

package org.echocat.jomon.cache;

import org.echocat.jomon.cache.ServletRequestBasedCache.Resolver;
import org.echocat.jomon.cache.management.LruCacheCreator;
import org.echocat.jomon.runtime.util.Duration;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.servlet.ServletRequest;

import static org.echocat.jomon.runtime.util.Duration.sleep;
import static org.echocat.jomon.testing.Assert.assertThat;
import static org.echocat.jomon.testing.BaseMatchers.*;
import static org.mockito.Mockito.*;

public class ServletRequestBasedCacheUnitTest {

    @Test
    public void testGetPutAndContains() throws Exception {
        final ServletRequest request = servletRequest();
        final Resolver resolver = resolver();
        final ServletRequestBasedCache<String, Integer> cache = servletRequestBasedCache(resolver);
        doReturn(request).when(resolver).resolve(cache);
        final LruCache<String, Integer> delegate = new LruCache<>(String.class, Integer.class);
        doReturn(delegate).when(request).getAttribute(cache.getRequestCacheAttributeName());

        cache.put("foo", 1);
        cache.put("bar", 2, new Duration("30ms"));
        assertThatAllObjectsAreThere(cache, delegate);

        sleep("40ms");
        assertThatFooIsThereAndBarIsGone(cache, delegate);

        doReturn(null).when(request).getAttribute(cache.getRequestCacheAttributeName());
        assertThatFooIsOnlyInDelegate(cache, delegate);

        doReturn(delegate).when(request).getAttribute(cache.getRequestCacheAttributeName());
        assertThatFooIsThereAndBarIsGone(cache, delegate);

        cache.remove("foo");
        assertThatFooAndBarWasAway(cache, delegate);
    }

    private void assertThatAllObjectsAreThere(@Nonnull ServletRequestBasedCache<String, Integer> cache, @Nonnull LruCache<String, Integer> delegate) {
        assertThat(cache.get("foo"), is(1));
        assertThat(delegate.get("foo"), is(1));
        assertThat(cache.contains("foo"), is(true));
        assertThat(delegate.contains("foo"), is(true));
        assertThat(cache.get("bar"), is(2));
        assertThat(delegate.get("bar"), is(2));
        assertThat(cache.contains("bar"), is(true));
        assertThat(delegate.contains("bar"), is(true));
    }

    private void assertThatFooIsOnlyInDelegate(@Nonnull ServletRequestBasedCache<String, Integer> cache, @Nonnull LruCache<String, Integer> delegate) {
        assertThat(cache.get("foo"), is(null));
        assertThat(delegate.get("foo"), is(1));
        assertThat(cache.contains("foo"), is(false));
        assertThat(delegate.contains("foo"), is(true));
        assertThat(cache.get("bar"), is(null));
        assertThat(delegate.get("bar"), is(null));
        assertThat(cache.contains("bar"), is(false));
        assertThat(delegate.contains("bar"), is(false));
    }

    private void assertThatFooIsThereAndBarIsGone(@Nonnull ServletRequestBasedCache<String, Integer> cache, @Nonnull LruCache<String, Integer> delegate) {
        assertThat(cache.get("foo"), is(1));
        assertThat(delegate.get("foo"), is(1));
        assertThat(cache.contains("foo"), is(true));
        assertThat(delegate.contains("foo"), is(true));
        assertThat(cache.get("bar"), is(null));
        assertThat(delegate.get("bar"), is(null));
        assertThat(cache.contains("bar"), is(false));
        assertThat(delegate.contains("bar"), is(false));
    }

    private void assertThatFooAndBarWasAway(@Nonnull ServletRequestBasedCache<String, Integer> cache, @Nonnull LruCache<String, Integer> delegate) {
        assertThat(cache.get("foo"), is(null));
        assertThat(delegate.get("foo"), is(null));
        assertThat(cache.contains("foo"), is(false));
        assertThat(delegate.contains("foo"), is(false));
        assertThat(cache.get("bar"), is(null));
        assertThat(delegate.get("bar"), is(null));
        assertThat(cache.contains("bar"), is(false));
        assertThat(delegate.contains("bar"), is(false));
    }

    @Test
    public void testFindRequestBasedCache() throws Exception {
        final ServletRequest request = servletRequest();
        final Resolver resolver = resolver();
        final ServletRequestBasedCache<String, Integer> cache = servletRequestBasedCache(resolver);

        assertThat(cache.findRequestBasedCache(), is(null));
        verify(request, times(0)).getAttribute(cache.getRequestCacheAttributeName());

        doReturn(request).when(resolver).resolve(cache);
        final Cache<String, Integer> delegate = cache.findRequestBasedCache();
        assertThat(delegate, isInstanceOf(LruCache.class));
        verify(request, times(1)).getAttribute(cache.getRequestCacheAttributeName());
        verify(request, times(1)).setAttribute(cache.getRequestCacheAttributeName(), delegate);

        doReturn(delegate).when(request).getAttribute(cache.getRequestCacheAttributeName());
        final Cache<String, Integer> delegate2 = cache.findRequestBasedCache();
        assertThat(delegate2, isSameAs(delegate));
        verify(request, times(2)).getAttribute(cache.getRequestCacheAttributeName());
        verify(request, times(1)).setAttribute(cache.getRequestCacheAttributeName(), delegate2);
    }

    @Nonnull
    protected ServletRequestBasedCache<String, Integer> servletRequestBasedCache(Resolver resolver) {
        return new ServletRequestBasedCache<>(String.class, Integer.class, master(), resolver);
    }

    @Nonnull
    protected Resolver resolver() {
        return mock(Resolver.class);
    }

    @Nonnull
    protected LruCacheCreator master() {
        return new LruCacheCreator();
    }

    @Nonnull
    protected ServletRequest servletRequest() {
        return mock(ServletRequest.class);
    }
}

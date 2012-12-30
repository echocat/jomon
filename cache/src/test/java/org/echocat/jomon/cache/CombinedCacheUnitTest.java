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

package org.echocat.jomon.cache;

import org.junit.Test;

import javax.annotation.Nonnull;

import static org.echocat.jomon.testing.Assert.assertThat;
import static org.echocat.jomon.testing.BaseMatchers.is;

public class CombinedCacheUnitTest {

    @Test
    public void testPutGetAndContains() throws Exception {
        final LruCache<String, Integer> cacheA = cacheWithCapacity(2);
        final LruCache<String, Integer> cacheB = cacheWithCapacity(4);
        final CombinedCache<String, Number> cache = new CombinedCache<>(String.class, Number.class, cacheA, cacheB);

        cache.put("foo1", 1);
        cache.put("foo2", 2);
        assertThatFirstValuesArePresent(cacheA, cacheB, cache);

        cache.put("foo3", 3);
        assertThatRetrieveOnCacheBWorksIfNotFoundOnCacheA(cacheA, cacheB, cache);

        cache.remove("foo1");
        assertThatFirstValueIsRemovedOnAllCaches(cacheA, cacheB, cache);

        cache.remove("foo2");
        assertThatSecondValueIsRemovedOnAllCaches(cacheA, cacheB, cache);

    }

    private void assertThatFirstValuesArePresent(@Nonnull LruCache<String, Integer> cacheA, @Nonnull LruCache<String, Integer> cacheB, @Nonnull CombinedCache<String, Number> cache) {
        assertThat((Integer) cache.get("foo1"), is(1));
        assertThat(cache.contains("foo1"), is(true));
        assertThat(cacheA.get("foo1"), is(1));
        assertThat(cacheB.get("foo1"), is(1));

        assertThat((Integer) cache.get("foo2"), is(2));
        assertThat(cache.contains("foo2"), is(true));
        assertThat(cacheA.get("foo2"), is(2));
        assertThat(cacheB.get("foo2"), is(2));
    }

    private void assertThatRetrieveOnCacheBWorksIfNotFoundOnCacheA(@Nonnull LruCache<String, Integer> cacheA, @Nonnull LruCache<String, Integer> cacheB, @Nonnull CombinedCache<String, Number> cache) {
        assertThat(cacheA.get("foo1"), is(null));
        assertThat(cacheB.get("foo1"), is(1));
        assertThat((Integer) cache.get("foo1"), is(1));
        assertThat(cacheA.get("foo1"), is(1)); // Because it was restored from cacheB
        assertThat(cache.contains("foo1"), is(true));

        assertThat(cacheA.get("foo2"), is(null));
        assertThat(cacheB.get("foo2"), is(2));
        assertThat((Integer) cache.get("foo2"), is(2));
        assertThat(cacheA.get("foo2"), is(2)); // Because it was restored from cacheB
        assertThat(cache.contains("foo2"), is(true));

        assertThat(cacheA.get("foo3"), is(null));
        assertThat(cacheB.get("foo3"), is(3));
        assertThat((Integer) cache.get("foo3"), is(3));
        assertThat(cacheA.get("foo3"), is(3)); // Because it was restored from cacheB
        assertThat(cache.contains("foo3"), is(true));
    }

    private void assertThatFirstValueIsRemovedOnAllCaches(@Nonnull LruCache<String, Integer> cacheA, @Nonnull LruCache<String, Integer> cacheB, @Nonnull CombinedCache<String, Number> cache) {
        assertThat(cache.get("foo1"), is(null));
        assertThat(cache.contains("foo1"), is(false));
        assertThat(cacheA.get("foo1"), is(null));
        assertThat(cacheB.get("foo1"), is(null));

        assertThat((Integer) cache.get("foo2"), is(2));
        assertThat(cache.contains("foo2"), is(true));
        assertThat(cacheA.get("foo2"), is(2));
        assertThat(cacheB.get("foo2"), is(2));

        assertThat((Integer) cache.get("foo3"), is(3));
        assertThat(cache.contains("foo3"), is(true));
        assertThat(cacheA.get("foo3"), is(3));
        assertThat(cacheB.get("foo3"), is(3));
    }

    private void assertThatSecondValueIsRemovedOnAllCaches(@Nonnull LruCache<String, Integer> cacheA, @Nonnull LruCache<String, Integer> cacheB, @Nonnull CombinedCache<String, Number> cache) {
        assertThat(cache.get("foo1"), is(null));
        assertThat(cache.contains("foo1"), is(false));
        assertThat(cacheA.get("foo1"), is(null));
        assertThat(cacheB.get("foo1"), is(null));

        assertThat(cache.get("foo2"), is(null));
        assertThat(cache.contains("foo2"), is(false));
        assertThat(cacheA.get("foo2"), is(null));
        assertThat(cacheB.get("foo2"), is(null));

        assertThat((Integer) cache.get("foo3"), is(3));
        assertThat(cache.contains("foo3"), is(true));
        assertThat(cacheA.get("foo3"), is(3));
        assertThat(cacheB.get("foo3"), is(3));
    }

    @Nonnull
    private LruCache<String, Integer> cacheWithCapacity(long capacity) {
        final LruCache<String, Integer> a = new LruCache<>(String.class, Integer.class);
        a.setCapacity(capacity);
        return a;
    }
}

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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.echocat.jomon.runtime.CollectionUtils.asSet;
import static org.echocat.jomon.testing.BaseMatchers.is;
import static org.junit.Assert.*;

public class LruCacheUnitTest extends CacheUnitTestSupport<LruCache<Object, Object>> {

    @Override
    protected LruCache<Object, Object> getInstance() {
        return new LruCache<>(Object.class, Object.class);
    }

    @SuppressWarnings("OverlyLongMethod")
    @Test
    public void testPutGetRemove() throws Exception {

        final LruCache<Object, Object> cache = getInstance(100, 5);

        cache.put("1", "_1");
        assertLinkedList(cache);

        cache.put("2", "_2");
        assertLinkedList(cache);

        cache.put("3", "_3");
        assertLinkedList(cache);

        cache.put("4", "_4");
        assertLinkedList(cache);

        cache.put("5", "_5");
        assertLinkedList(cache);

        assertEquals("_5", cache.get("5"));
        assertLinkedList(cache);

        {
            final Map<Object, Object> expected = new HashMap<>();
            expected.put("1", "_1");
            expected.put("2", "_2");
            expected.put("3", "_3");
            expected.put("4", "_4");
            expected.put("5", "_5");
            assertMapEntries(expected, cache);

        }

        cache.put("6", "_6");
        assertLinkedList(cache);

        assertNull(cache.get("1"));
        assertLinkedList(cache);

        {
            final Map<Object, Object> expected = new HashMap<>();
            expected.put("5", "_5");
            expected.put("2", "_2");
            expected.put("3", "_3");
            expected.put("4", "_4");
            expected.put("6", "_6");
            assertMapEntries(expected, cache);
        }

        cache.get("2");
        assertLinkedList(cache);

        cache.put("7", "_7");
        assertLinkedList(cache);

        assertNull(cache.get("3"));
        assertLinkedList(cache);

        cache.remove("7");
        assertLinkedList(cache);

        assertNull(cache.get("7"));
        assertLinkedList(cache);

        cache.remove("6");
        assertLinkedList(cache);

        cache.remove("5");
        assertLinkedList(cache);

        cache.remove("4");
        assertLinkedList(cache);

        cache.remove("2");
        assertLinkedList(cache);

        assertEquals(0, (long) cache.size());

        cache.put("a1", "_1");
        assertLinkedList(cache);

        assertEquals(1, (long) cache.size());

        cache.put("a2", "_2");
        assertLinkedList(cache);

        cache.put("a3", "_3");
        assertLinkedList(cache);

        cache.put("a4", "_4");
        assertLinkedList(cache);

        cache.get("a1");
        assertLinkedList(cache);

        cache.get("a2");
        assertLinkedList(cache);

        cache.put("a5", "5");
        assertLinkedList(cache);

        cache.put("a6", "6");
        assertLinkedList(cache);

        {
            final Map<Object, Object> expected = new HashMap<>();
            expected.put("a1", "_1");
            expected.put("a2", "_2");
            expected.put("a6", "6");
            expected.put("a4", "_4");
            expected.put("a5", "5");
            assertMapEntries(expected, cache);

        }

        Thread.sleep(60);

        cache.put("n1", "1");
        assertLinkedList(cache);

        cache.put("n2", "2");
        assertLinkedList(cache);

        cache.get("a2");
        assertLinkedList(cache);

        Thread.sleep(60);

        cache.put("n3", "3");
        assertLinkedList(cache);

        {
            final Map<Object, Object> expected = new HashMap<>();
            expected.put("n1", "1");
            expected.put("n3", "3");
            expected.put("n2", "2");
            assertMapEntries(expected, cache);
        }

        cache.remove("n3");
        assertLinkedList(cache);

        cache.remove("n1");
        assertLinkedList(cache);

    }

    @Test
    public void testClear() throws Exception {
        final LruCache<Object, Object> cache = getInstance(1000, 5);
        cache.put("1", "v1");
        cache.put("2", "v2");
        cache.put("3", "v3");
        assertEquals("Initialization", 3, (long)cache.size());

        cache.clear();
        assertEquals("size()", 0, (long)cache.size());

        assertNull("get after clear", cache.get("1"));

    }

    @Test
    public void testPutGetRemoveForOneItem() throws Exception {
        final LruCache<Object, Object> cache = getInstance(1000, 5);
        cache.put(1, "foo");
        assertEquals("foo", cache.get(1));
        cache.remove(1);
        assertNull(cache.get(1));
    }

    @Test
    public void testHandleRemove() {
        final Set<Integer> handled = new HashSet<>();
        final LruCache<Integer, Integer> cache = new LruCache<Integer, Integer>(Integer.class, Integer.class) {
            @Override
            protected void handleRemove(CacheEntry<Integer, Integer> value) {
                super.handleRemove(value);
                handled.add(value.getValue().get());
            }
        };
        cache.setCapacity(3L);
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);
        // This should lead to a call to handleRemove(1), because max size = 3 and therefore removeLast() will be called ...
        cache.put(4, 4);
        assertThat(handled, is(asSet(1)));
        // This should of course lead to a call to handleRemove(3) ...
        cache.remove(3);
        assertThat(handled, is(asSet(1, 3)));
        // This should lead to a call to handleRemove(2), because the cached value for key 2 is 5 now ...
        cache.put(2, 5);
        assertThat(cache.get(2), is(5));
        assertThat(handled, is(asSet(1, 2, 3)));
        // This should lead to handleRemove(...) calls for all remaining values in the cache ...
        cache.clear();
        assertThat(handled, is(asSet(1, 2, 3, 4, 5)));
    }

}

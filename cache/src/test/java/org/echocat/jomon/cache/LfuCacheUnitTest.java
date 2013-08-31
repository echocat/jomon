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

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LfuCacheUnitTest extends CacheUnitTestSupport<LfuCache<Object, Object>> {

    @Override
    protected LfuCache<Object, Object> getInstance() {
        return new LfuCache<>(Object.class, Object.class);
    }

    @SuppressWarnings("OverlyLongMethod")
    @Test
    public void testCorrectness() throws Exception {

        final LfuCache<Object, Object> cache = getInstance(100, 5);

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

        assertNull(cache.get("4"));
        assertLinkedList(cache);

        {
            final Map<Object, Object> expected = new HashMap<>();
            expected.put("1", "_1");
            expected.put("2", "_2");
            expected.put("3", "_3");
            expected.put("6", "_6");
            expected.put("5", "_5");
            assertMapEntries(expected, cache);

        }

        cache.get("2");
        assertLinkedList(cache);

        cache.put("7", "_7");
        assertLinkedList(cache);

        assertNull(cache.get("6"));

        cache.remove("7");
        assertLinkedList(cache);

        assertNull(cache.get("7"));

        cache.remove("5");
        assertLinkedList(cache);

        cache.remove("3");
        assertLinkedList(cache);

        cache.remove("2");
        assertLinkedList(cache);

        cache.remove("1");
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
            expected.put("a3", "_3");
            expected.put("a4", "_4");
            expected.put("a6", "6");
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
            expected.put("n3", "3");
            expected.put("n2", "2");
            assertMapEntries(expected, cache);

        }

        // Remove nonexistent element
        cache.remove("n1");
        assertLinkedList(cache);

        // Query nonexistent element
        cache.get("n1");
        assertLinkedList(cache);

        // Test with getting an expired element
        cache.clear();
        cache.put("a", "_a");
        cache.put("b", "_b");
        cache.put("c", "_c");
        cache.get("c");

        // Test a bug that occured when an outdated element has bee
        // removed by the get-method
        Thread.sleep(120);
        assertNull(cache.get("a"));
        assertNull(cache.get("b"));
        assertNull(cache.get("c"));
    }

}

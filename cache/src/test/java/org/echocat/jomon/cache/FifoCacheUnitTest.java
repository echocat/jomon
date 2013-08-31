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

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FifoCacheUnitTest extends CacheUnitTestSupport<FifoCache<Object, Object>> {

    @Nonnull
    @Override
    protected FifoCache<Object, Object> getInstance() {
        return new FifoCache<>(Object.class, Object.class);
    }

    @SuppressWarnings("OverlyLongMethod")
    @Test
    public void testCorrectness() throws Exception {

        final FifoCache<Object, Object> cache = getInstance(100, 5);

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
            expected.put("6", "_6");
            expected.put("2", "_2");
            expected.put("3", "_3");
            expected.put("4", "_4");
            expected.put("5", "_5");
            assertMapEntries(expected, cache);
        }

        // Query a nonexisting element
        assertNull(cache.get("12323"));
        assertLinkedList(cache);


        Thread.sleep(200);

        assertNull(cache.get("2"));
        assertLinkedList(cache);

        assertEquals(4, (long) cache.size());

        cache.put("7", "_7");
        assertLinkedList(cache);

        cache.put("8", "_8");
        assertLinkedList(cache);

        assertEquals(2, (long) cache.size());

        // Verify the request / hits stats
        assertEquals(4, (long) cache.getNumberOfRequests());
        assertEquals(1, (long) cache.getNumberOfHits());

        cache.clear();
        assertEquals(0, (long) cache.size());
    }

}

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

import org.echocat.jomon.runtime.util.Duration;
import org.echocat.jomon.testing.concurrent.ParallelTestRunner;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.echocat.jomon.testing.BaseMatchers.isLessThanOrEqualTo;
import static org.junit.Assert.*;

public abstract class CacheUnitTestSupport<T extends LimitedCache<Object, Object> & StatisticsEnabledCache<Object, Object>> {

    @Nonnull
    protected abstract T getInstance();

    @Nonnull
    protected T getInstance(Duration maxLifeTime, int maxEntries) {
        final T cache = getInstance();
        cache.setMaximumLifetime(maxLifeTime);
        cache.setCapacity((long)maxEntries);
        return cache;
    }

    @Nonnull
    protected T getInstance(long maxLifeTime, int maxEntries) {
        return getInstance(new Duration(maxLifeTime), maxEntries);
    }

    /**
     * Assert the correct order of the linked list contained in each AbstractCache.
     */
    protected static void assertLinkedList(InMemoryBasedCacheSupport<?, ?> c) throws Exception {
        if (c.getFirst() == null || c.getLast() == null) {
            assertNull(c.getLast());
            assertNull(c.getFirst());
        } else {
            assertNull(c.getFirst().getPrevious());
            assertNull(c.getLast().getNext());

            CacheEntry<?, ?> ce = c.getFirst();
            while (ce != null) {
                if (!ce.equals(c.getLast())) {
                    assertNotNull(ce.getNext());
                    assertEquals(ce.getNext().getPrevious(), ce);
                }
                if (!ce.equals(c.getFirst())) {
                    assertEquals(ce.getPrevious().getNext(), ce);
                }
                ce = ce.getNext();
            }
        }

    }

    protected static void assertMapEntries(Map<Object, Object> expected, InMemoryBasedCacheSupport<Object, Object> cache) {
        assertEquals("Length not currect", expected.size(), cache.getEntries().size());
        for (Object key : expected.keySet()) {
            final CacheEntry<?, ?> entry = cache.getEntries().get(key);
            assertNotNull("Entry for key '" + key + "' is null!", entry);
            assertEquals("key '" + key + "'", expected.get(key), entry.getValue().get());
        }
    }

    @Test
    public void testMaxLifeTimeSupport() throws Exception {
        final T cache = getInstance(100, 100);
        cache.put("varA", "valueA");
        cache.put("varB", "valueB");
        Thread.sleep(70);
        cache.put("varC", "valueC");
        assertEquals("valueA", cache.get("varA"));
        assertEquals("valueB", cache.get("varB"));
        assertEquals("valueC", cache.get("varC"));
        Thread.sleep(70);
        assertNull("valueA", cache.get("varA"));
        assertNull("valueB", cache.get("varB"));
        assertEquals("valueC", cache.get("varC"));
        Thread.sleep(70);
        assertNull("valueA", cache.get("varA"));
        assertNull("valueB", cache.get("varB"));
        assertNull("valueC", cache.get("varC"));
    }

    @Test
    public void testWithNullAsKey() throws Exception {
        final T cache = getInstance(1000, 1000);
        final Object o = new Object();
        assertEquals(0, (long)cache.size());
        assertFalse(cache.contains(null));
        assertNull(cache.get(null));
        // Add entry with null as key ...
        cache.put(null, o);
        assertEquals(1, (long)cache.size());
        assertTrue(cache.contains(null));
        assertSame(o, cache.get(null));
        // Add another entry ...
        cache.put(o, o);
        assertEquals(2, (long)cache.size());
        // Remove entry with null as key ...
        assertSame(o, cache.remove(null).get());
        assertEquals(1, (long)cache.size());
        assertFalse(cache.contains(null));
        assertNull(cache.get(null));
        // Try to remove entry with null as key again ...
        assertNull(cache.remove(null));
        assertEquals(1, (long)cache.size());
        assertFalse(cache.contains(null));
        assertNull(cache.get(null));
    }

    @Test
    public void testWithNullAsValue() throws Exception {
        final T cache = getInstance(1000, 1000);
        final Object o = new Object();
        assertEquals(0, (long)cache.size());
        assertFalse(cache.contains(o));
        assertNull(cache.get(o));
        // Add entry with null as value ...
        cache.put(o, null);
        assertEquals(1, (long)cache.size());
        assertTrue(cache.contains(o));
        assertNull(cache.get(o));
        // Remove entry with null as value ...
        assertNull(cache.remove(o).get());
        assertEquals(0, (long)cache.size());
        assertFalse(cache.contains(o));
        assertNull(cache.get(o));
        // Try to remove entry with null as value again ...
        assertNull(cache.remove(o));
        assertEquals(0, (long)cache.size());
        assertFalse(cache.contains(o));
        assertNull(cache.get(o));
    }

    @Test
    public void testWithNullAsKeyAndValue() throws Exception {
        final T cache = getInstance(1000, 1000);
        assertEquals(0, (long)cache.size());
        assertFalse(cache.contains(null));
        assertNull(cache.get(null));
        // Add entry with null as key and value ...
        cache.put(null, null);
        assertEquals(1, (long)cache.size());
        assertTrue(cache.contains(null));
        assertNull(cache.get(null));
        // Add another entry ...
        cache.put(new Object(), new Object());
        assertEquals(2, (long)cache.size());
        // Remove entry with null as key ...
        assertNull(cache.remove(null).get());
        assertEquals(1, (long)cache.size());
        assertFalse(cache.contains(null));
        assertNull(cache.get(null));
        // Try to remove entry with null as key again ...
        assertNull(cache.remove(null));
        assertEquals(1, (long)cache.size());
        assertFalse(cache.contains(null));
        assertNull(cache.get(null));
    }

    @Test
    public void testWithMaxSize1() {
        final T cache = getInstance(1000, 1);
        final Object o1 = new Object();
        cache.put(o1, o1);
        final Object o2 = new Object();
        cache.put(o2,o2);
        assertEquals(1, (long)cache.size());
        assertFalse(cache.contains(o1));
        assertTrue(cache.contains(o2));
    }

    @Test
    public void testShouldNotGrowLargerThanMaxSize() throws Exception {
        final long maxCacheSize = 1000;
        final T cache = getInstance(Integer.MAX_VALUE, (int)maxCacheSize);
        final List<ParallelTestRunner.Worker> workers = new ArrayList<>();
        final int numThreads = 50;
        final int numRoundsPerThread = 50000;
        final int minKey = 10000;
        final int maxKey = 99999;
        for (int i = 0; i < numThreads; ++i) {
            final Random random = new Random(i);
            workers.add(new ParallelTestRunner.Worker() {
                @Override
                public void run() throws Exception {
                    for (int i = 0; i < numRoundsPerThread; ++i) {
                        final int key = random.nextInt(maxKey - minKey) + minKey;
                        cache.put(key, Integer.toBinaryString(key));
                    }
                }
            });
        }
        final ParallelTestRunner parallelTestRunner = new ParallelTestRunner(workers);
        parallelTestRunner.run();
        assertThat(cache.size(), isLessThanOrEqualTo(maxCacheSize));
    }

}

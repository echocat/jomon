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
import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.echocat.jomon.testing.BaseMatchers.isLessThan;
import static org.junit.Assert.assertThat;

public abstract class CachePerformanceTestSupport<T extends LimitedCache<String, String>> {

    private static final int MAX_OPERATIONS = 100000;

    private static Map<String, String> c_testMap;
    private static long c_mapPutTime;
    private static long c_mapGetTime;
    private static long c_mapRemoveTime;

    @Nonnull
    protected abstract T getInstance();

    @Nonnull
    protected T getInstance(int maxLifeTime, long maxEntries) {
        return getInstance(new Duration(maxLifeTime), maxEntries);
    }

    @Nonnull
    protected T getInstance(Duration maxLifeTime, long maxEntries) {
        final T cache = getInstance();
        cache.setMaximumLifetime(maxLifeTime);
        cache.setCapacity(maxEntries);
        return cache;
    }

    private static Map<String, String> initMapPutTime() {
        if (c_mapPutTime == 0 || c_testMap == null) {
            final Map<String, String> map = new ConcurrentHashMap<>();
            final long startTime = System.nanoTime();
            for (int i = 0; i < MAX_OPERATIONS; ++i) {
                map.put(Integer.toString(i), "_" + i);
            }
            c_mapPutTime = System.nanoTime() - startTime;
            c_testMap = map;
        }
        return c_testMap;
    }

    private static void initMapGetTime() {
        if (c_mapGetTime == 0) {
            final Map<String, String> map = initMapPutTime();
            final long startTime = System.nanoTime();
            // measure MAX_OPERATIONS successful look ups as well as MAX_OPERATIONS unsuccessful look ups ...
            for (int i = 0; i < MAX_OPERATIONS * 2; ++i) {
                map.get(Integer.toString(i));
            }
            c_mapGetTime = System.nanoTime() - startTime;
        }
    }

    private static void initMapRemoveTime() {
        if (c_mapRemoveTime == 0) {
            final Map<String, String> map = initMapPutTime();
            final long startTime = System.nanoTime();
            // measure MAX_OPERATIONS unsuccessful look ups as well as MAX_OPERATIONS successful look ups ...
            for (int i = MAX_OPERATIONS * 2 - 1; i >= 0; --i) {
                map.remove(Integer.toString(i));
            }
            c_mapRemoveTime = System.nanoTime() - startTime;
        }
    }

    @Test
    public final void testPut() {
        initMapPutTime();
        final Cache<String, String> cache = getInstance(100000, MAX_OPERATIONS);
        final long startTime = System.nanoTime();
        for (int i = 0; i < MAX_OPERATIONS; ++i) {
            cache.put(Integer.toString(i), "_" + i);
        }
        final long duration = System.nanoTime() - startTime;
        assertThat(duration, isLessThan(c_mapPutTime * 2));
    }

    @Test
    public final void testPutMoreObjectsThanMaxSize() throws Exception {
        initMapPutTime();
        final Cache<String, String> cache = getInstance(100000, MAX_OPERATIONS / 10);
        final long startTime = System.nanoTime();
        for (int i = 0; i < MAX_OPERATIONS; ++i) {
            cache.put(Integer.toString(i), "_" + i);
        }
        final long duration = System.nanoTime() - startTime;
        assertThat(duration, isLessThan((long)(c_mapPutTime * 3d)));
    }

    @Test
    public final void testPutWithShortLifetime() {
        initMapPutTime();
        final Cache<String, String> cache = getInstance(5, MAX_OPERATIONS);
        final long startTime = System.nanoTime();
        for (int i = 0; i < MAX_OPERATIONS; ++i) {
            cache.put(Integer.toString(i), "_" + i);
        }
        final long duration = System.nanoTime() - startTime;
        assertThat(duration, isLessThan(c_mapPutTime * 2));
    }

    @Test
    public void testGet() {
        initMapGetTime();
        final Cache<String, String> cache = getInstance(100000, MAX_OPERATIONS / 2);
        for (int i = 0; i < MAX_OPERATIONS; ++i) {
            cache.put(Integer.toString(i), "_" + i);
        }
        final long startTime = System.nanoTime();
        // measure MAX_OPERATIONS successful look ups as well as MAX_OPERATIONS unsuccessful look ups ...
        for (int i = 0; i < MAX_OPERATIONS * 2; ++i) {
            cache.get(Integer.toString(i));
        }
        final long duration = System.nanoTime() - startTime;
        assertThat(duration, isLessThan((long) (c_mapGetTime * 5d)));
    }

    @Test
    public void testRemove() {
        initMapRemoveTime();
        final Cache<String, String> cache = getInstance(100000, MAX_OPERATIONS / 2);
        for (int i = 0; i < MAX_OPERATIONS; ++i) {
            cache.put(Integer.toString(i), "_" + i);
        }
        final long startTime = System.nanoTime();
        // measure MAX_OPERATIONS unsuccessful look ups as well as MAX_OPERATIONS successful look ups ...
        for (int i = MAX_OPERATIONS * 2 - 1; i >= 0; --i) {
            cache.remove(Integer.toString(i));
        }
        final long duration = System.nanoTime() - startTime;
        assertThat(duration, isLessThan((long) (c_mapRemoveTime * 3d)));
    }
}

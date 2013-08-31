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
import org.echocat.jomon.runtime.util.ValueProducer;
import org.echocat.jomon.testing.concurrent.ParallelTestRunner;
import org.echocat.jomon.testing.concurrent.ParallelTestRunner.Worker;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static org.echocat.jomon.testing.BaseMatchers.is;
import static org.junit.Assert.assertThat;

public abstract class CacheStressTestSupport<T extends LimitedCache<Integer, String>>{

    @Test
    public void testThatEachCacheValueIsProducedOnlyOnce() throws Exception {
        final int numThreads = 100;
        final int numEntries = 1000;
        final Random random = new Random();
        final AtomicInteger producedValues = new AtomicInteger(0);
        final ValueProducer<Integer, String> producer = new ValueProducer<Integer, String>() { @Override public String produce(Integer key) throws Exception {
            producedValues.incrementAndGet();
            Thread.sleep(random.nextInt(50));
            return key.toString();
        }};
        final Cache<Integer, String> cache = getInstance(Integer.MAX_VALUE, numEntries);
        final List<Worker> workers = new ArrayList<>();
        for (int i = 1; i <= numThreads; ++i) {
            workers.add(new ParallelTestRunner.Worker() {
                @Override
                public void run() throws Exception {
                    final List<Integer> keys = new ArrayList<>(numEntries);
                    for (int i = 0; i < numEntries; ++i) {
                        keys.add(i);
                    }
                    Collections.shuffle(keys);
                    for (Integer key : keys) {
                        assertThat(cache.get(key, producer), is(key.toString()));
                    }
                }
            });
        }
        new ParallelTestRunner(workers).run();
        assertThat(producedValues.get(), is(numEntries));
        final StatisticsEnabledCache<?, ?> statisticsEnabledCache = (StatisticsEnabledCache) cache;
        assertThat(statisticsEnabledCache.getNumberOfRequests(), is((long) numThreads * numEntries));
        assertThat(statisticsEnabledCache.getNumberOfHits(), is(statisticsEnabledCache.getNumberOfRequests() - numEntries));
    }

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

}

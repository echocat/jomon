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
import org.echocat.jomon.runtime.util.Value;
import org.echocat.jomon.runtime.util.ValueProducer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Cache<K, V> {


    public void put(@Nullable K key, @Nullable V value);

    public void put(@Nullable K key, @Nullable V value, @Nullable Duration expireAfter);

    /**
     * Returns the cached value for the given key, returns <code>null</code>
     * if there is no  cached value for the given key.
     */
    @Nullable
    public V get(@Nullable K key);

    /**
     * Returns the cached value for the given key if present,
     * otherwise the value will be produced using the given cacheValueProducer,
     * inserted into the cache and than returned. In concurrent situations
     * it is guaranteed that if multiple threads request the same key at
     * the same time, the value will produced only <b>once</b>, all other
     * threads will wait until the value is produced.
     */
    @Nullable
    public V get(@Nullable K key, @Nullable ValueProducer<K, V> cacheValueProducer, @Nullable Duration expireAfter);

    /**
     * Returns the cached value for the given key if present,
     * otherwise the value will be produced using the given cacheValueProducer,
     * inserted into the cache and than returned. In concurrent situations
     * it is guaranteed that if multiple threads request the same key at
     * the same time, the value will produced only <b>once</b>, all other
     * threads will wait until the value is produced.
     */
    @Nullable
    public V get(@Nullable K key, @Nullable ValueProducer<K, V> cacheValueProducer);

    @Nullable
    public Value<V> remove(@Nullable K key);

    public boolean contains(@Nullable K key);

    @Nonnull
    public Class<? extends K> getKeyType();

    @Nonnull
    public Class<? extends V> getValueType();
}

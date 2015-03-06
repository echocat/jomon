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

import org.echocat.jomon.runtime.util.Value;
import org.echocat.jomon.runtime.util.Value.Lazy;
import org.echocat.jomon.runtime.util.Duration;
import org.echocat.jomon.runtime.util.ValueProducer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

import static org.echocat.jomon.runtime.util.ProducingType.nonBlocking;

public class CombinedCache<K, V> extends CacheSupport<K, V> {

    private final Cache<? extends K, ? extends V>[] _delegates;

    public CombinedCache(@Nonnull Class<? extends K> keyType, @Nonnull Class<? extends V> valueType, @Nullable Cache<? extends K, ? extends V>... delegates) {
        super(keyType, valueType);
        // noinspection unchecked
        _delegates = delegates != null ? delegates : new Cache[0];
    }

    @Override
    public void put(@Nullable K key, @Nullable V value) {
        checkKey(key);
        checkValue(value);
        for (final Cache<? extends K, ? extends V> delegate : _delegates) {
            cast(delegate).put(key, value);
        }
    }

    @Override
    public void put(@Nullable K key, @Nullable V value, @Nullable Duration expireAfter) {
        checkKey(key);
        checkValue(value);
        for (final Cache<? extends K, ? extends V> delegate : _delegates) {
            cast(delegate).put(key, value, expireAfter);
        }
    }

    @Override
    public V get(@Nullable K key, @Nullable ValueProducer<K, V> cacheValueProducer, @Nullable Duration expireAfter) {
        checkKey(key);
        V result = null;
        final Set<Cache<K, V>> missedOn = new HashSet<>(_delegates.length);
        for (final Cache<? extends K, ? extends V> delegate : _delegates) {
            final Cache<K, V> cache = cast(delegate);
            result = cache.get(key);
            if (result != null) {
                break;
            } else {
                missedOn.add(cache);
            }
        }
        if (result == null && cacheValueProducer != null) {
            result = new Lazy<>(key, cacheValueProducer, nonBlocking).getValue();
        }
        if (result != null) {
            for (final Cache<K, V> cache : missedOn) {
                cache.put(key, result, expireAfter);
            }
        }
        return result;
    }

    @Override
    public V get(@Nullable K key, @Nullable ValueProducer<K, V> cacheValueProducer) {
        return get(key, cacheValueProducer, null);
    }

    @Override
    public V get(@Nullable K key) {
        return get(key, null);
    }

    @Override
    public Value<V> remove(@Nullable K key) {
        checkKey(key);
        Value<V> result = null;
        for (final Cache<? extends K, ? extends V> delegate : _delegates) {
            final Value<V> removed = cast(delegate).remove(key);
            if (removed != null) {
                result = removed;
            }
        }
        return result;
    }

    @Override
    public boolean contains(@Nullable K key) {
        checkKey(key);
        boolean result = false;
        for (final Cache<? extends K, ? extends V> delegate : _delegates) {
            if (cast(delegate).contains(key)) {
                result = true;
                break;
            }
        }
        return result;
    }

    @Nonnull
    protected Cache<K, V> cast(Cache<? extends K, ? extends V> delegate) {
        // noinspection unchecked
        return (Cache<K, V>) delegate;
    }

    @Nonnull
    public Cache<K, V>[] getDelegates() {
        // noinspection unchecked
        return (Cache<K, V>[]) _delegates;
    }
}

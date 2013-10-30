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

import org.echocat.jomon.runtime.util.ProducingType;
import org.echocat.jomon.runtime.util.Value;
import org.echocat.jomon.runtime.util.Value.Fixed;
import org.echocat.jomon.runtime.util.Value.Lazy;
import org.echocat.jomon.runtime.util.ValueProducer;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import static java.lang.System.currentTimeMillis;

@NotThreadSafe
public class CacheEntry<K, V> {

    @Nonnull
    private final K _key;
    @Nullable
    private Value<V> _value;
    @Nonnegative
    private long _created;
    @Nullable
    private Long _expireAt;
    @Nonnegative
    private long _lastAccessed;
    @Nonnegative
    private int _hits;
    @Nullable
    private CacheEntry<K, V> _next;
    @Nullable
    private CacheEntry<K, V> _previous;

    public CacheEntry(@Nullable K key, @Nullable Long expireAfter, @Nullable Value<V> value) {
        _key = key;
        setValue(expireAfter, value);
    }

    public CacheEntry(@Nullable K key, @Nullable Long expireAfter, @Nullable V value) {
        _key = key;
        setValue(expireAfter, value);
    }

    public CacheEntry(@Nullable K key, @Nullable Long expireAfter, @Nullable ValueProducer<K, V> producer, @Nonnull ProducingType producingType) {
        _key = key;
        setValue(expireAfter, producer, producingType);
    }

    @Nullable
    public K getKey() {
        return _key;
    }

    public void setValue(@Nullable Long expireAfter, @Nullable V value) {
        setValue(expireAfter, new Fixed<>(value));
    }

    public void setValue(@Nullable Long expireAfter, @Nullable ValueProducer<K, V> producer, @Nonnull ProducingType producingType) {
        setValue(expireAfter, producer != null ? new Lazy<>(_key, producer, producingType) : new Fixed<V>(null));
    }

    protected void setValue(@Nullable Long expireAfter, @Nonnull Value<V> entry) {
        _value = entry;
        _created = currentTimeMillis();
        _lastAccessed = _created;
        _expireAt = expireAfter != null ? _created + expireAfter : null;
    }

    @Nullable
    public Value<V> getValue() {
        return _value;
    }

    public void setPrevious(@Nullable CacheEntry<K, V> previous) {
        _previous = previous;
    }

    @Nullable
    public CacheEntry<K, V> getPrevious() {
        return _previous;
    }

    public void setNext(@Nullable CacheEntry<K, V> next) {
        _next = next;
    }

    @Nullable
    public CacheEntry<K, V> getNext() {
        return _next;
    }

    @Nonnegative
    public long getCreated() {
        return _created;
    }

    @Nullable
    public Long getExpire() {
        return _expireAt;
    }

    @Nonnegative
    public long getLastAccessed() {
        return _lastAccessed;
    }

    @Nonnegative
    public int getHits() {
        return _hits;
    }

    public void hit() {
        _lastAccessed = currentTimeMillis();
        _hits++;
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else if (!(o instanceof CacheEntry)) {
            result = false;
        } else {
            final Object thisKey = getKey();
            final Object thatKey = ((CacheEntry<?, ?>)o).getKey();
            result = thisKey != null ? thisKey.equals(thatKey) : thatKey == null;
        }
        return result;
    }

    @Override
    public int hashCode() {
        final Object thisKey = getKey();
        return thisKey != null ? thisKey.hashCode() : 0;
    }

    @Override
    public String toString() {
        return getKey() + "=>" + getValue();
    }
}

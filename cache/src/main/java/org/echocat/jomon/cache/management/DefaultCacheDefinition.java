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

package org.echocat.jomon.cache.management;

import org.echocat.jomon.cache.*;
import org.echocat.jomon.runtime.util.ProducingType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DefaultCacheDefinition<K, V> extends LimitedCacheDefinition<K, V, DefaultCacheDefinition<K, V>> {

    private ProducingType _producingType;

    @Nonnull
    public static <K, V> DefaultCacheDefinition<K, V> cache(@Nonnull Class<? extends LimitedCache<?, ?>> type, @Nonnull Class<K> keyType, @Nonnull Class<V> valueType) {
        return new DefaultCacheDefinition<>(type, keyType, valueType);
    }

    @Nonnull
    public static <K, V> DefaultCacheDefinition<K, V> lruCache(@Nonnull Class<K> keyType, @Nonnull Class<V> valueType) {
        // noinspection unchecked, RedundantCast
        return cache((Class<? extends LimitedCache<?, ?>>)(Object)LruCache.class, keyType, valueType);
    }

    @Nonnull
    public static <K, V> DefaultCacheDefinition<K, V> lfuCache(@Nonnull Class<K> keyType, @Nonnull Class<V> valueType) {
        // noinspection unchecked, RedundantCast
        return cache((Class<? extends LimitedCache<?, ?>>)(Object)LfuCache.class, keyType, valueType);
    }

    @Nonnull
    public static <K, V> DefaultCacheDefinition<K, V> fifoCache(@Nonnull Class<K> keyType, @Nonnull Class<V> valueType) {
        // noinspection unchecked, RedundantCast
        return cache((Class<? extends LimitedCache<?, ?>>)(Object)FifoCache.class, keyType, valueType);
    }

    public DefaultCacheDefinition(@Nonnull Class<? extends Cache<?, ?>> requiredType, @Nonnull Class<K> keyType, @Nonnull Class<V> valueType) {
        super(requiredType, keyType, valueType);
    }

    @Nullable
    public ProducingType getProducingType() {
        return _producingType;
    }

    public void setProducingType(@Nullable ProducingType producingType) {
        _producingType = producingType;
    }

    @Nonnull
    public DefaultCacheDefinition<K, V> withProducingType(@Nonnull ProducingType producingType) {
        if (_producingType != null) {
            throw new IllegalStateException("ProducingType already set.");
        }
        _producingType = producingType;
        return thisInstance();
    }
}

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

import org.echocat.jomon.cache.Cache;
import org.echocat.jomon.cache.ServletRequestBasedCache;
import org.echocat.jomon.cache.ServletRequestBasedCache.Resolver;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ServletRequestBasedCacheDefinition<K, V> extends CacheDefinition<K, V, ServletRequestBasedCacheDefinition<K, V>> {

    @Nonnull
    public static <K,V> ServletRequestBasedCacheDefinition<K, V> servletRequestBasedCache(@Nonnull Class<K> keyType, @Nonnull Class<V> valueType, @Nonnull Resolver resolver, @Nullable CacheDefinition<? extends K, ? extends V, ?> delegate) {
        return new ServletRequestBasedCacheDefinition<>(keyType, valueType, resolver, delegate);
    }

    @Nonnull
    public static <K,V> ServletRequestBasedCacheDefinition<K, V> servletRequestBasedCache(@Nonnull Class<K> keyType, @Nonnull Class<V> valueType, @Nonnull Resolver resolver) {
        return servletRequestBasedCache(keyType, valueType, resolver, null);
    }

    @Nonnull
    public static <K,V> ServletRequestBasedCacheDefinition<K, V> servletRequestBasedCacheOf(@Nonnull Class<K> keyType, @Nonnull Class<V> valueType, @Nonnull Resolver resolver, @Nullable CacheDefinition<? extends K, ? extends V, ?> delegate) {
        return servletRequestBasedCache(keyType, valueType, resolver, delegate);
    }

    @Nonnull
    public static <K,V> ServletRequestBasedCacheDefinition<K, V> servletRequestBasedCacheOf(@Nonnull Class<K> keyType, @Nonnull Class<V> valueType, @Nonnull Resolver resolver) {
        return servletRequestBasedCache(keyType, valueType, resolver);
    }

    private final Resolver _resolver;
    private final CacheDefinition<? extends K, ? extends V, ?> _delegate;

    public ServletRequestBasedCacheDefinition(@Nonnull Class<K> keyType, @Nonnull Class<V> valueType, @Nonnull Resolver resolver, @Nullable CacheDefinition<? extends K, ? extends V, ?> delegate) {
        // noinspection unchecked
        super((Class<? extends Cache<?, ?>>) (Class) ServletRequestBasedCache.class, keyType, valueType);
        _resolver = resolver;
        _delegate = delegate;
    }

    @Nonnull
    public Resolver getResolver() {
        return _resolver;
    }

    @Nullable
    public CacheDefinition<? extends K, ? extends V, ?> getDelegate() {
        return _delegate;
    }
}

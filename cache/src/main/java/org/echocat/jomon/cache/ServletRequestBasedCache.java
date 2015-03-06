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
import org.echocat.jomon.cache.management.CacheCreator;
import org.echocat.jomon.cache.management.CacheDefinition;
import org.echocat.jomon.runtime.util.Duration;
import org.echocat.jomon.runtime.util.ProducingType;
import org.echocat.jomon.runtime.util.ValueProducer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletRequest;

import static org.echocat.jomon.cache.management.DefaultCacheDefinition.lruCache;

public class ServletRequestBasedCache<K, V> extends CacheSupport<K, V> implements IdentifiedCache<K, V> {

    private final CacheCreator _cacheCreator;
    private final Resolver _resolver;
    private final CacheDefinition<? extends K, ? extends V, ?> _delegateDefinition;

    private String _id;

    public ServletRequestBasedCache(@Nonnull Class<? extends K> keyType, @Nonnull Class<? extends V> valueType, @Nonnull CacheCreator cacheCreator, @Nonnull Resolver resolver, @Nullable CacheDefinition<? extends K, ? extends V, ?> delegateDefinition) {
        super(keyType, valueType);
        _cacheCreator = cacheCreator;
        _resolver = resolver;
        _delegateDefinition = delegateDefinition != null ? delegateDefinition : lruCache(keyType, valueType);
    }

    public ServletRequestBasedCache(@Nonnull Class<? extends K> keyType, @Nonnull Class<? extends V> valueType, @Nonnull CacheCreator cacheCreator, @Nonnull Resolver resolver) {
        this(keyType, valueType, cacheCreator, resolver, null);
    }

    @Override
    public void put(@Nullable K key, @Nullable V value) {
        final Cache<K, V> cache = findRequestBasedCache();
        if (cache != null) {
            cache.put(key, value);
        }
    }

    @Override
    public void put(@Nullable K key, @Nullable V value, @Nullable Duration expireAfter) {
        final Cache<K, V> cache = findRequestBasedCache();
        if (cache != null) {
            cache.put(key, value, expireAfter);
        }
    }

    @Override
    @Nullable
    public V get(@Nullable K key) {
        final Cache<K, V> cache = findRequestBasedCache();
        return cache != null ? cache.get(key) : null;
    }

    @Override
    @Nullable
    public V get(@Nullable K key, @Nullable ValueProducer<K, V> cacheValueProducer, @Nullable Duration expireAfter) {
        final Cache<K, V> cache = findRequestBasedCache();
        return cache != null ? cache.get(key, cacheValueProducer, expireAfter) : new Lazy<>(key, cacheValueProducer, ProducingType.blocking).getValue();
    }

    @Override
    @Nullable
    public V get(@Nullable K key, @Nullable ValueProducer<K, V> cacheValueProducer) {
        final Cache<K, V> cache = findRequestBasedCache();
        return cache != null ? cache.get(key, cacheValueProducer) : new Lazy<>(key, cacheValueProducer, ProducingType.blocking).getValue();
    }

    @Override
    @Nullable
    public Value<V> remove(@Nullable K key) {
        final Cache<K, V> cache = findRequestBasedCache();
        return cache != null ? cache.remove(key) : null;
    }

    @Override
    public boolean contains(@Nullable K key) {
        final Cache<K, V> cache = findRequestBasedCache();
        return cache != null && cache.contains(key);
    }

    @Nullable
    protected Cache<K, V> findRequestBasedCache() {
        final ServletRequest request = _resolver.resolve(this);
        final Cache<K, V> result;
        if (request != null) {
            result = findCacheAt(request);
        } else {
            result = null;
        }
        return result;

    }

    @Nullable
    protected Cache<K, V> findCacheAt(ServletRequest request) {
        final String requestCacheAttributeName = getRequestCacheAttributeName();
        final Object plainCache = request.getAttribute(requestCacheAttributeName);
        final Cache<K, V> result;
        if (plainCache == null) {
            result = checkedCreation();
            request.setAttribute(requestCacheAttributeName, result);
        } else if (plainCache instanceof Cache) {
            result = checkedCast(plainCache);
        } else {
            throw new IllegalStateException("The request attribute " + requestCacheAttributeName + " contains not a cache: " + plainCache);
        }
        return result;
    }

    @Nonnull
    protected Cache<K, V> checkedCreation() {
        try {
            //noinspection unchecked
            return (Cache<K, V>) _cacheCreator.create(null, _cacheCreator, _delegateDefinition);
        } catch (final Exception e) {
            throw new RuntimeException("Could not create cache for request with " + _delegateDefinition + ".", e);
        }
    }

    @Nonnull
    protected Cache<K, V> checkedCast(@Nonnull Object plainCache) {
        // noinspection unchecked
        final Cache<K, V> result = (Cache<K, V>) plainCache;
        if (!getKeyType().isAssignableFrom(result.getKeyType()) || !getValueType().isAssignableFrom(result.getValueType())) {
            throw new IllegalStateException("The request attribute " + getRequestCacheAttributeName() + " contains in invalid cache: " + result);
        }
        return result;
    }

    @Nonnull
    public String getRequestCacheAttributeName() {
        return (_id != null ? getClass().getName() + "." + _id : getClass().getName());
    }

    @Override
    public String getId() {
        return _id;
    }

    @Override
    public void setId(String id) {
        _id = id;
    }

    @Nonnull
    public CacheCreator getCacheCreator() {
        return _cacheCreator;
    }

    @Nonnull
    public Resolver getResolver() {
        return _resolver;
    }

    @Nonnull
    public CacheDefinition<? extends K, ? extends V, ?> getDelegateDefinition() {
        return _delegateDefinition;
    }

    public interface Resolver {

        @Nullable
        public ServletRequest resolve(@Nonnull ServletRequestBasedCache<?, ?> forCache);

    }

}

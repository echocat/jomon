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

package org.echocat.jomon.cache.management;

import org.echocat.jomon.cache.*;
import org.echocat.jomon.runtime.iterators.ConvertingIterator;
import org.echocat.jomon.runtime.util.Entry;
import org.echocat.jomon.runtime.util.Entry.Impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PreDestroy;
import java.util.*;

import static org.echocat.jomon.cache.CacheUtils.assertValidCacheId;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietlyIfAutoCloseable;

public class DefaultCacheRepository implements CacheRepository, AutoCloseable {

    private final Map<String, Cache<?, ?>> _idToCache = new HashMap<>();

    private final CacheCreator _cacheCreator;

    private CacheDefinition<?, ?, ?> _defaultDefinition;
    private Map<String, CacheDefinition<?, ?, ?>> _overwrittenCacheDefinitions;
    private Collection<CacheListener> _listeners;

    public DefaultCacheRepository(@Nonnull CacheCreator cacheCreator) {
        _cacheCreator = cacheCreator;
    }

    @Nullable
    public CacheDefinition<?, ?, ?> getDefaultDefinition() {
        return _defaultDefinition;
    }

    public void setDefaultDefinition(@Nullable CacheDefinition<?, ?, ?> defaultDefinition) {
        _defaultDefinition = defaultDefinition;
    }

    @Nullable
    public Map<String, CacheDefinition<?, ?, ?>> getOverwrittenCacheDefinitions() {
        return _overwrittenCacheDefinitions;
    }

    public void setOverwrittenCacheDefinitions(@Nullable Map<String, CacheDefinition<?, ?, ?>> overwrittenCacheDefinitions) {
        _overwrittenCacheDefinitions = overwrittenCacheDefinitions;
    }

    @Nullable
    public Collection<CacheListener> getListeners() {
        return _listeners;
    }

    public void setListeners(@Nullable Collection<CacheListener> listeners) {
        _listeners = listeners;
    }

    @Nonnull
    @Override
    public <K, V> Cache<K, V> provide(@Nonnull Class<?> forType, @Nullable CacheDefinition<K, V, ?> defaultDefinition) throws IllegalCacheDefinitionException {
        return provide(forType, null, defaultDefinition);
    }

    @Nonnull
    @Override
    public <K, V> Cache<K, V> provide(@Nonnull Class<?> forType, @Nullable String variant, @Nullable CacheDefinition<K, V, ?> defaultDefinition) throws IllegalCacheDefinitionException {
        final String id;
        if (variant != null) {
            id = forType.getName() + "." + variant;
        } else {
            id = forType.getName();
        }
        return provide(id, defaultDefinition);
    }

    @Nonnull
    @Override
    public <K, V> Cache<K, V> provide(@Nonnull String id, @Nullable CacheDefinition<K, V, ?> defaultDefinition) {
        assertValidCacheId(id);
        synchronized (_idToCache) {
            Cache<?, ?> cache = _idToCache.get(id);
            if (cache == null) {
                final CacheDefinition<K, V, ?> definition = selectDefinitionBy(id, defaultDefinition);
                cache = create(id, definition);
                _idToCache.put(id, cache);
            }
            // noinspection unchecked
            return (Cache<K, V>) cache;
        }
    }

    @Override
    @Nullable
    public <K, V> Cache<K, V> find(@Nonnull String id) {
        synchronized (_idToCache) {
            // noinspection unchecked
            return (Cache<K, V>) _idToCache.get(id);
        }
    }

    @Override
    public void clear() {
        synchronized (_idToCache) {
            for (Cache<?, ?> cache : _idToCache.values()) {
                if (cache instanceof ClearableCache) {
                    ((ClearableCache) cache).clear();
                }
            }
        }
    }

    @Override
    public void remove(@Nonnull String id) {
        synchronized (_idToCache) {
            try {
                final Cache<?, ?> cache = _idToCache.get(id);
                if (cache != null) {
                    if (beforeDestroy(id, cache)) {
                        if (cache instanceof ClearableCache) {
                            ((ClearableCache<?, ?>) cache).clear();
                        }
                        _idToCache.remove(id);
                        afterDestroy(id, cache);
                    } else {
                        _idToCache.remove(id);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Could not remove cache '" + id + "'.", e);
            }
        }
    }

    @Override
    @Nonnull
    public Iterator<Entry<String, Cache<?, ?>>> iterator() {
        final Set<Map.Entry<String, Cache<?, ?>>> idToCache;
        synchronized (_idToCache) {
            idToCache = new HashSet<>(_idToCache.entrySet());
        }
        return new ConvertingIterator<Map.Entry<String, Cache<?, ?>>, Entry<String, Cache<?,?>>>(idToCache.iterator()) { @Override protected Entry<String, Cache<?, ?>> convert(Map.Entry<String, Cache<?, ?>> input) {
            return new Impl<String, Cache<?, ?>>(input.getKey(), input.getValue());
        }};
    }

    @Override
    @PreDestroy
    public void close() throws Exception {
        synchronized (_idToCache) {
            final Iterator<Map.Entry<String,Cache<?,?>>> i = _idToCache.entrySet().iterator();
            while (i.hasNext()) {
                final Map.Entry<String,Cache<?,?>> entry = i.next();
                final String id = entry.getKey();
                final Cache<?, ?> cache = entry.getValue();
                try {
                    if (beforeDestroy(id, cache)) {
                        closeQuietlyIfAutoCloseable(cache);
                        i.remove();
                        afterDestroy(id, cache);
                    } else {
                        i.remove();
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Could not remove cache '" + id + "'.", e);
                }
            }
        }
    }

    @Nonnull
    protected <K, V> CacheDefinition<K, V, ?> selectDefinitionBy(@Nonnull String id, @Nullable CacheDefinition<K, V, ?> specifiedDefaultDefinition) {
        final Map<String, CacheDefinition<?, ?, ?>> overwrittenCacheDefinitions = _overwrittenCacheDefinitions;
        CacheDefinition<?, ?, ?> result = overwrittenCacheDefinitions != null ? overwrittenCacheDefinitions.get(id) : null;
        if (result == null) {
            result = specifiedDefaultDefinition;
        }
        if (result == null) {
            result = _defaultDefinition;
        }
        if (result == null) {
            throw new IllegalCacheDefinitionException("There could no cache definition be selected for '" + id + "'.");
        }
        //noinspection unchecked
        return (CacheDefinition<K, V, ?>) result;
    }

    @Nonnull
    protected <K, V> Cache<K, V> create(@Nonnull String id, @Nonnull CacheDefinition<K, V, ?> definition) {
        final Cache<K, V> cache;
        try {
            if (_cacheCreator.canHandleType(definition)) {
                if (!beforeCreate(id, definition)) {
                // noinspection ThrowCaughtLocally
                    throw new IllegalCacheDefinitionException("Listener denied creation of cache by " + definition + ".");
                }
                cache = _cacheCreator.create(this, _cacheCreator, definition);
                if (cache instanceof IdentifiedCache) {
                    ((IdentifiedCache<K, V>)cache).setId(id);
                }
                if (cache instanceof ListenerEnabledCache) {
                    ((ListenerEnabledCache<K, V>)cache).setListeners(_listeners);
                }
                afterCreate(id, definition, cache);
            } else {
                // noinspection ThrowCaughtLocally
                throw new IllegalCacheDefinitionException("Could not provide a cache by " + definition + ".");
            }
            return cache;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Could not create cache '" + id + "' by " + definition + ".", e);
        }
    }

    protected <K, V> boolean beforeCreate(@Nonnull String id, @Nonnull CacheDefinition<K, V, ?> definition) throws Exception {
        final Iterable<CacheListener> listeners = _listeners;
        boolean result = true;
        if (listeners != null) {
            for (CacheListener listener : listeners) {
                if (listener instanceof CreationCacheListener) {
                    if (!((CreationCacheListener)listener).beforeCreate(id, definition)) {
                        result = false;
                        break;
                    }
                }
            }
        }
        return result;
    }

    protected <K, V> void afterCreate(@Nonnull String id, @Nonnull CacheDefinition<K, V, ?> definition, @Nonnull Cache<?, ?> cache) {
        final Iterable<CacheListener> listeners = _listeners;
        if (listeners != null) {
            for (CacheListener listener : listeners) {
                if (listener instanceof CreationCacheListener) {
                    ((CreationCacheListener)listener).afterCreate(id, definition, cache);
                }
            }
        }
    }

    protected <K, V> boolean beforeDestroy(@Nonnull String id, @Nonnull Cache<K, V> cache) throws Exception {
        final Iterable<CacheListener> listeners = _listeners;
        boolean result = true;
        if (listeners != null) {
            for (CacheListener listener : listeners) {
                if (listener instanceof CreationCacheListener) {
                    if (!((DestroyCacheListener)listener).beforeDestroy(id, cache)) {
                        result = false;
                        break;
                    }
                }
            }
        }
        return result;
    }

    protected <K, V> void afterDestroy(@Nonnull String id, @Nonnull Cache<K, V> cache) throws Exception {
        final Iterable<CacheListener> listeners = _listeners;
        if (listeners != null) {
            for (CacheListener listener : listeners) {
                if (listener instanceof DestroyCacheListener) {
                    ((DestroyCacheListener)listener).afterDestroy(id, cache);
                }
            }
        }
    }

}

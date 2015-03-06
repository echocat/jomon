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

import org.echocat.jomon.cache.Cache;
import org.echocat.jomon.cache.CombinedCache;
import org.echocat.jomon.cache.management.CombinedCacheDefinition.IdToDefinition;
import org.echocat.jomon.cache.management.CombinedCacheDefinition.IdentifiersToDefinition;
import org.echocat.jomon.cache.management.CombinedCacheDefinition.TypeAndVariantToDefinition;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static java.util.Arrays.asList;

public class CombinedCacheCreator implements CacheCreator {

    private static final Iterable<CacheCreator> DEFAULT_DELEGATES = asList(
        new LruCacheCreator(),
        new LfuCacheCreator(),
        new FifoCacheCreator(),
        new ServletRequestBasedCacheCreator()
    );

    private Iterable<CacheCreator> _delegates;

    public Iterable<CacheCreator> getDelegates() {
        return _delegates;
    }

    public void setDelegates(Iterable<CacheCreator> delegates) {
        _delegates = delegates;
    }

    @Override
    public boolean canHandleType(@Nonnull CacheDefinition<?, ?, ?> by) throws Exception {
        return by instanceof CombinedCacheDefinition || findDelegateFor(by) != null;
    }

    @Nonnull
    @Override
    public <K, V> Cache<K, V> create(@Nullable CacheProvider provider, @Nonnull CacheCreator master, @Nonnull CacheDefinition<K, V, ?> by) throws Exception {
        final Cache<K, V> cache;
        if (by instanceof CombinedCacheDefinition) {
            // noinspection unchecked
            cache = createCombined(provider, master, (CombinedCacheDefinition<K, V>)by);
        } else {
            final CacheCreator creator = findDelegateFor(by);
            if (creator == null) {
                throw new IllegalArgumentException("Could not handle definition " + by + ".");
            }
            cache = creator.create(provider, master, by);
        }
        return cache;
    }

    @Nonnull
    protected <K, V> CombinedCache<K, V> createCombined(@Nullable CacheProvider provider, @Nonnull CacheCreator master, @Nonnull CombinedCacheDefinition<K, V> by) throws Exception {
        final List<IdentifiersToDefinition<? extends K,? extends V>> definitions = by.getDelegates();
        // noinspection unchecked
        final Cache<K, V>[] delegates = new Cache[definitions != null ? definitions.size() : 0];
        if (definitions != null) {
            int i = 0;
            for (final IdentifiersToDefinition<? extends K, ? extends V> definition : definitions) {
                if (provider == null) {
                    // noinspection unchecked
                    delegates[i++] = create(null, master, (CacheDefinition<K, V, ?>) definition.getDefinition());
                } else if (definition instanceof IdToDefinition) {
                    // noinspection unchecked
                    delegates[i++] = provider.provide(((IdToDefinition) definition).getId(), (CacheDefinition<K, V, ?>) definition.getDefinition());
                } else if (definition instanceof TypeAndVariantToDefinition) {
                    // noinspection unchecked
                    delegates[i++] = provider.provide(((TypeAndVariantToDefinition) definition).getForType(), ((TypeAndVariantToDefinition) definition).getVariant(), (CacheDefinition<K, V, ?>) definition.getDefinition());
                } else {
                    throw new IllegalArgumentException();
                }
            }
        }
        return new CombinedCache<>(by.getKeyType(), by.getValueType(), delegates);
    }

    @Nullable
    protected <K, V> CacheCreator findDelegateFor(@Nonnull CacheDefinition<K, V, ?> by) throws Exception{
        final Iterable<CacheCreator> delegates = _delegates;
        CacheCreator result = null;
        if (delegates != null) {
            for (final CacheCreator delegate : delegates) {
                if (delegate.canHandleType(by)) {
                    result = delegate;
                    break;
                }
            }
        }
        if (result == null) {
            for (final CacheCreator delegate : DEFAULT_DELEGATES) {
                if (delegate.canHandleType(by)) {
                    result = delegate;
                    break;
                }
            }
        }
        return result;
    }

}

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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class CombinedCacheDefinition<K, V> extends CacheDefinition<K, V, CombinedCacheDefinition<K, V>> {

    @Nonnull
    public static <K,V> CombinedCacheDefinition<K, V> combinedCache(@Nonnull Class<K> keyType, @Nonnull Class<V> valueType, @Nonnull IdentifiersToDefinition<? extends K, ? extends V>... delegates) {
        final CombinedCacheDefinition<K, V> definition = new CombinedCacheDefinition<>(keyType, valueType);
        definition.withDelegates(delegates);
        return definition;
    }

    @Nonnull
    public static <K,V> CombinedCacheDefinition<K, V> combinedCacheOf(@Nonnull Class<K> keyType, @Nonnull Class<V> valueType, @Nonnull IdentifiersToDefinition<? extends K, ? extends V>... delegates) {
        return combinedCache(keyType, valueType, delegates);
    }

    @Nonnull
    public static <K, V> TypeAndVariantToDefinition<K, V> with(@Nonnull Class<?> forType, @Nonnull CacheDefinition<K, V, ?> definition) {
        return with(forType, null, definition);
    }

    @Nonnull
    public static <K, V> TypeAndVariantToDefinition<K, V> with(@Nonnull Class<?> forType, @Nullable String variant, @Nonnull CacheDefinition<K, V, ?> definition) {
        return new TypeAndVariantToDefinition<>(forType, variant, definition);
    }

    @Nonnull
    public static <K, V> IdToDefinition<K, V> with(@Nonnull String id, @Nonnull CacheDefinition<K, V, ?> definition) {
        return new IdToDefinition<>(id, definition);
    }

    private List<IdentifiersToDefinition<? extends K, ? extends V>> _delegates;

    public CombinedCacheDefinition(@Nonnull Class<K> keyType, @Nonnull Class<V> valueType) {
        // noinspection unchecked
        super((Class<? extends Cache<?, ?>>) (Class) CombinedCache.class, keyType, valueType);
    }

    public List<IdentifiersToDefinition<? extends K, ? extends V>> getDelegates() {
        return _delegates;
    }

    public void setDelegates(List<IdentifiersToDefinition<? extends K, ? extends V>> delegates) {
        _delegates = delegates != null ? new ArrayList<>(delegates) : null;
    }

    @Nonnull
    public CombinedCacheDefinition<K, V> withDelegate(@Nonnull IdentifiersToDefinition<? extends K, ? extends V> delegate) {
        return withDelegates(delegate);
    }

    @Nonnull
    public CombinedCacheDefinition<K, V> withDelegates(@Nonnull IdentifiersToDefinition<? extends K, ? extends V>... delegates) {
        if (_delegates == null) {
            _delegates = new ArrayList<>();
        }
        _delegates.addAll(asList(delegates));
        return thisInstance();
    }

    public abstract static class IdentifiersToDefinition<K, V> {

        private final CacheDefinition<K, V, ?> _definition;

        public IdentifiersToDefinition(@Nonnull CacheDefinition<K, V, ?> definition) {
            _definition = definition;
        }

        @Nonnull
        public CacheDefinition<K, V, ?> getDefinition() {
            return _definition;
        }
    }

    public static class IdToDefinition<K, V> extends IdentifiersToDefinition<K, V> {

        private final String _id;

        public IdToDefinition(@Nonnull String id, @Nonnull CacheDefinition<K, V, ?> definition) {
            super(definition);
            _id = id;
        }

        @Nonnull
        public String getId() {
            return _id;
        }
    }

    public static class TypeAndVariantToDefinition<K, V> extends IdentifiersToDefinition<K, V> {

        private final Class<?> _forType;
        private final String _variant;

        public TypeAndVariantToDefinition(@Nonnull Class<?> forType, @Nullable String variant, @Nonnull CacheDefinition<K, V, ?> definition) {
            super(definition);
            _forType = forType;
            _variant = variant;
        }

        @Nonnull
        public Class<?> getForType() {
            return _forType;
        }

        @Nullable
        public String getVariant() {
            return _variant;
        }
    }
}

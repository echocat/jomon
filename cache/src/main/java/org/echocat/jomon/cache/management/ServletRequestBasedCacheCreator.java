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
import org.echocat.jomon.cache.ServletRequestBasedCache;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ServletRequestBasedCacheCreator implements CacheCreator {

    @Override
    public boolean canHandleType(@Nonnull CacheDefinition<?, ?, ?> by) throws Exception {
        return by instanceof ServletRequestBasedCacheDefinition;
    }

    @Nonnull
    @Override
    public <K, V> Cache<K, V> create(@Nullable CacheProvider provider, @Nonnull CacheCreator master, @Nonnull CacheDefinition<K, V, ?> by) throws Exception {
        if (!canHandleType(by)) {
            throw new IllegalArgumentException("Could not handle " + by + ".");
        }
        // noinspection unchecked
        final ServletRequestBasedCacheDefinition<K, V> definition = (ServletRequestBasedCacheDefinition<K, V>) by;
        return new ServletRequestBasedCache<>(by.getKeyType(), by.getValueType(), master, definition.getResolver(), definition.getDelegate());
    }

}

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
import org.echocat.jomon.cache.LimitedCache;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class LimitedCacheCreatorSupport extends CacheCreatorSupport {

    @Override
    @Nonnull
    public <K, V> Cache<K, V> create(@Nullable CacheProvider cacheProvider, @Nonnull CacheCreator master, @Nonnull CacheDefinition<K, V, ?> by) throws Exception {
        final LimitedCache<K, V> result = newInstance(by);
        if (by instanceof LimitedCacheDefinition) {
            // noinspection unchecked
            final LimitedCacheDefinition<K, V, ?> limitedDefinition = (LimitedCacheDefinition<K, V, ?>) by;
            result.setCapacity(limitedDefinition.getCapacity());
            result.setMaximumLifetime(limitedDefinition.getMaximumLifetime());
        }
        return result;
    }

    @Nonnull
    protected abstract <K, V> LimitedCache<K, V> newInstance(@Nonnull CacheDefinition<K, V, ?> by) throws Exception;

}

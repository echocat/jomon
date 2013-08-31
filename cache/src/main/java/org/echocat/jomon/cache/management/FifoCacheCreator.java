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
import org.echocat.jomon.cache.FifoCache;
import org.echocat.jomon.cache.LimitedCache;

import javax.annotation.Nonnull;

public class FifoCacheCreator extends DefaultCacheCreatorSupport {

    @Override
    public boolean canHandleType(@Nonnull Class<? extends Cache<?, ?>> type) throws Exception {
        return FifoCache.class.isAssignableFrom(type);
    }

    @Nonnull
    @Override
    protected <K, V> LimitedCache<K, V> newInstance(@Nonnull CacheDefinition<K, V, ?> by) throws Exception {
        return new FifoCache<>(by.getKeyType(), by.getValueType());
    }
}

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
import org.echocat.jomon.cache.ProducingTypeEnabledCache;
import org.echocat.jomon.runtime.util.ProducingType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class DefaultCacheCreatorSupport extends LimitedCacheCreatorSupport {

    private ProducingType _defaultProducingType;

    @Nullable
    public ProducingType getDefaultProducingType() {
        return _defaultProducingType;
    }

    public void setDefaultProducingType(@Nullable ProducingType defaultProducingType) {
        _defaultProducingType = defaultProducingType;
    }

    @Nonnull
    @Override
    public <K, V> Cache<K, V> create(@Nullable CacheProvider cacheProvider, @Nonnull CacheCreator master, @Nonnull CacheDefinition<K, V, ?> by) throws Exception {
        final Cache<K, V> result = super.create(cacheProvider, master, by);
        if (result instanceof ProducingTypeEnabledCache && by instanceof DefaultCacheDefinition) {
            final ProducingType producingType = ((DefaultCacheDefinition) by).getProducingType();
            if (producingType != null) {
                ((ProducingTypeEnabledCache) result).setProducingType(producingType);
            } else if (_defaultProducingType != null) {
                ((ProducingTypeEnabledCache) result).setProducingType(_defaultProducingType);
            }
        }
        return result;
    }
}

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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface CacheCreator {

    public boolean canHandleType(@Nonnull CacheDefinition<?, ?, ?> by) throws Exception;

    @Nonnull
    public <K, V> Cache<K, V> create(@Nullable CacheProvider provider, @Nonnull CacheCreator master, @Nonnull CacheDefinition<K, V, ?> by) throws Exception;

}

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

public interface CacheProvider {

    @Nonnull
    public <K, V> Cache<K, V> provide(@Nonnull String id, @Nullable CacheDefinition<K, V, ?> defaultDefinition) throws IllegalCacheDefinitionException;

    @Nonnull
    public <K, V> Cache<K, V> provide(@Nonnull Class<?> forType, @Nullable String variant, @Nullable CacheDefinition<K, V, ?> defaultDefinition) throws IllegalCacheDefinitionException;

    @Nonnull
    public <K, V> Cache<K, V> provide(@Nonnull Class<?> forType, @Nullable CacheDefinition<K, V, ?> defaultDefinition) throws IllegalCacheDefinitionException;

}

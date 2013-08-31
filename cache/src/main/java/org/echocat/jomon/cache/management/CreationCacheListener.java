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
import org.echocat.jomon.cache.CacheListener;

import javax.annotation.Nonnull;

public interface CreationCacheListener extends CacheListener {

    public boolean beforeCreate(@Nonnull String id, @Nonnull CacheDefinition<?, ?, ?> definition);

    public void afterCreate(@Nonnull String id, @Nonnull CacheDefinition<?, ?, ?> definition, @Nonnull Cache<?, ?> cache);

}

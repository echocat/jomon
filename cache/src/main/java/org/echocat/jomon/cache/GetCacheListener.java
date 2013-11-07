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

package org.echocat.jomon.cache;

import org.echocat.jomon.runtime.util.Value;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface GetCacheListener extends CacheListener {

    public boolean beforeGet(@Nonnull Cache<?, ?> cache, @Nullable Object key);

    public void afterGet(@Nonnull Cache<?, ?> cache, @Nullable Object key, @Nullable Value<?> value);

}

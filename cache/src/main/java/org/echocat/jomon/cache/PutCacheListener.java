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

import org.echocat.jomon.runtime.util.Duration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface PutCacheListener extends CacheListener {

    public boolean beforePut(@Nonnull Cache<?, ?> cache, @Nullable Object key, @Nullable Value<?> value, @Nullable Duration expireAfter);

    public void afterPut(@Nonnull Cache<?, ?> cache, @Nullable Object key, @Nullable Value<?> value, @Nullable Duration expireAfter);

}

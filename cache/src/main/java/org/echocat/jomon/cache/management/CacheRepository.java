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
import org.echocat.jomon.runtime.util.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface CacheRepository extends CacheProvider, Iterable<Entry<String, Cache<?, ?>>> {

    @Nullable
    public <K, V> Cache<K, V> find(@Nullable String id);

    public void clear();

    public void remove(@Nonnull String id);


}

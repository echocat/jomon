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

package org.echocat.jomon.cache;

import org.echocat.jomon.runtime.util.Duration;

import javax.annotation.Nullable;

public interface LimitedCache<K, V> extends Cache<K, V> {

    public void setMaximumLifetime(@Nullable Duration millis);

    @Nullable
    public Duration getMaximumLifetime();

    public void setCapacity(@Nullable Long size);

    @Nullable
    public Long getCapacity();

}

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

import javax.annotation.Nullable;
import java.util.Date;

public interface StatisticsEnabledCache<K, V> extends Cache<K, V> {

    @Nullable
    public Long getNumberOfRequests();

    @Nullable
    public Long getNumberOfHits();

    @Nullable
    public Long getNumberOfDrops();

    @Nullable
    public Date getCreated();

    @Nullable
    public Long size();

    public void resetStatistics();
}

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

package org.echocat.jomon.net.cluster.channel;

import org.echocat.jomon.runtime.util.Duration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class StateCondition<C extends ClusterChannel<?, ?>> {

    private final Duration _maxWaitTime;

    public StateCondition(@Nonnull Duration maxWaitTime) {
        _maxWaitTime = maxWaitTime;
    }

    public abstract boolean check(@Nullable C clusterChannel) throws Exception;

    @Nonnull
    public Duration getMaxWaitTime() {
        return _maxWaitTime;
    }

}

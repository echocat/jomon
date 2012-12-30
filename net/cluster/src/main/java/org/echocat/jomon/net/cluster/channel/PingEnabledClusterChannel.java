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

package org.echocat.jomon.net.cluster.channel;

import org.echocat.jomon.runtime.util.Duration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Date;

public interface PingEnabledClusterChannel<ID, N extends Node<ID>> extends ClusterChannel<ID, N> {

    public void ping();

    @Nullable
    public Date getLastPingSendAt();

    @Nonnull
    public Duration getPingInterval();

    public void setPingInterval(@Nonnull Duration pingInterval);

}

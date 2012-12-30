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

package org.echocat.jomon.net.cluster.channel.tcp;

import org.echocat.jomon.net.cluster.channel.StatisticEnabledNode.Impl;

import javax.annotation.Nonnull;
import java.util.UUID;

public abstract class TcpNodeSupport extends Impl<UUID> implements TcpNode {

    private final UUID _id;

    public TcpNodeSupport(@Nonnull UUID id) {
        _id = id;
    }

    @Nonnull
    @Override
    public UUID getId() {
        return _id;
    }

    @Nonnull
    @Override
    public UUID getUuid() {
        return getId();
    }

}

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

package org.echocat.jomon.net.cluster.channel.multicast;

import org.echocat.jomon.net.cluster.channel.StatisticEnabledNode.Impl;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.net.InetSocketAddress;
import java.util.UUID;

public class MulticastNode extends Impl<Short> {

    private final short _id;
    private final UUID _uuid;
    private final InetSocketAddress _address;

    public MulticastNode(@Nonnegative short id, @Nonnull UUID uuid, @Nonnull InetSocketAddress address) {
        _id = id;
        _uuid = uuid;
        _address = address;
    }

    @Override
    public Short getId() {
        return _id;
    }

    @Nonnull
    @Override
    public UUID getUuid() {
        return _uuid;
    }

    @Override
    public InetSocketAddress getAddress() {
        return _address;
    }

}

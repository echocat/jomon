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

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;
import java.util.UUID;

public class LocalTcpNode extends TcpNodeSupport {

    private final InetSocketAddress _address;

    public LocalTcpNode(@Nonnull UUID id, @Nonnull InetSocketAddress address) {
        super(id);
        _address = address;
    }

    @Nonnull
    @Override
    public InetSocketAddress getAddress() {
        return _address;
    }
}

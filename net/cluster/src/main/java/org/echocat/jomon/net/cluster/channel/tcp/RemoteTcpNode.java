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

package org.echocat.jomon.net.cluster.channel.tcp;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.UUID;

import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

public abstract class RemoteTcpNode extends TcpNodeSupport implements Closeable {

    private final Socket _socket;

    public RemoteTcpNode(@Nonnull UUID id, @Nonnull Socket socket) throws IOException {
        super(id);
        _socket = socket;
    }

    @Nonnull
    @Override
    public InetSocketAddress getAddress() {
        return (InetSocketAddress) _socket.getRemoteSocketAddress();
    }

    @Override
    public void close() throws IOException {
        closeQuietly(_socket);
    }

    public boolean isConnected() {
        return _socket.isConnected() && !_socket.isClosed();
    }
}

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
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.UUID;

import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

public class InboundTcpNode extends RemoteTcpNode {

    private final InputStream _inputStream;

    public InboundTcpNode(@Nonnull UUID id, @Nonnull Socket socket, @Nonnull InputStream inputStream) throws IOException {
        super(id, socket);
        _inputStream = inputStream;
    }

    @Nonnull
    public InputStream getInputStream() {
        return _inputStream;
    }

    @Override
    public void close() throws IOException {
        try {
            closeQuietly(_inputStream);
        } finally {
            super.close();
        }
    }
}

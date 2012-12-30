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
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;

import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

public class OutboundTcpNode extends RemoteTcpNode {

    private final OutputStream _outputStream;
    private final Runnable _onClose;

    public OutboundTcpNode(@Nonnull UUID id, @Nonnull Socket socket, @Nonnull OutputStream outputStream) throws IOException {
        this(id, socket, outputStream, null);
    }

    public OutboundTcpNode(@Nonnull UUID id, @Nonnull Socket socket, @Nonnull OutputStream outputStream, @Nullable Runnable onClose) throws IOException {
        super(id, socket);
        _outputStream = outputStream;
        _onClose = onClose;
    }

    @Nonnull
    public OutputStream getOutputStream() {
        return _outputStream;
    }

    @Override
    public void close() throws IOException {
        try {
            if (_onClose != null) {
                _onClose.run();
            }
        } finally {
            try {
                closeQuietly(_outputStream);
            } finally {
                super.close();
            }
        }
    }
}

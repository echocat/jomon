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

import org.echocat.jomon.net.cluster.channel.ReceivedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.net.Socket;
import java.util.UUID;

import static org.echocat.jomon.net.cluster.channel.ByteUtils.*;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

public class InboundTcpWorker extends Thread implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(TcpClusterChannel.class);

    private final InboundTcpNode _node;
    private final InputStream _is;
    private final Reader _reader;

    public InboundTcpWorker(@Nonnull Reader reader, @Nonnull Socket socket, @Nonnull UUID serverUuid, @Nonnull String service, @Nullable String name) throws IOException {
        setName("InboundTcp(" + service + "/" + (name != null ? name : serverUuid) + ")<(resolving)");
        setDaemon(true);
        _reader = reader;
        final OutputStream os = socket.getOutputStream();
        sendInit(os, serverUuid);
        _is = socket.getInputStream();
        _node = readNode(socket, _is);
        setName("InboundTcp(" + service + "/" + (name != null ? name : serverUuid) + ")<(" + _node.getAddress() + ")");
    }

    @Override
    public void run() {
        try {
            while (!currentThread().isInterrupted() && _node.isConnected()) {
                final byte command = readCommand();
                final byte[] messageData = readMessageData(_is);
                final ReceivedMessage<TcpNode> message = new ReceivedMessage<TcpNode>(command, messageData, _node);
                _reader.read(message);
                _node.recordInbound();
            }
        } catch (final InterruptedIOException ignored) {
            currentThread().interrupt();
        } catch (final EOFException ignored) {
        } catch (final Exception e) {
            if (!(e instanceof IOException) || _node.isConnected()) {
                LOG.warn("Got unexpected error while handling connection from " + _node.getAddress() + ". Close this connection now.", e);
            }
        } finally {
            closeQuietly(this);
        }
    }

    private byte readCommand() throws IOException {
        final byte[] command = new byte[1];
        if (_is.read(command) < 1) {
            throw new EOFException();
        }
        return command[0];
    }

    protected void sendInit(@Nonnull OutputStream to, @Nonnull UUID serverUuid) throws IOException {
        final byte[] uuidAsBytes = new byte[16];
        putLong(uuidAsBytes, 0, serverUuid.getMostSignificantBits());
        putLong(uuidAsBytes, 8, serverUuid.getLeastSignificantBits());
        to.write(uuidAsBytes);
    }

    @Nonnull
    protected InboundTcpNode readNode(@Nonnull Socket socket, @Nonnull InputStream is) throws IOException {
        final UUID uuid = readUuid(is);
        return new InboundTcpNode(uuid, socket, is);
    }

    @Nonnull
    protected UUID readUuid(@Nonnull InputStream is) throws IOException {
        final byte[] buf = new byte[16];
        final int read = is.read(buf);
        if (read != 16) {
            throw new IOException("Received a content that is not 16 bytes long.");
        }
        return new UUID(getLong(buf, 0), getLong(buf, 8));
    }

    @Nonnull
    protected byte[] readMessageData(@Nonnull InputStream is) throws IOException {
        final int length = readLengthFrom(is);
        final byte[] messageData = new byte[length];
        int offset = 0;
        int read = is.read(messageData);
        while (read >= 0 && offset < length) {
            offset += read;
            read = is.read(messageData, offset, length - offset);
        }
        if (read < 0) {
            throw new EOFException();
        }
        if (offset != length) {
            throw new IOException("Received a payload with another payload length then expected.");
        }
        return messageData;
    }

    protected int readLengthFrom(@Nonnull InputStream is) throws IOException {
        final byte[] bytes = new byte[4];
        if (is.read(bytes) < 0) {
            throw new EOFException();
        }
        final int length = getInt(bytes, 0);
        if (length < 0) {
            throw new IOException("Received illegal packet. Leading packet is not the length of the following content.");
        }
        return length;
    }

    @Nonnull
    public InboundTcpNode getNode() {
        return _node;
    }

    @Override
    public void close() throws Exception {
        try {
            _node.close();
        } finally {
            _reader.onClose(this);
        }
    }

    @Override
    public String toString() {
        return _node.toString();
    }

    public static interface Reader {
        public void read(@Nonnull ReceivedMessage<TcpNode> message) throws IOException;
        public void onClose(@Nonnull InboundTcpWorker worker) throws Exception;
    }

}

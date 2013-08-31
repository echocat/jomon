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

import org.echocat.jomon.net.cluster.channel.Message;
import org.echocat.jomon.net.service.SrvEntryBasedServicesManager;
import org.echocat.jomon.runtime.util.Duration;
import org.echocat.jomon.runtime.util.ServiceTemporaryUnavailableException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.UUID;

import static java.lang.Thread.currentThread;
import static org.echocat.jomon.net.Protocol.tcp;
import static org.echocat.jomon.net.cluster.channel.ByteUtils.putLong;
import static org.echocat.jomon.net.cluster.channel.ClusterChannelConstants.pingCommand;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

@ThreadSafe
public class TcpInput extends SrvEntryBasedServicesManager<InetSocketAddress, OutputStream> {

    private final UUID _uuid;

    private Duration _connectionTimeout = new Duration("2s");
    private Duration _soTimeout = new Duration("30s");

    public TcpInput(@Nonnull String service, @Nonnull UUID uuid) {
        super(tcp, service);
        _uuid = uuid;
    }

    @Nonnull
    public Duration getConnectionTimeout() {
        return _connectionTimeout;
    }

    public void setConnectionTimeout(@Nonnull Duration connectionTimeout) {
        _connectionTimeout = connectionTimeout;
    }

    @Nonnull
    public Duration getSoTimeout() {
        return _soTimeout;
    }

    public void setSoTimeout(@Nonnull Duration soTimeout) {
        _soTimeout = soTimeout;
    }

    @Override
    protected OutputStream tryGetOutputFor(@Nonnull InetSocketAddress original, @Nonnull InetSocketAddress target, @Nonnull State oldState) throws Exception {
        final Socket socket = new Socket();
        socket.setKeepAlive(true);
        socket.setReuseAddress(true);
        socket.setSoTimeout((int) _soTimeout.toMilliSeconds());
        socket.connect(target, (int) _connectionTimeout.toMilliSeconds());
        boolean success = false;
        try {
            OutputStream os = socket.getOutputStream();
            try {
                sendPing(os);
                success = true;
            } catch (InterruptedException ignored) {
                currentThread().interrupt();
                os = null;
            } finally {
                if (!success) {
                    closeQuietly(os);
                }
            }
            return os;
        } finally {
            if (!success) {
                closeQuietly(socket);
            }
        }
    }

    @SuppressWarnings("DuplicateThrows")
    public void send(@Nonnull Message message) throws Exception, InterruptedException {
        for (Object output : getOutputs()) {
            send(message, (OutputStream) output);
        }
    }

    public void sendPing() throws Exception {
        try {
            send(createPingMessage());
        } catch (InterruptedException ignored) {
            currentThread().interrupt();
        }
    }

    @SuppressWarnings("DuplicateThrows")
    protected void send(@Nonnull Message message, @Nonnull OutputStream to) throws Exception, InterruptedException {
        boolean success = false;
        boolean errorHandled = false;
        try {
            sendUnsafe(message, to);
            success = true;
        } catch (ServiceTemporaryUnavailableException e) {
            markAsGone(to, e.getMessage());
            errorHandled = true;
        } finally {
            if (!success && !errorHandled) {
                markAsGone(to);
            }
        }
    }

    protected void sendUnsafe(@Nonnull Message message, @Nonnull final OutputStream to) throws Exception {
        // noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (to) {
            to.write(message.getCommand());
            to.write(message.getData(), message.getOffset(), message.getLength());
        }
    }

    @SuppressWarnings("DuplicateThrows")
    protected void sendPing(@Nonnull OutputStream to) throws Exception, InterruptedException {
        send(createPingMessage(), to);
    }

    @Nonnull
    protected Message createPingMessage() {
        final byte[] payload = new byte[16];
        putLong(payload, 0, _uuid.getMostSignificantBits());
        putLong(payload, 8, _uuid.getLeastSignificantBits());
        return new Message(pingCommand, payload);
    }

    @Override
    public void markAsGone(@Nonnull OutputStream service, @Nullable String cause) throws InterruptedException {
        try {
            super.markAsGone(service, cause);
        } finally {
            closeQuietly(service);
        }
    }

    @Override protected InetSocketAddress toInetSocketAddress(@Nonnull InetSocketAddress input) throws Exception { return input; }
}

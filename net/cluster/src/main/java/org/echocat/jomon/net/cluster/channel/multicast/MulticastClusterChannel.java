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

package org.echocat.jomon.net.cluster.channel.multicast;

import org.echocat.jomon.net.cluster.channel.*;
import org.echocat.jomon.runtime.util.Duration;
import org.echocat.jomon.runtime.util.GotInterruptedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.*;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.*;

import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.echocat.jomon.net.cluster.channel.ByteUtils.getLong;
import static org.echocat.jomon.net.cluster.channel.ClusterChannelConstants.pingCommand;
import static org.echocat.jomon.runtime.concurrent.ThreadUtils.stop;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

public class MulticastClusterChannel extends NetBasedClusterChannel<Short, MulticastNode> implements AddressEnabledClusterChannel<Short, MulticastNode>, SendingQueueEnabledClusterChannel<Short, MulticastNode> {

    private static final Logger LOG = LoggerFactory.getLogger(MulticastClusterChannel.class);
    private static final int BUFFER_SIZE = 1024;
    private static final Random RANDOM = new SecureRandom();

    private BlockingDeque<Message> _messageQueue = new LinkedBlockingDeque<>(10000);
    private final Map<Short, MulticastNode> _idToNode = new ConcurrentHashMap<>();
    private volatile Collection<MulticastNode> _nodes;

    private final Reader _reader = new Reader();
    private final Writer _writer = new Writer();
    private final Pinger _pinger = new Pinger();

    @Nullable
    private volatile InetSocketAddress _address;
    @Nullable
    private volatile NetworkInterface _networkInterface;
    @Nonnull
    private Duration _ttl = new Duration("10s");
    @Nonnegative
    private double _pingIntervalToTimeoutRatio = 2.5;

    @Nullable
    private Thread _writingThread;
    @Nullable
    private Thread _readingThread;
    @Nullable
    private Thread _pingingThread;
    @Nullable
    private volatile MulticastSocket _out;
    @Nullable
    private volatile MulticastSocket _in;
    private volatile short _id;

    public MulticastClusterChannel() {
        setSoTimeout(new Duration("1s"));
    }

    public MulticastClusterChannel(@Nullable UUID uuid) {
        super(uuid);
        setSoTimeout(new Duration("1s"));
    }

    @Override
    public InetSocketAddress getAddress() {
        return _address;
    }

    @Override
    public void setAddress(@Nullable final InetSocketAddress address) {
        doSafeAndReinetIfNeeded(new Callable<Void>() {
            @Override
            public Void call() {
                _address = address;
                return null;
            }
        });
    }

    @Override
    public void setAddress(@Nullable final InetSocketAddress address, @Nullable final NetworkInterface networkInterface) {
        doSafeAndReinetIfNeeded(new Callable<Void>() {
            @Override
            public Void call() {
                _address = address;
                _networkInterface = networkInterface;
                return null;
            }
        });
    }

    @Override
    public NetworkInterface getInterface() {
        return _networkInterface;
    }

    @Override
    public void setInterface(@Nullable final NetworkInterface networkInterface) {
        doSafeAndReinetIfNeeded(new Callable<Void>() { @Override public Void call() {
            _networkInterface = networkInterface;
            return null;
        }});
    }

    @Override
    public int getSendingQueueCapacity() {
        return _messageQueue.remainingCapacity() + _messageQueue.size();
    }

    @Override
    public void setSendingQueueCapacity(@Nonnegative final int capacity) {
        doSafeAndReinetIfNeeded(new Callable<Void>() { @Override public Void call() {
            _messageQueue = new LinkedBlockingDeque<>(capacity > 0 ? capacity : 1);
            return null;
        }});
    }

    @Nonnull
    public Duration getTtl() {
        return _ttl;
    }

    public void setTtl(@Nonnull Duration ttl) {
        if (ttl == null) {
            throw new NullPointerException();
        }
        _ttl = ttl;
    }

    @Nonnegative
    public final double getPingIntervalToTimeoutRatio() {
        return _pingIntervalToTimeoutRatio;
    }

    public final void setPingIntervalToTimeoutRatio(@Nonnegative double pingIntervalToTimeoutRatio) {
        if (pingIntervalToTimeoutRatio <= 0) {
            throw new IllegalArgumentException();
        }
        _pingIntervalToTimeoutRatio = pingIntervalToTimeoutRatio;
    }

    @Override
    protected void initInLock() throws Exception {
        super.initInLock();
        _pingingThread = new Thread(_pinger, toString() + ".Pinger");
        _pingingThread.setDaemon(true);
        _readingThread = new Thread(_reader, toString() + ".Reader");
        _readingThread.setDaemon(true);
        _writingThread = new Thread(_writer, toString() + ".Writer");
        _writingThread.setDaemon(true);

        _pingingThread.start();
        _readingThread.start();
        _writingThread.start();
    }

    @Override
    protected void closeBeforeLock() throws Exception {
        super.closeBeforeLock();
        closeIn();
        closeOut();
        stop(_pingingThread);
        stop(_writingThread);
        stop(_readingThread);
    }

    @Override
    protected void closeInLock() throws Exception {
        super.closeInLock();
        try {
            try {
                try {
                    try {
                        stop(_pingingThread);
                    } finally {
                        _pingingThread = null;
                    }
                } finally {
                    try {
                        stop(_writingThread);
                    } finally {
                        _writingThread = null;
                    }
                }
            } finally {
                try {
                    stop(_readingThread);
                } finally {
                    _readingThread = null;
                }
            }
        } finally {
            try {
                closeIn();
            } finally {
                closeOut();
            }
        }
    }

    protected void closeIn() {
        try {
            try {
                final InetSocketAddress address = _address;
                final NetworkInterface networkInterface = _networkInterface;
                if (address != null) {
                    try {
                        _in.leaveGroup(address, networkInterface);
                    } catch (final Exception ignored) {}
                }
            } finally {
                closeQuietly(_in);
            }
        } finally {
            _in = null;
        }
    }

    protected void closeOut() {
        try {
            closeQuietly(_out);
        } finally {
            _out = null;
        }
    }

    @Nullable
    protected DatagramSocket getOut() throws IOException, InterruptedException {
        getLock().lockInterruptibly();
        try {
            if (_out == null) {
                _out = new MulticastSocket();
                _out.setTimeToLive((int) _ttl.in(SECONDS));
                _out.setSoTimeout((int) getSoTimeout().in(MILLISECONDS));
            }
            return _out != null && !_out.isClosed() ? _out : null;
        } finally {
            getLock().unlock();
        }
    }

    @Nullable
    protected DatagramSocket getIn() throws IOException, InterruptedException {
        getLock().lockInterruptibly();
        try {
            if (_in == null) {
                final InetSocketAddress address = _address;
                final NetworkInterface networkInterface = _networkInterface;
                if (address != null) {
                    _in = new MulticastSocket(address.getPort());
                    _in.joinGroup(address, networkInterface);
                    _in.setTimeToLive((int) _ttl.in(SECONDS));
                    _in.setSoTimeout((int) getSoTimeout().in(MILLISECONDS));
                } else {
                    _in = null;
                }
            }
            return _in != null && !_in.isClosed() ? _in : null;
        } finally {
            getLock().unlock();
        }
    }

    @Override
    public boolean isConnected() {
        return _address != null && _readingThread != null && _writingThread != null;
    }

    @Nonnull
    @Override
    public MulticastNode getLocalNode() {
        return new MulticastNode(_id, getUuid(), _address);
    }

    @Override
    public void send(@Nonnull Message message) {
        try {
            _messageQueue.put(message);
        } catch (final InterruptedException ignored) {
            currentThread().interrupt();
            throw new GotInterruptedException();
        }
    }

    @Override
    public void send(@Nonnull Message message, @Nonnegative long timeout, @Nonnull TimeUnit unit) {
        try {
            _messageQueue.offer(message, timeout, unit);
        } catch (final InterruptedException ignored) {
            currentThread().interrupt();
        }
    }

    protected void sendInternal(@Nonnegative short id, @Nonnull Message message) throws IOException, InterruptedException {
        final DatagramSocket out = getOut();
        if (out != null) {
            final DatagramPacket packet = toPacket(id, message);
            if (packet != null) {
                try {
                    out.send(packet);
                    recordMessageSend();
                } catch (final SocketException e) {
                    final String messageOfException = e.getMessage();
                    if (messageOfException == null && !messageOfException.equalsIgnoreCase("socket closed")) {
                        throw e;
                    }
                }
            }
        }
    }

    protected void sendPing(@Nonnegative short id) throws IOException, InterruptedException {
        final byte[] payload = new byte[16];
        final UUID uuid = getUuid();
        ByteUtils.putLong(payload, 0, uuid.getMostSignificantBits());
        ByteUtils.putLong(payload, 8, uuid.getLeastSignificantBits());
        sendInternal(id, new Message(pingCommand, payload));
        recordPingSend();
    }

    @Nonnegative
    protected short getId(boolean forceSendPing) throws IOException, InterruptedException {
        short id = _id;
        for (int i = 0; i < 100 && id <= 0; i++) {
            final byte[] bytes = new byte[2];
            RANDOM.nextBytes(bytes);
            id = ByteUtils.getShort(bytes, 0);
        }
        if (id <= 0) {
            throw new IllegalStateException("It was not possible to retrieve an id in 100 tries.");
        }
        if (forceSendPing || id != _id || isPingRequired()) {
            sendPing(id);
        }
        if (id != _id) {
            _id = id;
            LOG.info("Registered myself with id #" + id + " in the cluster.");
        }
        return id;
    }

    @Nullable
    protected DatagramPacket toPacket(@Nonnegative short id, @Nonnull Message message) throws SocketException {
        final InetSocketAddress address = _address;
        final DatagramPacket packet;
        if (address != null) {
            if (message.getLength() <= (BUFFER_SIZE - 3)) {
                final byte[] payload = new byte[message.getLength() + 3];
                payload[0] = message.getCommand();
                ByteUtils.putShort(payload, 1, id);
                System.arraycopy(message.getData(), message.getOffset(), payload, 3, message.getLength());
                packet = new DatagramPacket(payload, payload.length, address);
            } else {
                packet = null;
                LOG.warn("It was not possible to send '" + message + "' because it reached the limit of " + (BUFFER_SIZE - 3) + " bytes for each packet. This message will be ignored.");
            }
        } else {
            packet = null;
        }
        return packet;
    }

    protected void readFrom(@Nonnull DatagramSocket in) throws IOException {
        final byte[] buffer = new byte[BUFFER_SIZE];
        final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        try {
            in.receive(packet);
            final ReceivedMessage<MulticastNode> message = toReceivedMessage(packet);
            if (message != null) {
                message.getFrom().recordOutbound();
                read(message);
            }
        } catch (final SocketTimeoutException ignored) {
        } catch (final SocketException e) {
            final String message = e.getMessage();
            if (message == null && !message.equalsIgnoreCase("socket closed")) {
                throw e;
            }
        }

    }

    @Nullable
    protected ReceivedMessage<MulticastNode> toReceivedMessage(@Nonnull DatagramPacket packet) throws SocketException {
        final ReceivedMessage<MulticastNode> message;
        final int length = packet.getLength();
        if (length >= 3) {
            final int offset = packet.getOffset();
            final byte[] data = packet.getData();
            final byte command = data[offset];
            final short remoteId = ByteUtils.getShort(data, offset + 1);
            final byte[] messageData = length >= 4 ? Arrays.copyOfRange(data, offset + 3, offset + length) : new byte[0];
            final MulticastNode node = toNode(command, remoteId, messageData, packet);
            message = node != null ? new ReceivedMessage<>(command, messageData, node) : null;
        } else {
            message = null;
        }
        return message;
    }

    @Nullable
    protected MulticastNode toNode(byte command, short remoteId, @Nonnull byte[] messageData, @Nullable DatagramPacket packet) {
        final SocketAddress address = packet.getSocketAddress();
        return toNode(command, remoteId, messageData, address);
    }

    @Nullable
    protected MulticastNode toNode(byte command, short remoteId, @Nonnull byte[] messageData, @Nullable SocketAddress address) {
        return toNode(command, remoteId, messageData, address instanceof InetSocketAddress ? (InetSocketAddress) address : null);
    }

    @Nullable
    protected MulticastNode toNode(byte command, short remoteId, @Nonnull byte[] messageData, @Nullable InetSocketAddress socketAddress) {
        final MulticastNode node;
        if (command == pingCommand) {
            if (messageData.length == 16) {
                final UUID uuid = new UUID(getLong(messageData, 0), getLong(messageData, 8));
                node = new MulticastNode(remoteId, uuid, socketAddress);
                node.recordVitalSign();
            } else {
                node = null;
            }
        } else {
            final MulticastNode potentialNode = remoteId != _id ? findNodeBy(remoteId) : null;
            if (potentialNode != null && potentialNode.getId() == remoteId) {
                final InetSocketAddress potentialSocketAddress = potentialNode.getAddress();
                final InetAddress potentialAddress = potentialSocketAddress != null ? potentialSocketAddress.getAddress() : null;
                final InetAddress address = socketAddress != null ? socketAddress.getAddress() : null;
                if (address != null ? address.equals(potentialAddress) : potentialAddress == null) {
                    node = potentialNode;
                    potentialNode.recordInbound();
                } else {
                    node = null;
                }
            } else {
                node = null;
            }
        }
        return node;
    }

    @Nullable
    private MulticastNode findNodeBy(short remoteId) {
        synchronized (_idToNode) {
            return _idToNode.get(remoteId);
        }
    }


    @Override
    protected void readPing(@Nonnull ReceivedMessage<MulticastNode> message) {
        synchronized (_idToNode) {
            final MulticastNode node = message.getFrom();
            if (!node.getUuid().equals(getUuid())) {
                final MulticastNode current = _idToNode.get(node.getId());
                if (current != null && current.equals(node)) {
                    current.recordVitalSign();
                } else {
                    _idToNode.put(node.getId(), node);
                    LOG.info("Node " + node + " entered the cluster.");
                    for (final Handler handler : getHandlers()) {
                        if (handler instanceof PresenceHandler) {
                            ((PresenceHandler) handler).nodeEnter(this, current);
                        }
                    }
                }
                if (node != null && node.getId() == _id) {
                    // Another node assigned itself to my id... so I invalidate myself to select a new id...
                    _id = 0;
                }
            }
        }
    }

    @Override
    @Nonnegative
    public Integer getSendingQueueSize() {
        return _messageQueue.size();
    }

    @Override
    @Nonnegative
    @Nonnull
    public Short getId() {
        return _id;
    }

    protected final void cleanUpNodes() {
        final long expiresAt = currentTimeMillis() - (long) (getPingInterval().in(MILLISECONDS) * _pingIntervalToTimeoutRatio);
        synchronized (_idToNode) {
            final Iterator<MulticastNode> i = _idToNode.values().iterator();
            while (i.hasNext()) {
                final MulticastNode node = i.next();
                if (node.getLastSeenInMillis() <= expiresAt) {
                    i.remove();
                    LOG.info("Node " + node + " left the cluster. (Timeout)");
                    for (final Handler handler : getHandlers()) {
                        if (handler instanceof PresenceHandler) {
                            ((PresenceHandler) handler).nodeLeft(this, node);
                        }
                    }
                }
            }
            _nodes = _idToNode.values();
        }
    }

    @Nonnull
    @Override
    public final Set<MulticastNode> getNodes() {
        final Set<MulticastNode> result = new TreeSet<>(Node.ADDRESS_BASED_COMPARATOR);
        synchronized (_idToNode) {
            result.addAll(_idToNode.values());
        }
        return result;
    }

    @Override
    public final void ping() {
        cleanUpNodes();
        try {
            getId(true);
        } catch (final InterruptedException ignored) {
            currentThread().interrupt();
        } catch (final Exception e) {
            throw new RuntimeException("Could not send ping.", e);
        }
    }

    @Override
    protected void recordMessageSend() {
        super.recordMessageSend();
        final Collection<MulticastNode> nodes = _nodes;
        if (nodes != null) {
            for (final MulticastNode node : nodes) {
                node.recordOutbound();
            }
        }
    }

    protected class Writer implements Runnable {
        @Override
        public void run() {
            try {
                while (!currentThread().isInterrupted()) {
                    final Message message = _messageQueue.take();
                    try {
                        final short id = getId(false);
                        sendInternal(id, message);
                    } catch (final IOException e) {
                        LOG.warn("Could not write message '" + message + "' to " + _address + getNetworkInterfaceSuffix() + ". This message is lost.", e);
                    }
                }
            } catch (final InterruptedException ignored) {
                currentThread().interrupt();
            }
        }
    }

    protected class Reader implements Runnable {
        @Override
        public void run() {
            try {
                while (!currentThread().isInterrupted()) {
                    try {
                        final DatagramSocket in = getIn();
                        if (in != null) {
                            readFrom(in);
                        } else {
                            sleep(1000);
                        }
                    } catch (final IOException e) {
                        LOG.warn("Could not read message from " + _address + getNetworkInterfaceSuffix() + ".", e);
                    }
                }
            } catch (final InterruptedException ignored) {
                currentThread().interrupt();
            }
        }
    }

    @Nullable
    protected String getNetworkInterfaceSuffix() {
        final NetworkInterface networkInterface = _networkInterface;
        return networkInterface != null ? "@" + networkInterface.getName() : "";
    }

    @Override
    public String toString() {
        final String name = getName();
        final String result;
        if (name != null) {
            result = name;
        } else {
            final InetSocketAddress address = _address;
            result = address != null ? address + getNetworkInterfaceSuffix() : "<offline>";
        }
        return result;
    }

}

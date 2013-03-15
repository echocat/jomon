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

import org.echocat.jomon.net.cluster.channel.*;
import org.echocat.jomon.net.cluster.channel.tcp.InboundTcpWorker.Reader;
import org.echocat.jomon.runtime.StringUtils;
import org.echocat.jomon.runtime.jaxb.InetSocketAddressPropertyEditor;
import org.echocat.jomon.runtime.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;

import static java.lang.Thread.currentThread;
import static java.util.Collections.unmodifiableSet;
import static org.echocat.jomon.net.cluster.channel.Node.ADDRESS_BASED_COMPARATOR;
import static org.echocat.jomon.runtime.concurrent.ThreadUtils.stop;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

@ThreadSafe
public class TcpClusterChannel extends NetBasedClusterChannel<UUID, TcpNode> implements AddressEnabledClusterChannel<UUID, TcpNode>, SendingQueueEnabledClusterChannel<UUID, TcpNode>, RemoteAddressesEnabledClusterChannel<UUID, TcpNode>, ServiceEnabledClusterChannel<UUID, TcpNode>, BlockableClusterChannel<UUID, TcpNode>, DropMessagesEnabledClusterChannel<UUID, TcpNode> {

    public static final int DEFAULT_PORT = 56876;
    public static final Duration RETRY_DURATION = new Duration("10s");

    private static final Logger LOG = LoggerFactory.getLogger(TcpClusterChannel.class);

    private final Set<InboundTcpWorker> _inboundWorkers = new HashSet<>();
    private final Reader _reader = new Reader() {
        @Override public void read(@Nonnull ReceivedMessage<TcpNode> message) throws IOException {
            TcpClusterChannel.this.read(message);
        }
        @Override public void onClose(@Nonnull InboundTcpWorker worker) {
            final Lock lock = getLock();
            try {
                lock.lockInterruptibly();
                try {
                    _inboundWorkers.remove(worker);
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException ignored) {
                currentThread().interrupt();
            }
        }
    };

    private String _service = "ttc";
    private Duration _connectionTimeout = new Duration("2s");
    private Collection<InetSocketAddress> _remoteAddresses;
    private InetSocketAddress _address = new InetSocketAddress(DEFAULT_PORT);
    private int _maxNumberOfIncomingConnections = 100;
    private int _numberOfIncomingWorker = 10;
    private int _sendingQueueCapacity = 250;
    private boolean _blocking = true;
    private boolean _dropMessagesIfQueueIsFull;

    private OutboundTcpHandler _outbound;
    private ServerSocket _in;
    private Acceptor _acceptor;

    public TcpClusterChannel() {}

    public TcpClusterChannel(@Nullable UUID uuid) {
        super(uuid);
    }

    @Nonnull
    @Override
    public UUID getId() {
        return getUuid();
    }

    @Override
    @Nonnull
    public String getService() {
        return _service;
    }

    @Override
    public void setService(@Nonnull final String service) {
        doSafeAndReinetIfNeeded(new Callable<Void>() { @Override public Void call() throws Exception {
            _service = service;
            return null;
        }});
    }

    public Duration getConnectionTimeout() {
        return _connectionTimeout;
    }

    public void setConnectionTimeout(final Duration connectionTimeout) {
        doSafe(new Callable<Void>() { @Override public Void call() throws Exception {
            _connectionTimeout = connectionTimeout;
            if (_outbound != null) {
                _outbound.setConnectionTimeout(connectionTimeout);
            }
            return null;
        }});
    }

    @Override
    @Nullable
    public Collection<InetSocketAddress> getRemoteAddresses() {
        return _remoteAddresses;
    }

    @Override
    public void setRemoteAddresses(@Nullable final Collection<InetSocketAddress> remoteAddresses) {
        doSafe(new Callable<Void>() { @Override public Void call() throws Exception {
            if (remoteAddresses != null ? !remoteAddresses.equals(_remoteAddresses) : _remoteAddresses != null) {
                _remoteAddresses = remoteAddresses;
                if (_outbound != null) {
                    _outbound.setInputs(remoteAddresses);
                    try {
                        _outbound.check();
                    } catch (InterruptedException ignored) {
                        currentThread().interrupt();
                    }
                }
            }
            return null;
        }});
    }

    @Override
    @Nullable
    public String getRemoteAddressesAsString() {
        final Collection<InetSocketAddress> remoteAddresses = _remoteAddresses;
        final String result;
        if (remoteAddresses != null) {
            final StringBuilder sb = new StringBuilder();
            for (InetSocketAddress address : remoteAddresses) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(address.getHostString()).append(':').append(address.getPort());
            }
            result = sb.toString();
        } else {
            result = null;
        }
        return result;
    }

    @Override
    public void setRemoteAddressesAsString(@Nullable String remoteAddressesAsString) {
        final Collection<InetSocketAddress> addresses;
        if (remoteAddressesAsString != null) {
            addresses = new ArrayList<>();
            final String[] remoteAddressesAsStrings = StringUtils.split(remoteAddressesAsString, ",;\n\r\t", false, true);
            for (String remoteAddressAsString : remoteAddressesAsStrings) {
                final InetSocketAddressPropertyEditor editor = new InetSocketAddressPropertyEditor();
                editor.setAsText(remoteAddressAsString);
                final Object value = editor.getValue();
                if (value instanceof InetSocketAddress) {
                    addresses.add((InetSocketAddress) value);
                }
            }
        } else {
            addresses = null;
        }
        setRemoteAddresses(addresses);
    }

    @Override
    public int getSendingQueueCapacity() {
        return _sendingQueueCapacity;
    }

    @Override
    public void setSendingQueueCapacity(final int sendingQueueCapacity) {
        doSafeAndReinetIfNeeded(new Callable<Void>() { @Override public Void call() throws Exception {
            _sendingQueueCapacity = sendingQueueCapacity;
            return null;
        }});
    }

    @Override
    public boolean isBlocking() {
        return _blocking;
    }

    @Override
    public void setBlocking(final boolean blocking) {
        doSafeAndReinetIfNeeded(new Callable<Void>() { @Override public Void call() throws Exception {
            _blocking = blocking;
            return null;
        }});
    }

    @Override
    public boolean isDropMessagesIfQueueIsFull() {
        return _dropMessagesIfQueueIsFull;
    }

    @Override
    public void setDropMessagesIfQueueIsFull(final boolean dropMessagesIfQueueIsFull) {
        doSafeAndReinetIfNeeded(new Callable<Void>() { @Override public Void call() throws Exception {
            _dropMessagesIfQueueIsFull = dropMessagesIfQueueIsFull;
            return null;
        }});
    }

    @Override
    @Nullable
    public InetSocketAddress getAddress() {
        return _address;
    }

    @Override
    public void setAddress(@Nullable final InetSocketAddress address) {
        doSafe(new Callable<Void>() { @Override public Void call() throws Exception {
            if (address != null ? !address.equals(_address) : _address != null) {
                _address = address;
                closeQuietly(_in);
                _in = null;
                closeQuietly(_inboundWorkers);
            }
            return null;
        }});
    }

    @Nonnegative
    public int getNumberOfIncomingWorker() {
        return _numberOfIncomingWorker;
    }

    public void setNumberOfIncomingWorker(@Nonnegative final int numberOfIncomingWorker) {
        doSafeAndReinetIfNeeded(new Callable<Void>() { @Override public Void call() throws Exception {
            _numberOfIncomingWorker = numberOfIncomingWorker;
            return null;
        }});
    }

    @Nonnegative
    public int getMaxNumberOfIncomingConnections() {
        return _maxNumberOfIncomingConnections;
    }

    public void setMaxNumberOfIncomingConnections(@Nonnegative int maxNumberOfIncomingConnections) {
        _maxNumberOfIncomingConnections = maxNumberOfIncomingConnections;
    }

    @Override
    public void setSoTimeout(@Nonnull final Duration soTimeout) {
        doSafe(new Callable<Void>() { @Override public Void call() throws Exception {
            TcpClusterChannel.super.setSoTimeout(soTimeout);
            if (_outbound != null) {
                _outbound.setSoTimeout(soTimeout);
            }
            return null;
        }});
    }

    @Override
    public void setPingInterval(@Nonnull final Duration pingInterval) {
        doSafe(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TcpClusterChannel.super.setPingInterval(pingInterval);
                if (_outbound != null) {
                    _outbound.setCheckInterval(pingInterval);
                }
                return null;
            }
        });
    }



    @Override
    protected void initInLock() throws Exception {
        super.initInLock();
        _outbound = new OutboundTcpHandler(getService(), getUuid(), getSendingQueueCapacity(), getName(), isBlocking(), isDropMessagesIfQueueIsFull());
        _outbound.setConnectionTimeout(getConnectionTimeout());
        _outbound.setSoTimeout(getSoTimeout());
        _outbound.setCheckInterval(getPingInterval());
        _outbound.setInputs(getRemoteAddresses());

        _acceptor = new Acceptor();
        _acceptor.start();
    }

    @Override
    protected void closeInLock() throws Exception {
        try {
            closeQuietly(_in);
            closeQuietly(_outbound);
            stop(_acceptor);
            stop(_inboundWorkers);
            closeQuietly(_in);
        } finally {
            super.closeInLock();
            _in = null;
            _outbound = null;
            _acceptor = null;
        }
    }

    @Nullable
    protected ServerSocket getIn() throws IOException, InterruptedException {
        final Lock lock = getLock();
        lock.lockInterruptibly();
        try {
            if (_in != null && (_in.isClosed() || !_in.isBound())) {
                closeQuietly(_in);
                _in = null;
            }
            if (_in == null && _address != null) {
                _in = new ServerSocket();
                _in.bind(_address);
                _in.setReuseAddress(true);
                _in.setSoTimeout((int) getSoTimeout().toMilliSeconds());
                LOG.info("Start to listen at " + _address.getAddress().getCanonicalHostName() + ":" + _address.getPort() + " for " + _service + "...");
            }
            return _in;
        } finally {
            lock.unlock();
        }
    }

    @Nonnull
    protected OutboundTcpHandler getOutbound() {
        return doSafe(new Callable<OutboundTcpHandler>() { @Override public OutboundTcpHandler call() throws Exception {
            if (_outbound == null) {
                throw new IllegalStateException("Init was not called yet.");
            }
            return _outbound;
        }});
    }

    @Override
    public void ping() {
        getOutbound().sendPing();
    }

    @Override
    protected void readPing(@Nonnull ReceivedMessage<TcpNode> message) {
        // This is done by OutboundTcpHandler and InboundTcpWorker
    }

    @Override
    public void send(@Nonnull Message message) throws IllegalArgumentException {
        try {
            getOutbound().send(message);
            recordMessageSend();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("It was not possible to send " + message + ".", e);
        }
    }

    @Override
    public void send(@Nonnull Message message, @Nonnegative long timeout, @Nonnull TimeUnit unit) throws IllegalArgumentException {
        try {
            getOutbound().send(message, timeout, unit);
            recordMessageSend();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("It was not possible to send " + message + ".", e);
        }
    }

    @Override
    public Integer getSendingQueueSize() {
        final OutboundTcpHandler outbound = _outbound;
        return outbound != null ? outbound.getCurrentMaximumQueueSize() : null;
    }

    @Override
    public boolean isConnected() {
        return _outbound != null;
    }

    @Nonnull
    @Override
    public LocalTcpNode getLocalNode() {
        return new LocalTcpNode(getUuid(), _address);
    }

    @Nonnull
    @Override
    public Set<? extends TcpNode> getNodes() {
        final AtomicReference<Object[]> outboundTcpNodes = new AtomicReference<>();
        final Set<InboundTcpNode> inboundTcpNodes = new HashSet<>();
        doSafe(new Callable<Set<? extends TcpNode>>() { @Override public Set<? extends TcpNode> call() throws Exception {
            if (_outbound != null) {
                outboundTcpNodes.set(_outbound.getOutputs());
            }
            for (InboundTcpWorker worker : _inboundWorkers) {
                inboundTcpNodes.add(worker.getNode());
            }
            return null;
        }});
        return merge(outboundTcpNodes.get(), inboundTcpNodes);
    }

    @Nonnull
    protected Set<TcpNodeInfo> merge(@Nullable Object[] outboundTcpNodes, @Nullable Set<InboundTcpNode> inboundTcpNodes) {
        final Map<UUID, TcpNodeInfo> uuidToNode = new HashMap<>();
        if (outboundTcpNodes != null) {
            for (Object plainNode : outboundTcpNodes) {
                final OutboundTcpNode node = (OutboundTcpNode) plainNode;
                final UUID uuid = node.getUuid();
                TcpNodeInfo info = uuidToNode.get(uuid);
                if (info == null) {
                    info = new TcpNodeInfo(uuid, node.getAddress());
                    uuidToNode.put(uuid, info);
                }
                info.setOutbound(node);
            }
        }
        if (inboundTcpNodes != null) {
            for (InboundTcpNode node : inboundTcpNodes) {
                final UUID uuid = node.getUuid();
                TcpNodeInfo info = uuidToNode.get(uuid);
                if (info == null) {
                    info = new TcpNodeInfo(uuid, node.getAddress());
                    uuidToNode.put(uuid, info);
                }
                info.setInbound(node);
            }
        }
        final Set<TcpNodeInfo> info = new TreeSet<>(ADDRESS_BASED_COMPARATOR);
        info.addAll(uuidToNode.values());
        return unmodifiableSet(info);
    }

    protected class Acceptor extends Thread {

        public Acceptor() {
            final String name = TcpClusterChannel.this.getName();
            setName("InboundTcp(" + getService() + "/" + (name != null ? name : getUuid()) + ").Acceptor");
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                while (!currentThread().isInterrupted()) {
                    try {
                        final ServerSocket in = getIn();
                        if (in != null) {
                            try {
                                final Socket socket = in.accept();
                                boolean success = false;
                                try {
                                    handleIncoming(socket);
                                    success = true;
                                } catch (SocketException e) {
                                    LOG.info("Could not accept connection from " + socket.getRemoteSocketAddress() + ". Got: " + e.getMessage());
                                } finally {
                                    if (!success) {
                                        closeQuietly(socket);
                                    }
                                }
                            } catch (SocketException e) {
                                if (!in.isClosed()) {
                                    // noinspection ThrowCaughtLocally
                                    throw e;
                                }
                            }
                        } else {
                            RETRY_DURATION.sleep();
                        }
                    } catch (InterruptedException ignored) {
                        currentThread().interrupt();
                    } catch (SocketTimeoutException ignored) {
                    } catch (IOException e) {
                        LOG.warn("Got error while waiting for an incoming connection. Go to sleep and retry it after " + RETRY_DURATION + "...", e);
                        RETRY_DURATION.sleep();
                    } catch (Exception e) {
                        LOG.error("Got error while waiting for an incoming connection. Go to sleep and retry it after " + RETRY_DURATION + "...", e);
                        RETRY_DURATION.sleep();
                    }
                }
            } catch (InterruptedException ignored) {
                currentThread().interrupt();
            }
        }

        protected void handleIncoming(@Nonnull Socket socket) throws IOException, InterruptedException {
            final Lock lock = getLock();
            lock.lockInterruptibly();
            try {
                if (_inboundWorkers.size() < _maxNumberOfIncomingConnections) {
                    final InboundTcpWorker worker = new InboundTcpWorker(_reader, socket, getUuid(), getService(), TcpClusterChannel.this.getName());
                    final InboundTcpNode node = worker.getNode();
                    final UUID uuid = node.getUuid();
                    if (getUuid().equals(uuid)) {
                        // Ignore a connection of myself to me
                        node.close();
                    } else {
                        worker.start();
                        _inboundWorkers.add(worker);
                    }
                } else {
                    LOG.warn("Dropping incoming connection from " + socket.getRemoteSocketAddress() + " because the maximum of " + _maxNumberOfIncomingConnections + " is reach.");
                }
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public String toString() {
        final String name = getName();
        final InetSocketAddress address = getAddress();
        return "TcpClusterChannel(" + getService() + "/" + (name != null ? name : getUuid()) + "):" + (address != null ? address : "<offline>");
    }
}

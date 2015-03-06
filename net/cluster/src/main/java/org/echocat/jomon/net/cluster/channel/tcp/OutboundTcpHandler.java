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

import org.echocat.jomon.net.service.SrvEntryBasedServicesManager;
import org.echocat.jomon.net.cluster.channel.Message;
import org.echocat.jomon.runtime.util.Duration;
import org.echocat.jomon.runtime.util.ServiceTemporaryUnavailableException;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.echocat.jomon.net.Protocol.tcp;
import static org.echocat.jomon.net.cluster.channel.ByteUtils.*;
import static org.echocat.jomon.net.cluster.channel.ClusterChannelConstants.pingCommand;
import static org.echocat.jomon.runtime.concurrent.ThreadUtils.stop;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

@ThreadSafe
public class OutboundTcpHandler extends SrvEntryBasedServicesManager<InetSocketAddress, OutboundTcpNode> {

    private final Map<InetSocketAddress, OutboundTcpNode> _addressToNode = new WeakHashMap<>();
    private final Map<OutboundTcpNode, Sender> _nodeToSender = new ConcurrentHashMap<>();

    private final UUID _uuid;
    private final boolean _dropMessagesIfQueueIsFull;
    private final int _queuePerNodeCapacity;
    private final String _name;
    private final boolean _waitForSendFinished;

    private Duration _connectionTimeout = new Duration("2s");
    private Duration _soTimeout = new Duration("30s");

    public OutboundTcpHandler(@Nonnull String service, @Nonnull UUID uuid, @Nonnegative int queuePerNodeCapacity, @Nullable String name, boolean waitForSendFinished, boolean dropMessagesIfQueueIsFull) {
        super(tcp, service);
        _uuid = uuid;
        _dropMessagesIfQueueIsFull = dropMessagesIfQueueIsFull;
        _queuePerNodeCapacity = queuePerNodeCapacity > 0 ? queuePerNodeCapacity : 1;
        _name = name;
        _waitForSendFinished = waitForSendFinished;
        setCheckerThreadName(toString() + ".Checker");
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
    protected OutboundTcpNode tryGetOutputFor(@Nonnull InetSocketAddress original, @Nonnull InetSocketAddress target, @Nonnull State oldState) throws Exception {
        synchronized (this) {
            OutboundTcpNode node = _addressToNode.get(target);
            if (node == null || !node.isConnected()) {
                node = createNewNodeFor(target);
                _addressToNode.put(target, node);
            }
            if (node != null) {
                boolean success = false;
                try {
                    sendUnsafe(createPingMessage(), node);
                    success = true;
                } finally {
                    if (!success) {
                        closeQuietly(node);
                        node = null;
                    }
                }
            }
            return node;
        }
    }

    @Nonnull
    protected OutboundTcpNode createNewNodeFor(@Nonnull final InetSocketAddress target) throws IOException {
        final Socket socket = new Socket();
        socket.setKeepAlive(true);
        socket.setReuseAddress(true);
        socket.setSoTimeout((int) _soTimeout.in(MILLISECONDS));
        try {
            socket.connect(target, (int) _connectionTimeout.in(MILLISECONDS));
        } catch (ConnectException | SocketTimeoutException e) {
            throw new ServiceTemporaryUnavailableException(e);
        }
        boolean success = false;
        try {
            final InputStream is = socket.getInputStream();
            try {
                final UUID remoteUuid = readUuid(is);
                final OutputStream os = socket.getOutputStream();
                try {
                    sendUuid(os, _uuid);
                    final OutboundTcpNode result;
                    if (!remoteUuid.equals(_uuid)) {
                        result = new OutboundTcpNode(remoteUuid, socket, os, new Runnable() { @Override public void run() {
                            synchronized (OutboundTcpHandler.this) {
                                _addressToNode.remove(target);
                            }
                        }});
                        success = true;
                    } else {
                        result = null;
                    }
                    return result;
                } finally {
                    if (!success) {
                        closeQuietly(os);
                    }
                }
            } catch (final IOException e) {
                throw new ServiceTemporaryUnavailableException(e);
            } finally {
                if (!success) {
                    closeQuietly(is);
                }
            }
        } finally {
            if (!success) {
                closeQuietly(socket);
            }
        }
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

    protected void sendUuid(@Nonnull OutputStream to, @Nonnull UUID localUuid) throws IOException {
        final byte[] uuidAsBytes = new byte[16];
        putLong(uuidAsBytes, 0, localUuid.getMostSignificantBits());
        putLong(uuidAsBytes, 8, localUuid.getLeastSignificantBits());
        to.write(uuidAsBytes);
    }

    public void send(@Nonnull Message message) throws IOException, InterruptedException {
        final Object[] outputs = getOutputs();
        final Set<SendingTask> tasks = _waitForSendFinished ? new HashSet<SendingTask>(outputs.length) : null;
        for (final Object output : outputs) {
            // noinspection SuspiciousMethodCalls
            final Sender sender = _nodeToSender.get(output);
            if (sender != null) {
                final SendingTask task = sender.submit(message);
                if (task != null && tasks != null) {
                    tasks.add(task);
                }
            }
        }
        if (tasks != null) {
            for (final SendingTask task : tasks) {
                try {
                    task.get();
                } catch (final ExecutionException e) {
                    handleExecutionException(e);
                }
            }
        }
    }

    public void send(@Nonnull Message message, @Nonnegative long timeout, @Nonnull TimeUnit unit) throws IOException, InterruptedException, TimeoutException {
        final long timeoutAtInMillis = currentTimeMillis() + unit.toMillis(timeout);
        final Object[] outputs = getOutputs();
        final Set<SendingTask> tasks = _waitForSendFinished ? new HashSet<SendingTask>(outputs.length) : null;
        for (final Object output : outputs) {
            // noinspection SuspiciousMethodCalls
            final Sender sender = _nodeToSender.get(output);
            if (sender != null) {
                final long currentTimeoutInMillis = timeoutAtInMillis - currentTimeMillis();
                if (currentTimeoutInMillis > 0) {
                    final SendingTask task = sender.submit(message, currentTimeoutInMillis, MILLISECONDS);
                    if (task != null && tasks != null) {
                        tasks.add(task);
                    }
                } else {
                    throw new TimeoutException();
                }
            }
        }
        if (tasks != null) {
            for (final SendingTask task : tasks) {
                final long currentTimeoutInMillis = timeoutAtInMillis - currentTimeMillis();
                if (currentTimeoutInMillis > 0) {
                    try {
                        task.get(currentTimeoutInMillis, MILLISECONDS);
                    } catch (final ExecutionException e) {
                        handleExecutionException(e);
                    }
                } else {
                    throw new TimeoutException();
                }
            }
        }
    }

    protected void handleExecutionException(@Nonnull ExecutionException e) throws IOException {
        final Throwable cause = e.getCause();
        if (cause instanceof IOException) {
            throw (IOException) cause;
        } else if (cause instanceof RuntimeException) {
            throw (RuntimeException) cause;
        } else if (cause instanceof Error) {
            throw (Error) cause;
        } else if (cause != null) {
            throw new RuntimeException(cause.getMessage(), cause);
        } else {
            throw new RuntimeException(e);
        }
    }

    public void sendPing() {
        try {
            check();
        } catch (final InterruptedException ignored) {
            currentThread().interrupt();
        } catch (final Exception e) {
            throw new RuntimeException("It was not possible to send a ping to all nodes.", e);
        }
    }

    @Nonnull
    @Override
    public Object[] getOutputs() {
        return super.getOutputs();
    }

    protected void send(@Nonnull Message message, @Nonnull OutboundTcpNode to) throws IOException, InterruptedException {
        boolean success = false;
        boolean errorHandled = false;
        try {
            sendUnsafe(message, to);
            success = true;
        } catch (final ServiceTemporaryUnavailableException e) {
            markAsGone(to, e.getMessage());
            errorHandled = true;
        } finally {
            if (!success && !errorHandled) {
                markAsGone(to);
            }
        }
    }

    protected void sendUnsafe(@Nonnull Message message, @Nonnull final OutboundTcpNode to) throws IOException {
        // noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (to) {
            try {
                final OutputStream os = to.getOutputStream();
                os.write(message.getCommand());
                final byte[] lengthAsBytes = new byte[4];
                putInt(lengthAsBytes, 0, message.getLength());
                os.write(lengthAsBytes);
                os.write(message.getData(), message.getOffset(), message.getLength());
                to.recordOutbound();
            } catch (final SocketException e) {
                throw new ServiceTemporaryUnavailableException(e);
            }
        }
    }

    @Nonnull
    protected Message createPingMessage() {
        return new Message(pingCommand, new byte[0]);
    }

    @Override
    public void markAsGone(@Nonnull OutboundTcpNode node, @Nullable String cause) throws InterruptedException {
        try {
            super.markAsGone(node, cause);
        } finally {
            closeQuietly(node);
        }
    }

    @Override
    @Nonnull
    protected Object[] rebuildOutputs(@Nonnull Containers<OutboundTcpNode> containers) {
        final List<Container<OutboundTcpNode>> newContainers = containers.getContainersByLowersPriority();
        final Object[] outputs = new Object[newContainers.size()];
        int c = 0;
        for (final Container<OutboundTcpNode> container : newContainers) {
            outputs[c++] = container.getOutput();
        }
        return outputs;
    }

    @Override protected InetSocketAddress toInetSocketAddress(@Nonnull InetSocketAddress input) throws Exception { return input; }
    @Override protected void reportNoServicesAvailable() {}

    @Override
    public String toString() {
        return "OutboundTcp(" + getService() + "/" + (_name != null ? _name : _uuid) + ")";
    }


    @Override
    protected void onContainerGone(@Nonnull Container<OutboundTcpNode> container) {
        try {
            final Sender sender = _nodeToSender.remove(container.getOutput());
            stop(sender);
        } finally {
            super.onContainerGone(container);
        }
    }

    @Override
    protected void onContainerEnter(@Nonnull Container<OutboundTcpNode> container) {
        try {
            final Sender sender = new Sender(container.getOutput());
            sender.start();
            _nodeToSender.put(container.getOutput(), sender);
        } finally {
            super.onContainerEnter(container);
        }
    }

    @Nonnegative
    @Nullable
    public Integer getCurrentMaximumQueueSize() {
        Integer currentMaximumQueueSize = null;
        final Object[] outputs = getOutputs();
        for (final Object output : outputs) {
            // noinspection SuspiciousMethodCalls
            final Sender sender = _nodeToSender.get(output);
            if (sender != null) {
                final Integer size = sender.getQueueSize();
                if (size != null && (currentMaximumQueueSize == null || currentMaximumQueueSize < size)) {
                    currentMaximumQueueSize = size;
                }
            }
        }
        return currentMaximumQueueSize;
    }

    protected class Sender extends Thread {

        private final OutboundTcpNode _node;
        private final BlockingDeque<SendingTask> _tasks;

        public Sender(@Nonnull OutboundTcpNode node) {
            _node = node;
            _tasks = new LinkedBlockingDeque<>(_queuePerNodeCapacity);
            setName(toString());
        }

        @Override
        public void run() {
            try {
                while (!currentThread().isInterrupted()) {
                    final SendingTask task = _tasks.take();
                    task.execute();
                }
            } catch (final InterruptedException ignored) {
                currentThread().interrupt();
            }
        }

        @Nonnull
        public SendingTask submit(@Nonnull Message message) throws InterruptedException {
            final SendingTask task = new SendingTask(message, _node);
            final boolean isInQueue;
            if (_dropMessagesIfQueueIsFull) {
                isInQueue = _tasks.offer(task);
            } else {
                _tasks.put(task);
                isInQueue = true;
            }
            return isInQueue ? task : null;
        }

        @Nonnull
        public SendingTask submit(@Nonnull Message message, @Nonnegative long timeout, @Nonnull TimeUnit unit) throws InterruptedException, TimeoutException, IOException {
            final SendingTask task = new SendingTask(message, _node);
            final boolean isInQueue = _tasks.offer(task, timeout, unit);
            if (isInQueue && !_dropMessagesIfQueueIsFull) {
                throw new TimeoutException();
            }
            return isInQueue ? task : null;
        }

        @Nonnegative
        public Integer getQueueSize() {
            return _tasks.size();
        }

        @Override
        public String toString() {
            return OutboundTcpHandler.this.toString() + ">(" + _node.getAddress() + ")";
        }
    }

    protected class SendingTask implements Future<Void> {

        private final Message _message;
        private final OutboundTcpNode _to;
        private final Lock _lock = new ReentrantLock(true);
        private final Condition _condition = _lock.newCondition();

        private volatile boolean _done;
        private Throwable _exception;

        public SendingTask(@Nonnull Message message, @Nonnull OutboundTcpNode to) {
            _message = message;
            _to = to;
        }

        public void execute() throws InterruptedException {
            _lock.lockInterruptibly();
            try {
                if (!_done) {
                    try {
                        send(_message, _to);
                    } catch (final InterruptedException e) {
                        throw e;
                    } catch (final Throwable e) {
                        _exception = e;
                    } finally {
                        _done = true;
                        _condition.signalAll();
                    }
                }
            } finally {
                _lock.unlock();
            }
        }

        @Override
        public boolean isDone() {
            return _done;
        }

        @Override
        @Nullable
        public Void get() throws InterruptedException, ExecutionException {
            _lock.lockInterruptibly();
            try {
                if (!_done) {
                    _condition.await();
                }
                if (_exception != null) {
                    throw new ExecutionException(_exception);
                }
            } finally {
                _lock.unlock();
            }
            return null;
        }

        @Override
        @Nullable
        public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            final long timeoutInMillis = currentTimeMillis() + unit.toMillis(timeout);
            final long lockTimeout = timeoutInMillis - currentTimeMillis();
            if (lockTimeout > 0 && !_lock.tryLock(lockTimeout, MILLISECONDS)) {
                throw new TimeoutException();
            }
            try {
                if (!_done) {
                    final long awaitTimeout = timeoutInMillis - currentTimeMillis();
                    if (awaitTimeout > 0 && !_condition.await(awaitTimeout, MILLISECONDS)) {
                        throw new TimeoutException();
                    }
                }
                if (_exception != null) {
                    throw new ExecutionException(_exception);
                }
                if (!_done) {
                    throw new TimeoutException();
                }
            } finally {
                _lock.unlock();
            }
            return null;
        }

        @Override public boolean cancel(boolean mayInterruptIfRunning) { return false; }
        @Override public boolean isCancelled() { return false; }

    }

}

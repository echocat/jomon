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

package org.echocat.jomon.net.cluster.channel;

import org.echocat.jomon.runtime.math.OverPeriodCounter;
import org.echocat.jomon.runtime.util.Duration;
import org.echocat.jomon.runtime.util.ResourceUtils;
import org.echocat.jomon.testing.concurrent.ParallelTestRunner.Worker;
import org.echocat.jomon.testing.environments.LogEnvironment;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Rule;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class ClusterChannelTestSupport<ID, N extends Node<ID>, C extends ClusterChannel<ID, N>> {

    protected static final Charset CHARSET = Charset.forName("ISO-8859-1");
    protected static final UUID U1 = new UUID(0, 1);
    protected static final UUID U2 = new UUID(0, 2);
    protected static final UUID U3 = new UUID(0, 3);
    protected static final UUID U4 = new UUID(0, 4);
    protected static final UUID U5 = new UUID(0, 5);
    protected static final UUID U6 = new UUID(0, 6);

    @Rule
    public final LogEnvironment _logEnvironment = new LogEnvironment();

    private final MessageHandlerImpl<ID, N, C> _messageHandler = new MessageHandlerImpl<>();
    private final StateHandler<C> _stateHandler;

    protected ClusterChannelTestSupport(@Nonnull Class<C> type) {
        _stateHandler = new StateHandler<>(type);
    }

    @Nonnull
    protected LogEnvironment getLogEnvironment() {
        return _logEnvironment;
    }

    @Nonnull
    protected MessageHandlerImpl<ID, N, C> getMessageHandler() {
        return _messageHandler;
    }

    @Nonnull
    public List<Pair<C, ReceivedMessage<N>>> getReceivedMessages() {
        return getMessageHandler().getReceivedMessages();
    }

    @Nonnegative
    public int getNumberOfReceivedMessages() {
        return getMessageHandler().getNumberOfReceivedMessages();
    }

    @Nonnegative
    public double getNumberMessagesReceivedPerSecond() {
        return getMessageHandler().getNumberMessagesReceivedPerSecond();
    }

    @Nonnull
    protected StateHandler<C> getStateHandler() {
        return _stateHandler;
    }

    protected void resetMessageHandler() {
        getMessageHandler().reset();
    }

    protected void waitFor(@Nonnull StateCondition<C> condition) throws Exception {
        getStateHandler().waitFor(condition);
    }

    @Nonnull
    protected List<Worker> createWorkersFor(@Nonnegative int numberOfWorkers, @Nonnegative final int numberOfMessagesPerWorker, @Nonnull final Collection<String> addSendMessages, @Nonnull final C sendMessageWith, @Nullable final OverPeriodCounter counter) {
        final List<Worker> workers = new ArrayList<>();
        for (int i = 0; i < numberOfWorkers; i++) {
            final int worker = i;
            workers.add(new Worker() { @Override public void run() throws Exception {
                for (int j = 0; j < numberOfMessagesPerWorker; j++) {
                    final String message = sendMessageWith.getUuid() + "-" + worker + "-" + j;
                    addSendMessages.add(message);
                    sendMessageWith.send(new Message((byte) 1, message, CHARSET));
                    if (counter != null) {
                        counter.record();
                    }
                    afterMessageSend(message);
                }
            }});
        }
        return Collections.unmodifiableList(workers);
    }

    protected void afterMessageSend(@Nonnull String message) throws Exception {}

    @Nonnull
    protected List<Worker> createWorkersFor(@Nonnegative int numberOfWorkers, @Nonnegative int numberOfMessagesPerWorker, @Nonnull Collection<String> addSendMessages, @Nonnull Iterable<? extends C> sendMessageWith, @Nullable OverPeriodCounter counter) {
        final List<Worker> workers = new ArrayList<>();
        for (C channel : sendMessageWith) {
            workers.addAll(createWorkersFor(numberOfWorkers, numberOfMessagesPerWorker, addSendMessages, channel, counter));
        }
        return workers;
    }

    @Nonnull
    protected StateCondition<C> thatAllNodesConnected(@Nonnull final Collection<C> channels) {
        final Duration maxWaitTime = new Duration(channels.size() * 1500);
        return new StateCondition<C>(maxWaitTime) { @Override public boolean check(@Nonnull C clusterChannel) throws Exception {
            boolean result = true;
            for (C channel : channels) {
                if (channel instanceof PingEnabledClusterChannel) {
                    ((PingEnabledClusterChannel) channel).ping();
                }
                if (channel.getNodes().size() < channels.size() - 1) {
                    result = false;
                }
            }
            return result;
        }};
    }

    @Nonnull
    protected Map<String, AtomicInteger> getMessageToCount() {
        final Map<String, AtomicInteger> messageToCount = new HashMap<>();
        for (Pair<C, ReceivedMessage<N>> pair : getMessageHandler().getReceivedMessages()) {
            final String message = pair.getValue().getDataAsString(CHARSET);
            AtomicInteger count = messageToCount.get(message);
            if (count == null) {
                count = new AtomicInteger();
                messageToCount.put(message, count);
            }
            count.incrementAndGet();
        }
        return messageToCount;
    }

    @Nonnull
    protected Message message(@Nonnull String content) {
        return new Message((byte)1, content, CHARSET);
    }

    @Nonnull
    protected Pair<C, ReceivedMessage<N>> message(@Nonnull C channel, @Nonnull String message, @Nonnull N from) {
        final ReceivedMessage<N> receivedMessage = new ReceivedMessage<>((byte) 1, message, CHARSET, from);
        return new ImmutablePair<>(channel, receivedMessage);
    }

    @Nonnull
    protected Pair<C, ReceivedMessage<N>> message(@Nonnull C channel, @Nonnull String message, @Nonnull UUID uuid) {
        return message(channel, message, createNode(uuid));
    }

    @Nonnull
    protected List<C> channels(@Nonnull UUID... uuids) throws Exception {
        final List<C> channels = new ArrayList<>();
        boolean success = false;
        try {
            for (UUID uuid : uuids) {
                channels.add(channel(uuid));
            }
            afterAllChannelsCreated(channels);
            for (C channel : channels) {
                if (channel instanceof PingEnabledClusterChannel) {
                    ((PingEnabledClusterChannel) channel).ping();
                }
            }
            waitFor(thatAllNodesConnected(channels));
            success = true;
        } finally {
            if (!success) {
                for (C channel : channels) {
                    closeQuietly(channel);
                }
            }
        }
        return Collections.unmodifiableList(channels);
    }

    protected void closeQuietly(@Nullable Iterable<? extends C> channels) {
        if (channels != null) {
            for (C channel : channels) {
                closeQuietly(channel);
            }
        }
    }

    protected void afterAllChannelsCreated(@Nonnull List<C> channels) throws Exception {}

    protected void closeQuietly(@Nullable C channel) {
        ResourceUtils.closeQuietly(channel);
    }

    @Nonnull
    protected abstract C channel(@Nonnull UUID uuid) throws Exception;

    @Nonnull
    protected abstract N createNode(@Nonnull UUID uuid);

}

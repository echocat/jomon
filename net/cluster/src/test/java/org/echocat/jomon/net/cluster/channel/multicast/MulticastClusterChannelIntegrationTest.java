/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012-2014 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.net.cluster.channel.multicast;

import org.apache.commons.lang3.tuple.Pair;
import org.echocat.jomon.net.cluster.channel.ClusterChannelTestSupport;
import org.echocat.jomon.net.cluster.channel.ReceivedMessage;
import org.echocat.jomon.net.cluster.channel.StateCondition;
import org.echocat.jomon.runtime.concurrent.StopWatch;
import org.echocat.jomon.runtime.math.OverPeriodCounter;
import org.echocat.jomon.runtime.util.Duration;
import org.echocat.jomon.testing.concurrent.ParallelTestRunner.Worker;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.echocat.jomon.net.NetworkInterfaceQuery.networkInterface;
import static org.echocat.jomon.net.NetworkInterfaceRepository.networkInterfaceRepository;
import static org.echocat.jomon.net.NetworkInterfaceType.loopBack;
import static org.echocat.jomon.testing.Assert.assertThat;
import static org.echocat.jomon.testing.BaseMatchers.*;
import static org.echocat.jomon.testing.IterableMatchers.containsAllItemsOf;
import static org.echocat.jomon.testing.IterableMatchers.hasSize;
import static org.echocat.jomon.testing.concurrent.ParallelTestRunner.run;

public class MulticastClusterChannelIntegrationTest extends ClusterChannelTestSupport<Short, MulticastNode, MulticastClusterChannel> {

    protected static final Random RANDOM = new SecureRandom();
    protected static final String HOST = "230." + RANDOM.nextInt(255) + "." + RANDOM.nextInt(255) + "." + (RANDOM.nextInt(254) + 1);
    protected static final int PORT = 5000 + RANDOM.nextInt(60000);

    private final Logger _logger = LoggerFactory.getLogger(MulticastClusterChannel.class);

    public MulticastClusterChannelIntegrationTest() {
        super(MulticastClusterChannel.class);
    }

    @Test
    @Ignore("Does currently not work on not good performing test machines.")
    public void test3Instances() throws Exception {
        final List<MulticastClusterChannel> channels = channels(U1, U2, U3);
        try {
            channels.get(0).send(message("message1"));
            waitFor(thatQueuesAreEmptyAndReceivedMessages(channels, message(channels.get(1), "message1", U1), message(channels.get(1), "message1", U1)));
            resetMessageHandler();

            channels.get(1).send(message("message2"));
            waitFor(thatQueuesAreEmptyAndReceivedMessages(channels, message(channels.get(0), "message2", U2), message(channels.get(2), "message2", U2)));
        } finally {
            closeQuietly(channels);
        }
    }

    @Test
    public void test1InstanceConcurrent() throws Exception {
        final MulticastClusterChannel channel = channel(U1);
        try {
            final int numberOfWorkers = 4;
            final int numberOfMessages = 1000;
            final Set<String> messagesSend = Collections.synchronizedSet(new HashSet<String>());
            final List<Worker> workers = createWorkersFor(numberOfWorkers, numberOfMessages, messagesSend, channel, null);
            final StopWatch stopWatch = new StopWatch();
            run(workers);
            assertThat(stopWatch.getCurrentDuration(), isLessThan(new Duration("7ms").multiplyBy(numberOfMessages)));
            assertThat(messagesSend, hasSize(numberOfWorkers * numberOfMessages));
        } finally {
            closeQuietly(channel);
        }
    }

    @Test
    @Ignore("Does currently not work on not good performing test machines.")
    public void test3InstancesConcurrent() throws Exception {
        final List<MulticastClusterChannel> channels = channels(U1, U2, U3);
        try {
            final int numberOfWorkersPerChannel = 10;
            final int numberOfMessagesPerWorker = 1000;
            final Set<String> messagesSend = Collections.synchronizedSet(new HashSet<String>());
            final OverPeriodCounter counter = new OverPeriodCounter(new Duration("1m"), new Duration("1s"));
            final List<Worker> workers = createWorkersFor(numberOfWorkersPerChannel, numberOfMessagesPerWorker, messagesSend, channels, counter);
            final StopWatch stopWatch = new StopWatch();
            run(workers);

            assertThat(stopWatch.getCurrentDuration(), isLessThan(new Duration("2ms").multiplyBy(messagesSend.size())));
            assertThat(messagesSend, hasSize(channels.size() * numberOfWorkersPerChannel * numberOfMessagesPerWorker));

            waitFor(new StateCondition<MulticastClusterChannel>(new Duration("1ms").multiplyBy(numberOfMessagesPerWorker).multiplyBy(0.15)) {
                @Override
                public boolean check(@Nullable MulticastClusterChannel clusterChannel) throws Exception {
                    for (final MulticastClusterChannel channel : channels) {
                        assertThat(channel.getSendingQueueSize(), is(0));
                    }
                    assertThat(getNumberOfReceivedMessages(), is(messagesSend.size() * (channels.size() - 1)));
                    final Map<String, AtomicInteger> messageToCount = getMessageToCount();
                    for (final String messageSend : messagesSend) {
                        final AtomicInteger count = messageToCount.get(messageSend);
                        assertThat(count, isNotNull());
                        assertThat(count.get(), is(channels.size() - 1));
                    }
                    return true;
                }
            });

            _logger.info("send: " + counter.getAsDouble() + " m/s, received: " + getNumberMessagesReceivedPerSecond() + " m/s");

        } finally {
            closeQuietly(channels);
        }
    }

    @Nonnull
    protected StateCondition<MulticastClusterChannel> thatQueuesAreEmptyAndReceivedMessages(@Nonnull final List<MulticastClusterChannel> channels, @Nonnull final Pair<MulticastClusterChannel, ReceivedMessage<MulticastNode>>... messages) {
        return new StateCondition<MulticastClusterChannel>(new Duration(channels.size() * 1000)) { @Override public boolean check(@Nonnull MulticastClusterChannel clusterChannel) throws Exception {
            for (final MulticastClusterChannel channel : channels) {
                assertThat(channel.getSendingQueueSize(), is(0));
            }
            assertThat(getReceivedMessages(), containsAllItemsOf(messages));
            return true;
        }};
    }
    @Override
    @Nonnull
    protected MulticastClusterChannel channel(@Nonnull UUID uuid) throws Exception {
        final MulticastClusterChannel channel = new MulticastClusterChannel(uuid);
        final NetworkInterface loopBackInterface = networkInterfaceRepository().findOneBy(
            networkInterface().whichIsOfType(loopBack)
        );
        assertThat(loopBackInterface, isNotNull());
        channel.setAddress(new InetSocketAddress(HOST, PORT), loopBackInterface);
        channel.register(getMessageHandler());
        channel.register(getStateHandler());
        channel.setName(uuid.getLeastSignificantBits() + "");
        channel.init();
        return channel;
    }

    @Nonnull
    @Override
    protected MulticastNode createNode(@Nonnull UUID uuid) {
        return new MulticastNode((short)uuid.getLeastSignificantBits(), uuid, new InetSocketAddress(HOST, PORT));
    }

    @Override
    protected void afterMessageSend(@Nonnull String message) throws Exception {
        super.afterMessageSend(message);
        Thread.sleep(6);
    }

    @Override
    protected void afterAllChannelsCreated(@Nonnull List<MulticastClusterChannel> channels) throws Exception {
        super.afterAllChannelsCreated(channels);
        Thread.sleep(100 * channels.size());
    }
}

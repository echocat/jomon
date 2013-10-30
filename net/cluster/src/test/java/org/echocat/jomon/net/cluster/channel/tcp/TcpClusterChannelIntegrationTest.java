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

import org.apache.commons.lang3.tuple.Pair;
import org.echocat.jomon.net.FreeTcpPortDetector;
import org.echocat.jomon.net.cluster.channel.ClusterChannelTestSupport;
import org.echocat.jomon.net.cluster.channel.ReceivedMessage;
import org.echocat.jomon.net.cluster.channel.StateCondition;
import org.echocat.jomon.runtime.concurrent.StopWatch;
import org.echocat.jomon.runtime.math.OverPeriodCounter;
import org.echocat.jomon.runtime.util.Duration;
import org.echocat.jomon.testing.concurrent.ParallelTestRunner.Worker;
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
import static org.echocat.jomon.net.cluster.channel.ClusterChannelUtils.formatNodesStatusOf;
import static org.echocat.jomon.testing.Assert.assertThat;
import static org.echocat.jomon.testing.BaseMatchers.*;
import static org.echocat.jomon.testing.CollectionMatchers.containsAllItemsOf;
import static org.echocat.jomon.testing.CollectionMatchers.hasSize;
import static org.echocat.jomon.testing.concurrent.ParallelTestRunner.run;

public class TcpClusterChannelIntegrationTest extends ClusterChannelTestSupport<UUID, TcpNode, TcpClusterChannel> {

    protected static final Random RANDOM = new SecureRandom();
    protected static final String HOST = "230." + RANDOM.nextInt(255) + "." + RANDOM.nextInt(255) + "." + (RANDOM.nextInt(254) + 1);
    protected static final int PORT = 5000 + RANDOM.nextInt(60000);

    private final Logger _logger = LoggerFactory.getLogger(TcpClusterChannel.class);

    public TcpClusterChannelIntegrationTest() {
        super(TcpClusterChannel.class);
    }

    @Test
    public void test3Instances() throws Exception {
        final List<TcpClusterChannel> channels = channels(U1, U2, U3);
        try {
            channels.get(0).send(message("message1"));
            waitFor(thatQueuesAreEmptyAndReceivedMessages(channels, message(channels.get(1), "message1", U1), message(channels.get(1), "message1", U1)));
            resetMessageHandler();

            channels.get(1).send(message("message2"));
            waitFor(thatQueuesAreEmptyAndReceivedMessages(channels, message(channels.get(0), "message2", U2), message(channels.get(2), "message2", U2)));

            for (TcpClusterChannel channel : channels) {
                _logger.info("Nodes status of (" + channel + "):\n" + formatNodesStatusOf(channel));
            }
        } finally {
            closeQuietly(channels);
        }
    }

    @Test
    public void test1InstanceConcurrent() throws Exception {
        final TcpClusterChannel channel = channel(U1);
        channel.init();
        try {
            final int numberOfWorkers = 5;
            final int numberOfMessages = 1000;
            final Set<String> messagesSend = Collections.synchronizedSet(new HashSet<String>());
            final List<Worker> workers = createWorkersFor(numberOfWorkers, numberOfMessages, messagesSend, channel, null);
            final StopWatch stopWatch = new StopWatch();
            run(workers);
            assertThat(stopWatch.getCurrentDuration(), isLessThan(new Duration("7ms").multiplyBy(numberOfMessages)));
            assertThat(messagesSend, hasSize(numberOfWorkers * numberOfMessages));

            _logger.info("Nodes status of (" + channel + "):\n" + formatNodesStatusOf(channel));
        } finally {
            closeQuietly(channel);
        }
    }

    @Test
    public void test6InstancesConcurrent() throws Exception {
        final List<TcpClusterChannel> channels = channels(U1, U2, U3, U4, U5, U6);
        try {
            final int numberOfWorkersPerChannel = 5;
            final int numberOfMessagesPerWorker = 250;
            final Set<String> messagesSend = Collections.synchronizedSet(new HashSet<String>());
            final OverPeriodCounter counter = new OverPeriodCounter(new Duration("1m"), new Duration("1s"));
            final List<Worker> workers = createWorkersFor(numberOfWorkersPerChannel, numberOfMessagesPerWorker, messagesSend, channels, counter);
            final StopWatch stopWatch = new StopWatch();
            run(workers);

            assertThat(stopWatch.getCurrentDuration(), isLessThan(new Duration("2ms").multiplyBy(messagesSend.size())));
            assertThat(messagesSend, hasSize(channels.size() * numberOfWorkersPerChannel * numberOfMessagesPerWorker));

            waitFor(new StateCondition<TcpClusterChannel>(new Duration("1ms").multiplyBy(numberOfMessagesPerWorker).multiplyBy(0.25)) {
                @Override
                public boolean check(@Nullable TcpClusterChannel clusterChannel) throws Exception {
                    assertThat(getNumberOfReceivedMessages(), is(messagesSend.size() * (channels.size() - 1)));
                    final Map<String, AtomicInteger> messageToCount = getMessageToCount();
                    for (String messageSend : messagesSend) {
                        final AtomicInteger count = messageToCount.get(messageSend);
                        assertThat(count, isNotNull());
                        assertThat(count.get(), is(channels.size() - 1));
                    }
                    return true;
                }
            });

            for (TcpClusterChannel channel : channels) {
                _logger.info("Nodes status of (" + channel + "):\n" + formatNodesStatusOf(channel));
            }
            _logger.info("send: " + counter.getAsDouble() + " m/s, received: " + getNumberMessagesReceivedPerSecond() + " m/s");

        } finally {
            closeQuietly(channels);
        }
    }

    @Nonnull
    protected StateCondition<TcpClusterChannel> thatQueuesAreEmptyAndReceivedMessages(@Nonnull final List<TcpClusterChannel> channels, @Nonnull final Pair<TcpClusterChannel, ReceivedMessage<TcpNode>>... messages) {
        return new StateCondition<TcpClusterChannel>(new Duration(channels.size() * 1000)) { @Override public boolean check(@Nonnull TcpClusterChannel clusterChannel) throws Exception {
            assertThat(getReceivedMessages(), containsAllItemsOf(messages));
            return true;
        }};
    }
    @Override
    @Nonnull
    protected TcpClusterChannel channel(@Nonnull UUID uuid) throws Exception {
        final TcpClusterChannel channel = new TcpClusterChannel(uuid);
        final NetworkInterface loopBackInterface = networkInterfaceRepository().findOneBy(
            networkInterface().whichIsOfType(loopBack)
        );
        final int port = new FreeTcpPortDetector(loopBackInterface, 10000, 50000).detect();
        channel.setAddress(new InetSocketAddress(port), loopBackInterface);
        channel.register(getMessageHandler());
        channel.register(getStateHandler());
        channel.setName(uuid.getLeastSignificantBits() + "");
        return channel;
    }

    @Override
    protected void afterAllChannelsCreated(@Nonnull List<TcpClusterChannel> channels) throws Exception {
        super.afterAllChannelsCreated(channels);
        final Set<InetSocketAddress> remotes = new HashSet<>();
        for (TcpClusterChannel channel : channels) {
            remotes.add(channel.getAddress());
        }
        for (TcpClusterChannel channel : channels) {
            channel.setRemoteAddresses(remotes);
        }
        for (TcpClusterChannel channel : channels) {
            channel.init();
        }
    }

    @Nonnull
    @Override
    protected TcpNode createNode(@Nonnull final UUID uuid) {
        return new TcpNodeSupport(uuid) {
            @Nonnull
            @Override
            public InetSocketAddress getAddress() {
                return new InetSocketAddress((int) uuid.getLeastSignificantBits());
            }
        };
    }
}

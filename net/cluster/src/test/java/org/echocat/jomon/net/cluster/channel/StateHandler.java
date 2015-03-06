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

import org.echocat.jomon.net.cluster.channel.HandlerEnabledClusterChannel.MessageHandler;
import org.echocat.jomon.net.cluster.channel.HandlerEnabledClusterChannel.PresenceHandler;
import org.echocat.jomon.runtime.concurrent.StopWatch;
import org.echocat.jomon.runtime.util.Duration;
import org.echocat.jomon.runtime.util.GotInterruptedException;

import javax.annotation.Nonnull;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class StateHandler<C extends ClusterChannel<?, ?>> implements MessageHandler, PresenceHandler {

    private final BlockingDeque<C> _events = new LinkedBlockingDeque<>();
    private final Class<C> _expectedChannelType;

    public StateHandler(@Nonnull Class<C> expectedChannelType) {
        _expectedChannelType = expectedChannelType;
    }

    @Override
    public void handle(@Nonnull HandlerEnabledClusterChannel<?, ?> channel, @Nonnull ReceivedMessage<?> receivedMessage) {
        put(channel);
    }

    @Override
    public void nodeEnter(@Nonnull HandlerEnabledClusterChannel<?, ?> clusterChannel, @Nonnull Node<?> node) {
        put(clusterChannel);
    }

    @Override
    public void nodeLeft(@Nonnull HandlerEnabledClusterChannel<?, ?> clusterChannel, @Nonnull Node<?> node) {
        put(clusterChannel);
    }

    protected void waitFor(@Nonnull StateCondition<C> condition) throws Exception {
        final StopWatch stopWatch = new StopWatch();
        final Duration maxWaitTime = condition.getMaxWaitTime();
        Duration next = getLeftDuration(maxWaitTime, stopWatch);
        boolean finished = false;
        AssertionError lastAssertionError = null;
        try {
            finished = condition.check(null);
        } catch (final AssertionError e) {
            lastAssertionError = e;
        }
        while (next.isGreaterThan(0) && !finished) {
            final C channel = _events.poll(100, MILLISECONDS);
            if (channel != null) {
                try {
                    finished = condition.check(channel);
                } catch (final AssertionError e) {
                    lastAssertionError = e;
                }
            }
            next = getLeftDuration(maxWaitTime, stopWatch);
        }
        if (!finished) {
            try {
                finished = condition.check(null);
            } catch (final AssertionError e) {
                lastAssertionError = e;
            }
        }
        if (!finished) {
            if (lastAssertionError instanceof AssertionError) {
                throw lastAssertionError;
            } else {
                throw new RuntimeException("Does not reach condition " + condition + " in " + stopWatch.getCurrentDuration() + ".", lastAssertionError);
            }
        }
    }

    @Nonnull
    protected Duration getLeftDuration(@Nonnull Duration maxDuration, @Nonnull StopWatch basedOn) {
        final long current = basedOn.getCurrentDuration().in(MILLISECONDS);
        final long max = maxDuration.in(MILLISECONDS);
        return new Duration(max > current ? max - current : 0);
    }

    protected void put(@Nonnull ClusterChannel<?, ?> channel) {
        try {
            _events.put(_expectedChannelType.cast(channel));
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GotInterruptedException(e);
        }
    }

}

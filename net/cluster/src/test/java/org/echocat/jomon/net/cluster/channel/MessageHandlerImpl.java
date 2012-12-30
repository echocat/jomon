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

package org.echocat.jomon.net.cluster.channel;

import org.echocat.jomon.net.cluster.channel.HandlerEnabledClusterChannel.MessageHandler;
import org.echocat.jomon.runtime.math.OverPeriodCounter;
import org.echocat.jomon.runtime.util.Duration;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessageHandlerImpl<ID, N extends Node<ID>, C extends ClusterChannel<ID, N>> implements MessageHandler {

    private final List<Pair<C, ReceivedMessage<N>>> _messages = Collections.synchronizedList(new ArrayList<Pair<C, ReceivedMessage<N>>>());
    private volatile OverPeriodCounter _messagesReceivedCounter = new OverPeriodCounter(new Duration("1m"), new Duration("1s"));

    @Override
    public void handle(@Nonnull HandlerEnabledClusterChannel<?, ?> channel, @Nonnull ReceivedMessage<?> receivedMessage) {
        // noinspection unchecked
        _messages.add((Pair<C, ReceivedMessage<N>>) new ImmutablePair(channel, receivedMessage));
        _messagesReceivedCounter.record();
    }

    @Nonnull
    public List<Pair<C, ReceivedMessage<N>>> getReceivedMessages() {
        synchronized (_messages) {
            return new ArrayList<>(_messages);
        }
    }

    @Nonnegative
    public int getNumberOfReceivedMessages() {
        return _messages.size();
    }

    @Nonnegative
    public double getNumberMessagesReceivedPerSecond() {
        return _messagesReceivedCounter.getAsDouble();
    }

    public void reset() {
        _messages.clear();
        _messagesReceivedCounter = new OverPeriodCounter(new Duration("1m"), new Duration("1s"));
    }
}

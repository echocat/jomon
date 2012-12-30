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

package org.echocat.jomon.net.cluster.cache;

import org.echocat.jomon.cache.management.CacheRepository;
import org.echocat.jomon.net.cluster.channel.HandlerEnabledClusterChannel;
import org.echocat.jomon.net.cluster.channel.Node;
import org.echocat.jomon.net.cluster.channel.ReceivedMessage;
import org.mockito.internal.verification.Times;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.nio.charset.Charset;
import java.util.UUID;

import static org.echocat.jomon.net.cluster.cache.CacheListenerForClusterChannelSupport.CHARSET;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public abstract class ClusterListenerTestSupport {

    protected Times wasInvokedTimes(@Nonnegative int number) {
        return new Times(number);
    }

    @Nonnull
    protected Node<?> node() {
        final Node<?> node = mock(Node.class);
        doReturn(1).when(node).getId();
        doReturn(new UUID(0, 1)).when(node).getUuid();
        return node;
    }

    @Nonnull
    protected HandlerEnabledClusterChannel<?, ?> clusterChannel() {
        return mock(HandlerEnabledClusterChannel.class);
    }

    @Nonnull
    protected CacheRepository cacheRepository() {
        return mock(CacheRepository.class);
    }

    @Nonnull
    protected ReceivedMessage<Node<?>> message(byte command, @Nonnull String message) {
        return message(command, message, node());
    }

    @Nonnull
    protected ReceivedMessage<Node<?>> message(byte command, @Nonnull String message, @Nonnull Node<?> node) {
        return message(command, message, CHARSET, node);
    }

    @Nonnull
    protected ReceivedMessage<Node<?>> message(byte command, @Nonnull String message, @Nonnull Charset charset, @Nonnull Node<?> node) {
        return new ReceivedMessage<Node<?>>(command, message, charset, node);
    }

}


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

package org.echocat.jomon.net.cluster.cache;

import org.echocat.jomon.cache.*;
import org.echocat.jomon.cache.management.CacheRepository;
import org.echocat.jomon.net.cluster.channel.HandlerEnabledClusterChannel;
import org.echocat.jomon.net.cluster.channel.HandlerEnabledClusterChannel.MessageHandler;
import org.echocat.jomon.net.cluster.channel.Message;
import org.echocat.jomon.net.cluster.channel.Node;
import org.echocat.jomon.net.cluster.channel.ReceivedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

import static org.echocat.jomon.cache.CacheUtils.assertValidCacheId;
import static org.echocat.jomon.net.cluster.cache.CacheClusterChannelConstants.clearCommand;

public class ClearOverClusterListener extends CacheListenerForClusterChannelSupport implements ClearableCacheListener {

    private static final Logger LOG = LoggerFactory.getLogger(ClearOverClusterListener.class);

    private final MessageHandler _messageHandler = new MessageHandler() { @Override public void handle(@Nonnull HandlerEnabledClusterChannel<?, ?> clusterChannel, @Nonnull ReceivedMessage<?> message) {
        if (message.getCommand() == clearCommand) {
            final String cacheId = message.getDataAsString(CHARSET);
            clear(cacheId, message.getFrom());
        }
    }};

    public ClearOverClusterListener(@Nonnull CacheRepository cacheRepository, @Nonnull HandlerEnabledClusterChannel<?, ?> clusterChannel) {
        super(cacheRepository, clusterChannel);
    }

    @Override
    @Nonnull
    protected MessageHandler getMessageHandler() {
        return _messageHandler;
    }


    @Override
    public void afterClear(@Nonnull Cache<?, ?> cache) {
        if (cache instanceof IdentifiedCache && !isPossibleEndlessLoop()) {
            final String cacheId = ((IdentifiedCache) cache).getId();
            assertValidCacheId(cacheId);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Send clear for cache: " + cacheId, createThrowableIfLogStackTraceIsNeeded());
            }
            send(new Message(clearCommand, cacheId, CHARSET));
            record(Event.afterClear);
        }
    }

    protected void clear(@Nonnull String cacheId, @Nonnull Node<?> from) {
        final Cache<Object, ?> cache = findCache(cacheId);
        if (cache instanceof ClearableCache) {
            startHandleMessage();
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Received clear for cache: " + cacheId + " - from: " + from);
                }
                ((ClearableCache) cache).clear();
            } finally {
                finishHandleMessage();
            }
        }
    }

    @Override public boolean beforeClear(@Nonnull Cache<?, ?> cache) { return true; }

    public static enum Event implements LocalTrackingEnabledCacheListener.Event {
        afterClear
    }

}

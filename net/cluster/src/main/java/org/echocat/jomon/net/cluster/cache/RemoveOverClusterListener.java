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
import javax.annotation.Nullable;

import static java.lang.Boolean.TRUE;
import static org.echocat.jomon.cache.CacheUtils.assertValidCacheId;
import static org.echocat.jomon.net.cluster.cache.CacheClusterChannelConstants.removeCommand;

public class RemoveOverClusterListener extends CacheListenerForClusterChannelSupport implements RemoveCacheListener {

    private static final Logger LOG = LoggerFactory.getLogger(RemoveOverClusterListener.class);

    public static final String LOG_STACK_TRACE_PROPERTY_NAME = RemoveOverClusterListener.class.getName() + ".logStackTrace";

    private final MessageHandler _messageHandler = new MessageHandler() { @Override public void handle(@Nonnull HandlerEnabledClusterChannel<?, ?> clusterChannel, @Nonnull ReceivedMessage<?> message) {
        if (message.getCommand() == removeCommand) {
            final String removeMessage = message.getDataAsString(CHARSET);
            final int fistIndex = removeMessage.indexOf(';');
            if (fistIndex >= 1 && fistIndex < removeMessage.length() + 1) {
                final String cacheId = removeMessage.substring(0, fistIndex);
                final String key = removeMessage.substring(fistIndex + 1);
                remove(cacheId, key, message.getFrom());
            }
        }
    }};

    public RemoveOverClusterListener(@Nonnull CacheRepository cacheRepository, @Nonnull HandlerEnabledClusterChannel<?, ?> clusterChannel) {
        super(cacheRepository, clusterChannel);
    }

    @Nonnull
    @Override
    protected MessageHandler getMessageHandler() {
        return _messageHandler;
    }

    @Override
    public void afterRemove(@Nonnull Cache<?, ?> cache, @Nullable Object key, @Nullable Value<?> oldValue) {
        // noinspection ObjectEquality
        if (cache instanceof IdentifiedCache && key != null && !isPossibleEndlessLoop()) {
            if (key instanceof String || key instanceof Integer || key instanceof Long || key instanceof Boolean) {
                final String cacheId = ((IdentifiedCache) cache).getId();
                assertValidCacheId(cacheId);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Send remove for object: " + cacheId + "/" + key, createThrowableIfLogStackTraceIsNeeded());
                }
                send(new Message(removeCommand, cacheId + ";" + key, CHARSET));
                record(Event.afterRemove);
            } else {
                LOG.warn("Could only handle key of type string. Invalid key was: " + key, createThrowableIfLogStackTraceIsNeeded());
            }
        }
    }

    protected void remove(@Nonnull String cacheId, @Nonnull String key, @Nonnull Node<?> from) {
        final Cache<Object, ?> cache = findCache(cacheId);
        if (cache != null) {
            final Object realKey;
            final Class<?> keyType = cache.getKeyType();
            if (Integer.class.equals(keyType)) {
                realKey = Integer.valueOf(key);
            } else if (Long.class.equals(keyType)) {
                realKey = Long.valueOf(key);
            } else if (Boolean.class.equals(keyType)) {
                realKey = TRUE.toString().equalsIgnoreCase(key);
            } else if (String.class.equals(keyType)) {
                realKey = key;
            } else {
                throw new IllegalArgumentException("Could not handle key '" + key + "' for cache " + cache + ".");
            }
            startHandleMessage();
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Received remove for object: " + cacheId + "/" + realKey + " - from: " + from);
                }
                cache.remove(realKey);
            } finally {
                finishHandleMessage();
            }
        }
    }

    @Override
    public boolean beforeRemove(@Nonnull Cache<?, ?> cache, @Nullable Object key) { return true; }

    public static enum Event implements LocalTrackingEnabledCacheListener.Event {
        afterRemove
    }

}

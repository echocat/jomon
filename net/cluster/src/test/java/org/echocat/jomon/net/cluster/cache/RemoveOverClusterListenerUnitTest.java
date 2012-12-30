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

import org.echocat.jomon.cache.Cache;
import org.echocat.jomon.cache.CacheListener;
import org.echocat.jomon.cache.LruCache;
import org.echocat.jomon.cache.Value;
import org.echocat.jomon.cache.management.CacheRepository;
import org.echocat.jomon.net.cluster.channel.HandlerEnabledClusterChannel;
import org.echocat.jomon.net.cluster.channel.Message;
import org.echocat.jomon.testing.environments.LogEnvironment;
import org.junit.Rule;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.echocat.jomon.net.cluster.cache.CacheClusterChannelConstants.clearCommand;
import static org.echocat.jomon.net.cluster.cache.CacheClusterChannelConstants.removeCommand;
import static org.echocat.jomon.testing.Assert.assertThat;
import static org.echocat.jomon.testing.BaseMatchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class RemoveOverClusterListenerUnitTest extends ClusterListenerTestSupport {

    @Rule
    public final LogEnvironment _logEnvironment = new LogEnvironment();

    @Test
    public void testHandleOfWrongCommand() throws Exception {
        final HandlerEnabledClusterChannel<?, ?> clusterChannel = clusterChannel();
        final CacheRepository cacheRepository = cacheRepository();
        final RemoveOverClusterListener listener = new RemoveOverClusterListener(cacheRepository, clusterChannel);
        listener.getMessageHandler().handle(clusterChannel, message(clearCommand, "foo;bar"));
        verify(cacheRepository, wasInvokedTimes(0)).find(anyString());
    }

    @Test
    public void testHandleOfWrongMessage() throws Exception {
        final HandlerEnabledClusterChannel<?, ?> clusterChannel = clusterChannel();
        final CacheRepository cacheRepository = cacheRepository();
        final RemoveOverClusterListener listener = new RemoveOverClusterListener(cacheRepository, clusterChannel);
        listener.getMessageHandler().handle(clusterChannel, message(removeCommand, "foo.bar"));
        verify(cacheRepository, wasInvokedTimes(0)).find(anyString());
    }

    @Test
    public void testHandleOfWithMissingCache() throws Exception {
        final HandlerEnabledClusterChannel<?, ?> clusterChannel = clusterChannel();
        final CacheRepository cacheRepository = cacheRepository();
        final RemoveOverClusterListener listener = new RemoveOverClusterListener(cacheRepository, clusterChannel);
        listener.getMessageHandler().handle(clusterChannel, message(removeCommand, "foo;bar"));
        verify(cacheRepository, wasInvokedTimes(1)).find("foo");
        verify(cacheRepository, wasInvokedTimes(1)).find(anyString());
    }

    @Test
    public void testHandleOfWithFoundCacheAndStringKey() throws Exception {
        // noinspection unchecked
        final Cache<String, ?> cache = mock(Cache.class);
        doReturn(String.class).when(cache).getKeyType();
        final HandlerEnabledClusterChannel<?, ?> clusterChannel = clusterChannel();
        final CacheRepository cacheRepository = cacheRepository();
        doReturn(cache).when(cacheRepository).find("foo");
        final RemoveOverClusterListener listener = new RemoveOverClusterListener(cacheRepository, clusterChannel);
        listener.getMessageHandler().handle(clusterChannel, message(removeCommand, "foo;bar"));
        verify(cacheRepository, wasInvokedTimes(1)).find("foo");
        verify(cacheRepository, wasInvokedTimes(1)).find(anyString());
        verify(cache, wasInvokedTimes(1)).remove("bar");
        verify(cache, wasInvokedTimes(1)).remove(anyString());
    }

    @Test
    public void testHandleOfWithFoundCacheAndIntegerKey() throws Exception {
        // noinspection unchecked
        final Cache<Object, ?> cache = mock(Cache.class);
        doReturn(Integer.class).when(cache).getKeyType();
        final HandlerEnabledClusterChannel<?, ?> clusterChannel = clusterChannel();
        final CacheRepository cacheRepository = cacheRepository();
        doReturn(cache).when(cacheRepository).find("foo");
        final RemoveOverClusterListener listener = new RemoveOverClusterListener(cacheRepository, clusterChannel);
        listener.getMessageHandler().handle(clusterChannel, message(removeCommand, "foo;1"));
        verify(cacheRepository, wasInvokedTimes(1)).find("foo");
        verify(cacheRepository, wasInvokedTimes(1)).find(anyString());
        verify(cache, wasInvokedTimes(1)).remove(1);
        verify(cache, wasInvokedTimes(1)).remove(anyObject());
    }

    @Test
    public void testHandleOfWithFoundCacheAndLongKey() throws Exception {
        // noinspection unchecked
        final Cache<Object, ?> cache = mock(Cache.class);
        doReturn(Long.class).when(cache).getKeyType();
        final HandlerEnabledClusterChannel<?, ?> clusterChannel = clusterChannel();
        final CacheRepository cacheRepository = cacheRepository();
        doReturn(cache).when(cacheRepository).find("foo");
        final RemoveOverClusterListener listener = new RemoveOverClusterListener(cacheRepository, clusterChannel);
        listener.getMessageHandler().handle(clusterChannel, message(removeCommand, "foo;1"));
        verify(cacheRepository, wasInvokedTimes(1)).find("foo");
        verify(cacheRepository, wasInvokedTimes(1)).find(anyString());
        verify(cache, wasInvokedTimes(1)).remove(1L);
        verify(cache, wasInvokedTimes(1)).remove(anyObject());
    }

    @Test
    public void testHandleOfWithFoundCacheAndBooleanKey() throws Exception {
        // noinspection unchecked
        final Cache<Object, ?> cache = mock(Cache.class);
        doReturn(Boolean.class).when(cache).getKeyType();
        final HandlerEnabledClusterChannel<?, ?> clusterChannel = clusterChannel();
        final CacheRepository cacheRepository = cacheRepository();
        doReturn(cache).when(cacheRepository).find("foo");
        final RemoveOverClusterListener listener = new RemoveOverClusterListener(cacheRepository, clusterChannel);
        listener.getMessageHandler().handle(clusterChannel, message(removeCommand, "foo;true"));
        verify(cacheRepository, wasInvokedTimes(1)).find("foo");
        verify(cacheRepository, wasInvokedTimes(1)).find(anyString());
        verify(cache, wasInvokedTimes(1)).remove(true);
        verify(cache, wasInvokedTimes(1)).remove(anyObject());
    }

    @Test
    public void testAvoidEndlessLoop() throws Exception {
        final AtomicInteger numberOfAfterRemovedCalls = new AtomicInteger();
        final LruCache<String, ?> cache = new LruCache<>(String.class, Object.class);
        cache.setId("foo");
        final HandlerEnabledClusterChannel<?, ?> clusterChannel = clusterChannel();
        final CacheRepository cacheRepository = cacheRepository();
        doReturn(cache).when(cacheRepository).find("foo");
        final RemoveOverClusterListener listener = new RemoveOverClusterListener(cacheRepository, clusterChannel) { @Override public void afterRemove(@Nonnull Cache<?, ?> cache, @Nullable Object key, @Nullable Value<?> oldValue) {
            super.afterRemove(cache, key, oldValue);
            numberOfAfterRemovedCalls.incrementAndGet();
        }};
        cache.setListeners(Arrays.<CacheListener>asList(listener));
        listener.getMessageHandler().handle(clusterChannel, message(removeCommand, "foo;bar"));
        verify(cacheRepository, wasInvokedTimes(1)).find("foo");
        verify(cacheRepository, wasInvokedTimes(1)).find(anyString());
        verify(clusterChannel, wasInvokedTimes(0)).send(any(Message.class));
        assertThat(numberOfAfterRemovedCalls.get(), is(1));
        cache.remove("bar");
        verify(clusterChannel, wasInvokedTimes(1)).send(any(Message.class));
        assertThat(numberOfAfterRemovedCalls.get(), is(2));
    }

    @Test
    public void testWithoutAvoidEndlessLoop() throws Exception {
        final AtomicInteger numberOfAfterRemovedCalls = new AtomicInteger();
        final LruCache<String, ?> cache = new LruCache<>(String.class, Object.class);
        cache.setId("foo");
        final HandlerEnabledClusterChannel<?, ?> clusterChannel = clusterChannel();
        final CacheRepository cacheRepository = cacheRepository();
        doReturn(cache).when(cacheRepository).find("foo");
        final RemoveOverClusterListener listener = new RemoveOverClusterListener(cacheRepository, clusterChannel) { @Override public void afterRemove(@Nonnull Cache<?, ?> cache, @Nullable Object key, @Nullable Value<?> oldValue) {
            getInHandleMessage().remove();
            super.afterRemove(cache, key, oldValue);
            numberOfAfterRemovedCalls.incrementAndGet();
        }};
        cache.setListeners(Arrays.<CacheListener>asList(listener));
        listener.getMessageHandler().handle(clusterChannel, message(removeCommand, "foo;bar"));
        verify(cacheRepository, wasInvokedTimes(1)).find("foo");
        verify(cacheRepository, wasInvokedTimes(1)).find(anyString());
        verify(clusterChannel, wasInvokedTimes(1)).send(any(Message.class));
        assertThat(numberOfAfterRemovedCalls.get(), is(1));
        cache.remove("bar");
        verify(clusterChannel, wasInvokedTimes(2)).send(any(Message.class));
        assertThat(numberOfAfterRemovedCalls.get(), is(2));
    }

    @Test
    public void testInitAndClose() throws Exception {
        final HandlerEnabledClusterChannel<?, ?> clusterChannel = clusterChannel();
        final RemoveOverClusterListener listener = new RemoveOverClusterListener(cacheRepository(), clusterChannel);
        verify(clusterChannel, wasInvokedTimes(0)).register(listener.getMessageHandler());
        listener.init();
        verify(clusterChannel, wasInvokedTimes(1)).register(listener.getMessageHandler());

        verify(clusterChannel, wasInvokedTimes(0)).unregister(listener.getMessageHandler());
        listener.close();
        verify(clusterChannel, wasInvokedTimes(1)).register(listener.getMessageHandler());
    }

}


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

package org.echocat.jomon.runtime.io;

import javax.annotation.Nonnull;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;
import java.util.List;

import static java.net.URL.setURLStreamHandlerFactory;
import static java.util.ServiceLoader.load;
import static org.echocat.jomon.runtime.CollectionUtils.addAll;
import static org.echocat.jomon.runtime.CollectionUtils.asImmutableList;

public class UrlUtils {

    private static final Iterable<UrlStreamHandlerFactory> URL_STREAM_HANDLER_FACTORIES = loadUrlStreamHandlerFactories();

    private static volatile boolean c_streamHandlerRegistered;

    public static void registerUrlStreamHandlerIfNeeded() {
        if (!c_streamHandlerRegistered) {
            c_streamHandlerRegistered = true;
            setURLStreamHandlerFactory(new URLStreamHandlerFactoryImpl());
        }
    }

    private static class URLStreamHandlerFactoryImpl implements URLStreamHandlerFactory {

        @Override
        public URLStreamHandler createURLStreamHandler(String protocol) {
            URLStreamHandler result = null;
            for (final UrlStreamHandlerFactory factory : URL_STREAM_HANDLER_FACTORIES) {
                result = factory.createURLStreamHandler(protocol);
                if (result != null) {
                    break;
                }
            }
            return result;
        }

    }

    @Nonnull
    private static Iterable<UrlStreamHandlerFactory> loadUrlStreamHandlerFactories() {
        final List<UrlStreamHandlerFactory> result = new ArrayList<>();
        addAll(result, load(UrlStreamHandlerFactory.class));
        return asImmutableList(result);
    }

    private UrlUtils() {}

}

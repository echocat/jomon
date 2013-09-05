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

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

import static java.net.URL.setURLStreamHandlerFactory;

public class UrlUtils {

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
            final URLStreamHandler result;
            if ("classpath".equals(protocol)) {
                result = new ClasspathURLStreamHandler();
            } else {
                result = null;
            }
            return result;
        }

    }

    private UrlUtils() {}

}

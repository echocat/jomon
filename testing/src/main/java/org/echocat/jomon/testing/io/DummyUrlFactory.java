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

package org.echocat.jomon.testing.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.MalformedURLException;

/**
 * This class provides a dummy instance of {@link URL} from wich can read via {@link URL#openStream()}.
 */
public class DummyUrlFactory {

    public static final String PROTOCOL_PREFIX = "dummyurl";

    private DummyUrlFactory() {
    }

    /**
     * Create a new dummy instance of {@link URL} for the given inputStream.
     * Note: Urls which are created in this way could only read one time.
     *
     * @param inputStream to create the new {@link URL} instance for.
     */
    public static URL get(InputStream inputStream) throws MalformedURLException {
        return get(inputStream, PROTOCOL_PREFIX + ":" + inputStream);
    }

    /**
     * Create a new dummy instance of {@link URL} for the given inputStream.
     * Note: Urls which are created in this way could only read one time.
     *
     * @param inputStream to create the new {@link URL} instance for.
     * @param urlStr which is dispayed when the {@link URL#toString()} is used.
     */
    public static URL get(InputStream inputStream, String urlStr) throws MalformedURLException {
        return new URL(null, urlStr, new StreamHandler(inputStream));
    }

    /**
     * Create a new dummy instance of {@link URL} for the given string with encoding.
     * Note: Urls which are created in this way could read multible times.
     *
     * @param string which is auqired out when {@link URL#openStream()} is called.
     * @param encoding which is used to encode the string to stream when {@link URL#openStream()} is called.
     */
    public static URL get(String string, String encoding) throws MalformedURLException {
        return get(string, encoding, PROTOCOL_PREFIX + ":" + string.hashCode());
    }

    /**
     * Create a new dummy instance of {@link URL} for the given string with encoding.
     * Note: Urls which are created in this way could read multible times.
     *
     * @param string which is auqired out when {@link URL#openStream()} is called.
     * @param encoding which is used to encode the string to stream when {@link URL#openStream()} is called.
     * @param urlStr which is dispayed when the {@link URL#toString()} is used.
     */
    public static URL get(String string, String encoding, String urlStr) throws MalformedURLException {
        return new URL(null, urlStr, new StreamHandler(string, encoding));
    }

    public static class StreamHandler extends URLStreamHandler {

        private InputStream _inputstream;
        private String _string;
        private String _encoding;

        private StreamHandler(InputStream inputstream) {
            _inputstream = inputstream;
        }

        private StreamHandler(String string, String encoding) {
            _string = string;
            _encoding = encoding;
        }

        @Override
        protected URLConnection openConnection(URL u) throws IOException {
            final URLConnection result;
            if (_inputstream != null) {
                result = new Connection(u, _inputstream);
            } else {
                result = new Connection(u, _string, _encoding);
            }
            return result;
        }
    }

    public static class Connection extends URLConnection {

        private InputStream _inputStream;
        private String _string;
        private String _encoding;

        protected Connection(URL u, InputStream inputStream) {
            super(u);
            _inputStream = inputStream;
        }

        protected Connection(URL u, String string, String encoding) {
            super(u);
            _string = string;
            _encoding = encoding;
        }

        @Override
        public void connect() throws IOException {
        }

        @Override
        public InputStream getInputStream() throws IOException {
            final InputStream result;
            if (_inputStream != null) {
                result = _inputStream;
            } else {
                result = new ByteArrayInputStream(_string.getBytes(_encoding));
            }
            return result;
        }


        @Override
        public String getContentEncoding() {
            final String result;
            if (_inputStream != null) {
                result = null;
            } else {
                result = _encoding;
            }
            return result;
        }

        @Override
        public String getContentType() {
            final String result;
            if (_inputStream != null) {
                result = "application/binary";
            } else {
                result = "text/plain";
            }
            return result;
        }

        @Override
        public int getContentLength() {
            final int result;
            if (_inputStream != null) {
                result = -1;
            } else {
                result = _string.length();
            }
            return result;
        }
    }
}

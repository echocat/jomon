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

package org.echocat.jomon.net.http;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import static java.nio.charset.Charset.forName;

public class HttpUtils {
    
    private static final HttpClient CLIENT = new DefaultHttpClient();

    @Nonnull
    public static Reader makeGetRequestAndReturnBodyReaderFor(@Nonnull URL url) throws IOException {
        final HttpResponse response = makeGetRequestAndReturnResponseFor(url);
        return toReader(response);
    }

    @Nonnull
    public static Reader toReader(@Nonnull HttpResponse response) throws IOException {
        if (wasResponseSuccessful(response)) {
            return createBodyReaderFor(response.getEntity());
        } else {
            throw makeIoExceptionFrom(response);
        }
    }

    @Nonnull
    public static InputStream toInputStream(@Nonnull HttpResponse response) throws IOException {
        if (wasResponseSuccessful(response)) {
            return createBodyInputStreamFor(response.getEntity());
        } else {
            throw makeIoExceptionFrom(response);
        }
    }

    @Nonnull
    public static String toString(@Nonnull HttpResponse response) throws IOException {
        try (final Reader reader = toReader(response)) {
            return IOUtils.toString(reader);
        }
    }

    @Nonnull
    public static String makeGetRequestAndReturnBodyAsStringFor(@Nonnull URL url) throws IOException {
        try (final Reader reader = makeGetRequestAndReturnBodyReaderFor(url)) {
            return IOUtils.toString(reader);
        }
    }

    @Nonnull
    public static HttpResponse makeGetRequestAndReturnResponseFor(@Nonnull URL url) throws IOException {
        final HttpUriRequest httpUriRequest = new HttpGet(url.toExternalForm());
        return CLIENT.execute(httpUriRequest);
    }
    
    
    @Nonnull
    public static HttpGet makeGetRequestFor(@Nonnull URL url) throws IOException {
        return new HttpGet(url.toExternalForm());
    }

    public static boolean wasResponseSuccessful(@Nonnull HttpResponse response) {
        final StatusLine statusLine = response.getStatusLine();
        final boolean result;
        if (statusLine != null) {
            final int statusCode = statusLine.getStatusCode();
            result = statusCode < 400; 
        } else {
            result = false;
        }
        return result;
    }

    @Nonnull
    public static HttpResponseException makeIoExceptionFrom(@Nonnull HttpResponse response) throws IOException {
        final int statusCode;
        final String statusMessage;
        final StatusLine statusLine = response.getStatusLine();
        if (statusLine != null) {
            statusCode = statusLine.getStatusCode();
            statusMessage = statusLine.getReasonPhrase();
        } else {
            statusCode = 0;
            statusMessage = null;
        }
        final String body;
        try (final Reader bodyReader = createBodyReaderFor(response.getEntity())) {
            body = IOUtils.toString(bodyReader);
        }
        return new HttpResponseException(statusCode, statusMessage, body);
    }
    
    @Nonnull
    public static Reader createBodyReaderFor(@Nonnull HttpEntity entity) throws IOException {
        Charset charset = findCharSetOf(entity);
        if (charset == null) {
            charset = forName("ISO-8859-1");
        }
        return new InputStreamReader(entity.getContent(), charset);
    }

    @Nonnull
    public static InputStream createBodyInputStreamFor(@Nonnull HttpEntity entity) throws IOException {
        return entity.getContent();
    }

    @Nullable
    public static Charset findCharSetOf(@Nonnull HttpEntity entity) throws IOException {
        final MimeType mimeType;
        final Header contentTypeHeader = entity.getContentType();
        final String contentType = contentTypeHeader != null ? contentTypeHeader.getValue() : null;
        try {
            mimeType = contentType != null ? new MimeType(contentType) : null;
        } catch (MimeTypeParseException e) {
            throw new IOException("Illegal contentType: " + contentType, e);
        }
        final String charsetName = mimeType != null ? mimeType.getParameter("charset") : null;
        try {
            return charsetName != null ? forName(charsetName) : null;
        } catch (UnsupportedCharsetException e) {
            throw new IOException("In contentType '" + mimeType + "' was an unsupported charset provided.", e);
        }
    }

    @Nonnull
    public static String urlDecode(@Nonnull String encodedString) {
        try {
            return URLDecoder.decode(encodedString, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Should never happen.
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Nonnull
    public static String encodeUrl(@Nonnull String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Should never happen.
            throw new RuntimeException("UTF-8 not supported.", e);
        }
    }

    private HttpUtils() {}
}

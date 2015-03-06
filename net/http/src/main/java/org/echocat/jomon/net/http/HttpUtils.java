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

package org.echocat.jomon.net.http;

import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.echocat.jomon.runtime.CollectionUtils;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static java.net.URI.create;
import static java.net.URLEncoder.encode;
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
        final MimeType mimeType = findMimeTypeOf(entity);
        return findCharSetOf(mimeType);
    }

    @Nullable
    public static Charset findCharSetOf(@Nullable MimeType mimeType) throws IOException {
        final String charsetName = mimeType != null ? mimeType.getParameter("charset") : null;
        try {
            return charsetName != null ? forName(charsetName) : null;
        } catch (final UnsupportedCharsetException e) {
            throw new IOException("In contentType '" + mimeType + "' was an unsupported charset provided.", e);
        }
    }

    @Nullable
    public static MimeType findMimeTypeOf(@Nonnull HttpEntity entity) throws IOException {
        final MimeType mimeType;
        final Header contentTypeHeader = entity.getContentType();
        final String contentType = contentTypeHeader != null ? contentTypeHeader.getValue() : null;
        try {
            mimeType = contentType != null ? new MimeType(contentType) : null;
        } catch (final MimeTypeParseException e) {
            throw new IOException("Illegal contentType: " + contentType, e);
        }
        return mimeType;
    }

    @Nonnull
    public static String urlDecode(@Nonnull String encodedString) {
        try {
            return URLDecoder.decode(encodedString, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            // Should never happen.
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Nonnull
    public static String encodeUrl(@Nonnull String s) {
        try {
            return encode(s, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            // Should never happen.
            throw new RuntimeException("UTF-8 not supported.", e);
        }
    }

    @Nonnull
    public static HttpPost postFor(@Nonnull URI uri, @Nonnull Charset charset, @Nullable String... parameters) throws UnsupportedEncodingException {
        return postFor(uri, charset, parameters != null ? CollectionUtils.<String, String>asMap(parameters) : null);
    }

    @Nonnull
    public static HttpPost postFor(@Nonnull URI uri, @Nonnull Charset charset, @Nullable Map<String, String> parameters) throws UnsupportedEncodingException {
        final HttpPost httpPost = new HttpPost(uri);
        final List<NameValuePair> pairs = new ArrayList<>(parameters != null ? parameters.size() : 0);
        if (parameters != null) {
            for (final Entry<String, String> keyToValue : parameters.entrySet()) {
                pairs.add(new BasicNameValuePair(keyToValue.getKey(), keyToValue.getValue()));
            }
        }
        httpPost.setEntity(new UrlEncodedFormEntity(pairs, charset));
        return httpPost;
    }

    @Nonnull
    public static HttpGet getFor(@Nonnull URI uri, @Nonnull Charset charset, @Nullable String... parameters) throws UnsupportedEncodingException {
        return getFor(uri, charset, parameters != null ? CollectionUtils.<String, String>asMap(parameters) : null);
    }

    @Nonnull
    public static HttpGet getFor(@Nonnull URI uri, @Nonnull Charset charset, @Nullable Map<String, String> parameters) throws UnsupportedEncodingException {
        final HttpGet httpGet;
        if (parameters != null && !parameters.isEmpty()) {
            final StringBuilder sb = new StringBuilder();
            sb.append(uri);
            boolean questionMarkAdded = uri.getQuery() != null;
            for (final Entry<String, String> keyToValue : parameters.entrySet()) {
                if (questionMarkAdded) {
                    sb.append('&');
                } else {
                    sb.append('?');
                    questionMarkAdded = true;
                }
                sb.append(encode(keyToValue.getKey(), charset.name())).append('=').append(encode(keyToValue.getValue(), charset.name()));
            }
            httpGet = new HttpGet(create(sb.toString()));
        } else {
            httpGet = new HttpGet(uri);
        }
        return httpGet;
    }

    private HttpUtils() {}
}

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

package org.echocat.jomon.net.http.client;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;

import javax.annotation.Nonnull;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class HttpClientUtils {

    @Nonnull
    public static HttpHost toHttpHost(@Nonnull URL host) {
        try {
            return toHttpHost(host.toURI());
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException("Could not transform " + host + " to an valid uri.", e);
        }
    }

    @Nonnull
    public static HttpHost toHttpHost(@Nonnull URI host) {
        final String scheme = host.getScheme();
        final int plainPort = host.getPort();
        final int port;
        if (plainPort >= 0) {
            port = plainPort;
        } else if ("https".equals(scheme)){
            port = 443;
        } else if ("http".equals(scheme)) {
            port = 80;
        } else {
            throw new IllegalArgumentException("Illegal proxy uri provided: " + host);
        }
        return new HttpHost(host.getHost(), port, scheme);
    }

    private HttpClientUtils() {}

    @Nonnull
    public static AuthScope toAuthScope(@Nonnull URI host) {
        return toAuthScope(toHttpHost(host));
    }

    @Nonnull
    public static AuthScope toAuthScope(@Nonnull HttpHost host) {
        return new AuthScope(host.getHostName(), host.getPort());
    }
}

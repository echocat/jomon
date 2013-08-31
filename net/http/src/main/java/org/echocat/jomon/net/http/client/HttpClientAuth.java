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
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.impl.auth.NegotiateScheme;

import javax.annotation.Nonnull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@SuppressWarnings("ClassWithTooManyConstructors")
public class HttpClientAuth {

    protected static final Pattern PLAIN_PARSE_PATTERN = Pattern.compile("([a-zA-Z0-9]+:\\/\\/[^&]+)&([^&]*)&([^&]*)(?:&(.*)|)");

    private final AuthScope _scope;
    private final HttpHost _host;
    private final Credentials _credentials;
    private final AuthScheme _scheme;

    public HttpClientAuth(@Nonnull String asString) {
        final Matcher matcher = PLAIN_PARSE_PATTERN.matcher(asString);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Could not parse: " + asString);
        }
        _host = parseAuthHostOf(matcher);
        _scope = HttpClientUtils.toAuthScope(_host);
        _credentials = parseCredentialsOf(matcher);
        _scheme = parseSchemeOf(matcher);
    }

    @Nonnull
    protected HttpHost parseAuthHostOf(@Nonnull Matcher matcher) {
        try {
            final URI uri = new URI(matcher.group(1));
            return HttpClientUtils.toHttpHost(uri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Could not parse uri part of: " + matcher.group(), e);
        }
    }

    @Nonnull
    protected Credentials parseCredentialsOf(@Nonnull Matcher matcher) {
        return new UsernamePasswordCredentials(matcher.group(2), matcher.group(3));
    }

    @Nonnull
    protected AuthScheme parseSchemeOf(@Nonnull Matcher matcher) {
        final String plainAuthScheme = matcher.group(4);
        final AuthScheme authScheme;
        if ("digest".equalsIgnoreCase(plainAuthScheme)) {
            authScheme = new DigestScheme();
        } else if ("negotiate".equalsIgnoreCase(plainAuthScheme)) {
            // noinspection deprecation
            authScheme = new NegotiateScheme();
        } else if ("basic".equalsIgnoreCase(plainAuthScheme) || isEmpty(plainAuthScheme)) {
            authScheme = new BasicScheme();
        } else {
            throw new IllegalArgumentException("Unknown authScheme '" + plainAuthScheme + "' of: " + matcher.group());
        }
        return authScheme;
    }

    public HttpClientAuth(@Nonnull URI host, @Nonnull String username, @Nonnull String password) {
        this(host, new UsernamePasswordCredentials(username, password), new BasicScheme());
    }

    public HttpClientAuth(@Nonnull URI host, @Nonnull Credentials credentials) {
        this(host, credentials, new BasicScheme());
    }

    public HttpClientAuth(@Nonnull HttpHost host, @Nonnull String username, @Nonnull String password) {
        this(host, new UsernamePasswordCredentials(username, password), new BasicScheme());
    }

    public HttpClientAuth(@Nonnull HttpHost host, @Nonnull Credentials credentials) {
        this(host, credentials, new BasicScheme());
    }

    public HttpClientAuth(@Nonnull AuthScope scope, @Nonnull HttpHost host, @Nonnull String username, @Nonnull String password) {
        this(scope, host, new UsernamePasswordCredentials(username, password), new BasicScheme());
    }

    public HttpClientAuth(@Nonnull AuthScope scope, @Nonnull HttpHost host, @Nonnull Credentials credentials) {
        this(scope, host, credentials, new BasicScheme());
    }

    public HttpClientAuth(@Nonnull URI host, @Nonnull String username, @Nonnull String password, @Nonnull AuthScheme scheme) {
        this(HttpClientUtils.toHttpHost(host), new UsernamePasswordCredentials(username, password), scheme);
    }

    public HttpClientAuth(@Nonnull URI host, @Nonnull Credentials credentials, @Nonnull AuthScheme scheme) {
        this(HttpClientUtils.toHttpHost(host), credentials, scheme);
    }

    public HttpClientAuth(@Nonnull HttpHost host, @Nonnull String username, @Nonnull String password, @Nonnull AuthScheme scheme) {
        this(host, new UsernamePasswordCredentials(username, password), scheme);
    }

    public HttpClientAuth(@Nonnull HttpHost host, @Nonnull Credentials credentials, @Nonnull AuthScheme scheme) {
        this(HttpClientUtils.toAuthScope(host), host, credentials, scheme);
    }

    public HttpClientAuth(@Nonnull AuthScope scope, @Nonnull HttpHost host, @Nonnull String username, @Nonnull String password, @Nonnull AuthScheme scheme) {
        this(scope, host, new UsernamePasswordCredentials(username, password), scheme);
    }

    public HttpClientAuth(@Nonnull AuthScope scope, @Nonnull HttpHost host, @Nonnull Credentials credentials, @Nonnull AuthScheme scheme) {
        _scope = scope;
        _host = host;
        _credentials = credentials;
        _scheme = scheme;
    }

    @Nonnull
    public AuthScope getScope() {
        return _scope;
    }

    @Nonnull
    public HttpHost getHost() {
        return _host;
    }

    @Nonnull
    public URI toUri() {
        return URI.create(getHost().toURI());
    }

    @Nonnull
    public Credentials getCredentials() {
        return _credentials;
    }

    @Nonnull
    public AuthScheme getScheme() {
        return _scheme;
    }

}

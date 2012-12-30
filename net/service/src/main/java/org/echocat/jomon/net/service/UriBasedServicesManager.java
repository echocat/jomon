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

package org.echocat.jomon.net.service;

import org.echocat.jomon.runtime.util.ServiceTemporaryUnavailableException;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.UnknownHostException;

import static java.net.InetAddress.getByName;
import static java.net.InetSocketAddress.createUnresolved;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.echocat.jomon.net.Protocol.tcp;

public abstract class UriBasedServicesManager extends SrvEntryBasedServicesManager<URI, URI> {

    protected UriBasedServicesManager(@Nonnull String service) {
        super(tcp, service);
        setCheckerThreadName("CheckServiceUris for " + getService() + "@" + getProtocol().getName());
    }

    @Nullable
    @Override
    protected InetSocketAddress toInetSocketAddress(@Nonnull URI input) throws Exception {
        final String host = input.getHost();
        if (host == null) {
            throw new IllegalArgumentException("No host fond in: "+ input);
        }
        final int port = getPortFor(input);
        InetSocketAddress result;
        try {
            result = new InetSocketAddress(getByName(host), port);
        } catch (UnknownHostException ignored) {
            result = createUnresolved(host, port);
        }
        return result;
    }

    @Nonnegative
    protected int getPortFor(@Nonnull URI input) throws Exception {
        int port = input.getPort();
        if (port < 0) {
            final String scheme = input.getScheme();
            if ("http".equals(scheme)) {
                port = 80;
            } else if ("https".equals(scheme)) {
                port = 443;
            } else {
                throw new IllegalArgumentException("No port found in: " + input);
            }
        }
        return port;
    }

    @Override
    protected URI tryGetOutputFor(@Nonnull URI input, @Nonnull InetSocketAddress address, @Nonnull State oldState) throws Exception {
        final URI uri = toUri(input, address);
        checkUri(uri, oldState);
        return uri;
    }

    /**
     * @throws Exception will cause the whole process to stop. This exception is not acceptable.
     * @throws ServiceTemporaryUnavailableException will cause that this uri is marked as unavailable. Another try will follow.
     */
    @SuppressWarnings("DuplicateThrows")
    protected abstract void checkUri(@Nonnull URI uri, @Nonnull State oldState) throws Exception, ServiceTemporaryUnavailableException;

    @Nonnull
    protected URI toUri(@Nonnull URI original, @Nonnull InetSocketAddress address) {
        final String scheme = original.getScheme();
        final int port = address.getPort();
        final String userInfo = original.getRawUserInfo();
        final String path = original.getRawPath();
        final String query = original.getRawQuery();
        final String fragment = original.getRawFragment();
        final StringBuilder sb = new StringBuilder();
        sb.append(scheme).append("://");
        if (isNotEmpty(userInfo)) {
            sb.append(userInfo).append('@');
        }
        sb.append(address.getHostString());
        if (canAppendPort(scheme, port)) {
            sb.append(':').append(port);
        }
        if (isNotEmpty(path)) {
            sb.append(path);
        }
        if (isNotEmpty(query)) {
            sb.append('?').append(query);
        }
        if (isNotEmpty(fragment)) {
            sb.append('#').append(fragment);
        }
        return URI.create(sb.toString());
    }

    protected boolean canAppendPort(@Nonnull String scheme, int port) {
        return ("http".equals(scheme) && port != 80) || ("https".equals(scheme) && port != 443);
    }
}

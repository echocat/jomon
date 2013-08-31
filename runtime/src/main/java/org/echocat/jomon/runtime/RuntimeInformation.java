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

package org.echocat.jomon.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.net.InetAddress;

public class RuntimeInformation {

    private static final Logger LOG = LoggerFactory.getLogger(RuntimeInformation.class);

    @Nullable
    private final InetAddress _host = resolveHost();
    @Nullable
    private final String _hostName = resolveHostNameFor(_host);
    @Nullable
    private final String _shortHostName = resolveShortHostNameFor(_hostName);

    @Nullable
    protected InetAddress resolveHost() {
        InetAddress result;
        try {
            result = InetAddress.getLocalHost();
        } catch (Exception e) {
            result = null;
            LOG.warn("Could not resolve local host.", e);
        }
        return result;
    }

    @Nullable
    protected String resolveHostNameFor(@Nullable InetAddress host) {
        String result;
        try {
            result = host.getCanonicalHostName();
        } catch (Exception e) {
            result = null;
            LOG.warn("Could not resolve host name..", e);
        }
        return result;
    }

    @Nullable
    protected String resolveShortHostNameFor(@Nullable String hostName) {
        final String result;
        if (hostName != null) {
            final int firstDot = hostName.indexOf('.');
            if (firstDot > 0 && firstDot + 1 < hostName.length()) {
                result = hostName.substring(0, firstDot);
            } else {
                result = hostName;
            }
        } else {
            result = null;
        }
        return result;
    }

    @Nullable
    public InetAddress getHost() {
        return _host;
    }

    @Nullable
    public String getShortHostName() {
        return _shortHostName;
    }

    @Nullable
    public String getHostName() {
        return _hostName;
    }

}

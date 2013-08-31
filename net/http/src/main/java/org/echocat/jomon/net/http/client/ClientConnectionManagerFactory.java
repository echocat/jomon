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

import org.echocat.jomon.runtime.util.Duration;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.SchemeRegistryFactory;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class ClientConnectionManagerFactory {

    private Duration _timeToLife = new Duration("15s");
    private int _maximumNumberOfConnections = 20;
    private int _maximumNumberOfConnectionsPerRoute = 20;
    private SchemeRegistry _schemeRegistry = SchemeRegistryFactory.createDefault();

    @Nonnull
    public ClientConnectionManager create() {
        final PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager(_schemeRegistry, _timeToLife.toMilliSeconds(), MILLISECONDS);
        connectionManager.setDefaultMaxPerRoute(_maximumNumberOfConnectionsPerRoute);
        connectionManager.setMaxTotal(_maximumNumberOfConnections);
        return connectionManager;
    }

    @Nonnull
    public Duration getTimeToLife() {
        return _timeToLife;
    }

    public void setTimeToLife(@Nonnull Duration timeToLife) {
        _timeToLife = timeToLife;
    }

    @Nonnegative
    public int getMaximumNumberOfConnections() {
        return _maximumNumberOfConnections;
    }

    public void setMaximumNumberOfConnections(@Nonnegative int maximumNumberOfConnections) {
        _maximumNumberOfConnections = maximumNumberOfConnections;
    }

    @Nonnegative
    public int getMaximumNumberOfConnectionsPerRoute() {
        return _maximumNumberOfConnectionsPerRoute;
    }

    public void setMaximumNumberOfConnectionsPerRoute(@Nonnegative int maximumNumberOfConnectionsPerRoute) {
        _maximumNumberOfConnectionsPerRoute = maximumNumberOfConnectionsPerRoute;
    }

    @Nonnull
    public SchemeRegistry getSchemeRegistry() {
        return _schemeRegistry;
    }

    public void setSchemeRegistry(@Nonnull SchemeRegistry schemeRegistry) {
        _schemeRegistry = schemeRegistry;
    }
}

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

package org.echocat.jomon.net.dns;

import org.echocat.jomon.net.FreeTcpPortDetector;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class TemporaryDnsServer extends DnsServer {

    private final InetSocketAddress _address;

    public TemporaryDnsServer() {
        super("");
        try {
            _address = getFreeLocalHostSocketAddress();
            addTCP(_address);
            addUDP(_address);
        } catch (Exception e) {
            throw new RuntimeException("Could not create server.", e);
        }
    }

    @Nonnull
    private static InetSocketAddress getFreeLocalHostSocketAddress() throws IOException {
        final InetAddress localHostAddress = getLocalHostAddress();
        final int port = getFreePort(localHostAddress);
        return new InetSocketAddress(localHostAddress, port);
    }

    @Nonnull
    private static InetAddress getLocalHostAddress() throws UnknownHostException {
        return InetAddress.getLocalHost();
    }

    private static int getFreePort(@Nonnull InetAddress localHostAddress) throws IOException {
        final FreeTcpPortDetector detector = new FreeTcpPortDetector(localHostAddress, 10000, 45000);
        return detector.detect();
    }

    @Nonnull
    public InetSocketAddress getAddress() {
        return _address;
    }
}

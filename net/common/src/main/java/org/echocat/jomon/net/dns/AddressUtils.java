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

package org.echocat.jomon.net.dns;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static java.net.InetAddress.getByAddress;
import static org.xbill.DNS.Address.*;

public class AddressUtils {

    @Nonnull
    public static InetAddress toInetAddress(@Nullable String hostName, @Nonnull String ipAsString) {
        return toInetAddress(hostName, toAddress(ipAsString));
    }

    @Nonnull
    public static InetAddress toInetAddress(@Nullable String hostName, @Nonnull byte[] address) {
        try {
            return getByAddress(hostName, address);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Nonnull
    public static byte[] toAddress(@Nonnull String ipAsString) {
        byte[] address = toByteArray(ipAsString, IPv4);
        if (address == null) {
            address = toByteArray(ipAsString, IPv6);
        }
        if (address == null) {
            throw new IllegalArgumentException(ipAsString + " is no valid ip in string format.");
        }
        return address;
    }

    private AddressUtils() {}
}

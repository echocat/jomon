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

package org.echocat.jomon.net;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import static org.echocat.jomon.net.NetworkInterfaceQuery.networkInterface;
import static org.echocat.jomon.net.NetworkInterfaceRepository.networkInterfaceRepository;
import static org.echocat.jomon.net.NetworkInterfaceType.loopBack;

public class NetworkInterfaceUtils {

    @Nonnull
    private static final NetworkInterfaceQuery LOOP_BACK_QUERY = networkInterface().whichIsOfType(loopBack);

    @Nonnull
    public static NetworkInterface getLoopBackInterface() {
        final NetworkInterface networkInterface = networkInterfaceRepository().findOneBy(LOOP_BACK_QUERY);
        if (networkInterface == null) {
            throw new IllegalStateException("Could not find a loopBack device.");
        }
        return networkInterface;
    }

    @Nullable
    public static InetAddress findFirstAddressOf(@Nonnull NetworkInterface networkInterface) {
        final Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
        return addresses.hasMoreElements() ? addresses.nextElement() : null;
    }

    @Nonnull
    public static InetAddress getFirstAddressOf(@Nonnull NetworkInterface networkInterface) {
        final InetAddress address = findFirstAddressOf(networkInterface);
        if (address == null) {
            throw new IllegalArgumentException("Interface " + networkInterface.getName() + " has no address.");
        }
        return address;
    }

    public static boolean containsAddress(@Nonnull NetworkInterface networkInterface, @Nonnull InetAddress address) {
        boolean result = false;
        final Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
        while (addresses.hasMoreElements()) {
            final InetAddress current = addresses.nextElement();
            if (current.equals(address)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public static void assertThatContainsAddress(@Nonnull NetworkInterface networkInterface, @Nonnull InetAddress address) throws IllegalArgumentException {
        if (!containsAddress(networkInterface, address)) {
            throw new IllegalArgumentException("Interface " + networkInterface.getName() + " is not bound to address " + address + ".");
        }
    }

    private NetworkInterfaceUtils() {}

}

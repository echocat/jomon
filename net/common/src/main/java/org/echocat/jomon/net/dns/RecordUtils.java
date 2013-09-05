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

import org.xbill.DNS.*;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import static org.xbill.DNS.Address.IPv4;
import static org.xbill.DNS.Address.toByteArray;
import static org.xbill.DNS.Name.fromConstantString;

public class RecordUtils {

    public static final int DEFAULT_TTL = 3600;

    @Nonnull
    public static SRVRecord srv(@Nonnull String name, @Nonnegative int priority, @Nonnegative int weight, @Nonnegative int port, @Nonnull String target) {
        return srv(name, DEFAULT_TTL, priority, weight, port, target);
    }

    @Nonnull
    public static SRVRecord srv(@Nonnull String name, @Nonnegative long ttl, @Nonnegative int priority, @Nonnegative int weight, @Nonnegative int port, @Nonnull String target) {
        return srv(fromConstantString(name), ttl, priority, weight, port, fromConstantString(target));
    }

    @Nonnull
    public static SRVRecord srv(@Nonnull Name name, @Nonnegative long ttl, @Nonnegative int priority, @Nonnegative int weight, @Nonnegative int port, @Nonnull Name target) {
        return new SRVRecord(name, DClass.IN, ttl, priority, weight, port, target);
    }

    @Nonnull
    public static ARecord a(@Nonnull String name, @Nonnull String ipv4) {
        return a(name, DEFAULT_TTL, ipv4);
    }

    @Nonnull
    public static ARecord a(@Nonnull String name, @Nonnegative long ttl, @Nonnull String ipv4) {
        return a(name, ttl, toByteArray(ipv4, IPv4));
    }

    @Nonnull
    public static ARecord a(@Nonnull String name, @Nonnegative long ttl, @Nonnull byte[] address) {
        try {
            return a(fromConstantString(name), ttl, InetAddress.getByAddress(address));
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Given address is invalid: " + Arrays.toString(address), e);
        }
    }

    @Nonnull
    public static ARecord a(@Nonnull Name name, @Nonnegative long ttl, @Nonnull InetAddress address) {
        return new ARecord(name, DClass.IN, ttl, address);
    }

    @Nonnull
    public static SOARecord soa(@Nonnull String name, @Nonnull String host, @Nonnegative long serial) {
        return soa(name, DEFAULT_TTL, host, serial);
    }

    @Nonnull
    public static SOARecord soa(@Nonnull String name, @Nonnegative long ttl, @Nonnull String host, @Nonnegative long serial) {
        return soa(name, ttl, host, serial, ttl, ttl * 2, ttl * 500, ttl);
    }

    @Nonnull
    public static SOARecord soa(@Nonnull String name, @Nonnegative long ttl, @Nonnull String host, @Nonnegative long serial, @Nonnegative long refresh, @Nonnegative long retry, @Nonnegative long expire, @Nonnegative long minimum) {
        return soa(name, ttl, host, "hostmaster." + host, serial, refresh, retry, expire, minimum);
    }

    @Nonnull
    public static SOARecord soa(@Nonnull String name, @Nonnegative long ttl, @Nonnull String host, @Nonnull String admin, @Nonnegative long serial, @Nonnegative long refresh, @Nonnegative long retry, @Nonnegative long expire, @Nonnegative long minimum) {
        return soa(fromConstantString(name), ttl, fromConstantString(host), fromConstantString(admin), serial, refresh, retry, expire, minimum);
    }

    @Nonnull
    public static SOARecord soa(@Nonnull Name name, @Nonnegative long ttl, @Nonnull Name host, @Nonnull Name admin, @Nonnegative long serial, @Nonnegative long refresh, @Nonnegative long retry, @Nonnegative long expire, @Nonnegative long minimum) {
        return new SOARecord(name, DClass.IN, ttl, host, admin, serial, refresh, retry, expire, minimum);
    }

    @Nonnull
    public static NSRecord ns(@Nonnull String name, @Nonnegative long ttl, @Nonnull String target) {
        return ns(fromConstantString(name), ttl, fromConstantString(target));
    }

    @Nonnull
    public static NSRecord ns(@Nonnull String name, @Nonnull String target) {
        return ns(name, DEFAULT_TTL, target);
    }

    @Nonnull
    public static NSRecord ns(@Nonnull Name name, @Nonnegative long ttl, @Nonnull Name target) {
        return new NSRecord(name, DClass.IN, ttl, target);
    }


    private RecordUtils() {}
}

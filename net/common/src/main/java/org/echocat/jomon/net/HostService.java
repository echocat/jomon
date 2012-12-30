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

package org.echocat.jomon.net;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class HostService {

    private final InetSocketAddress _address;
    private final int _priority;
    private final int _weight;
    private final long _ttl;

    @Nonnull
    public static HostService hostService(@Nonnull InetAddress address, @Nonnegative int port, @Nonnegative int priority, @Nonnegative int weight, @Nonnegative long ttl) {
        return hostService(new InetSocketAddress(address, port), priority, weight, ttl);
    }

    @Nonnull
    public static HostService hostService(@Nonnull InetSocketAddress address, @Nonnegative int priority, @Nonnegative int weight, @Nonnegative long ttl) {
        return new HostService(address, priority, weight, ttl);
    }

    public HostService(@Nonnull InetSocketAddress address, @Nonnegative int priority, @Nonnegative int weight, @Nonnegative long ttl) {
        _priority = priority;
        _weight = weight;
        _address = address;
        _ttl = ttl;
    }

    @Nonnull
    public InetSocketAddress getAddress() {
        return _address;
    }

    @Nonnegative
    public int getPriority() {
        return _priority;
    }

    @Nonnegative
    public int getWeight() {
        return _weight;
    }

    @Nonnegative
    public long getTtl() {
        return _ttl;
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else if (!(o instanceof HostService)) {
            result = false;
        } else {
            final HostService that = (HostService) o;
            result = _address.equals(that._address);
        }
        return result;
    }

    @Override
    public int hashCode() {
        return _address.hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(_address).append("{ttl=").append(_ttl).append(", priority=").append(_priority).append(", weight=").append(_weight).append("}");
        return sb.toString();
    }
}

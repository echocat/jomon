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

import org.echocat.jomon.runtime.iterators.CloseableIterator;
import org.echocat.jomon.runtime.repository.QueryableRepository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.NetworkInterface;
import java.net.SocketException;

import static com.google.common.collect.Iterators.forEnumeration;
import static java.net.NetworkInterface.getNetworkInterfaces;
import static org.echocat.jomon.runtime.CollectionUtils.countElementsOf;
import static org.echocat.jomon.runtime.CollectionUtils.findFirstOf;
import static org.echocat.jomon.runtime.iterators.IteratorUtils.filter;

public class NetworkInterfaceRepository implements QueryableRepository<NetworkInterfaceQuery, String, NetworkInterface> {

    @Nonnull
    private static final NetworkInterfaceRepository INSTANCE = new NetworkInterfaceRepository();

    @Nonnull
    public static NetworkInterfaceRepository getInstance() {
        return INSTANCE;
    }

    @Nonnull
    public static NetworkInterfaceRepository networkInterfaceRepository() {
        return getInstance();
    }

    @Nullable
    @Override
    public NetworkInterface findOneBy(@Nonnull String name) {
        try {
            return NetworkInterface.getByName(name);
        } catch (SocketException e) {
            throw new RuntimeException("Could not query interface by name: " + name, e);
        }
    }

    @Nullable
    @Override
    public NetworkInterface findOneBy(@Nonnull NetworkInterfaceQuery query) {
        return findFirstOf(findBy(query));
    }

    @Nonnull
    @Override
    public CloseableIterator<NetworkInterface> findBy(@Nonnull NetworkInterfaceQuery query) {
        try {
            return filter(forEnumeration(getNetworkInterfaces()), query);
        } catch (SocketException e) {
            throw new RuntimeException("Could not query interface by query.", e);
        }
    }

    @Override
    public long countBy(@Nonnull NetworkInterfaceQuery query) {
        return countElementsOf(findBy(query));
    }

}

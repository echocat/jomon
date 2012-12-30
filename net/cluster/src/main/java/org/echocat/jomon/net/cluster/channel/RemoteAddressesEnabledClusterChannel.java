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

package org.echocat.jomon.net.cluster.channel;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.util.Collection;

public interface RemoteAddressesEnabledClusterChannel<ID, N extends Node<ID>> extends ClusterChannel<ID, N> {

    @Nullable
    public Collection<InetSocketAddress> getRemoteAddresses();

    public void setRemoteAddresses(@Nullable Collection<InetSocketAddress> remoteAddresses);

    @Nullable
    public String getRemoteAddressesAsString();

    public void setRemoteAddressesAsString(@Nullable String remoteAddressesFromString);
}

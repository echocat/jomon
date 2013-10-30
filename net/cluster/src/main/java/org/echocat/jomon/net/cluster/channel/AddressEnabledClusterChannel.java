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

package org.echocat.jomon.net.cluster.channel;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;

public interface AddressEnabledClusterChannel<ID, N extends Node<ID>> extends ClusterChannel<ID, N> {

    @Nullable
    public InetSocketAddress getAddress();

    public void setAddress(@Nullable InetSocketAddress address);

    public void setAddress(@Nullable InetSocketAddress address, @Nullable NetworkInterface networkInterface);

    @Nullable
    public NetworkInterface getInterface();

    public void setInterface(@Nullable NetworkInterface networkInterface);

}

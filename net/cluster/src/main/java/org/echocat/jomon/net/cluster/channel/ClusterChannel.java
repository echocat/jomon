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

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public interface ClusterChannel<ID, N extends Node<ID>> extends AutoCloseable {

    @Nonnull
    public ID getId();

    @Nonnull
    public UUID getUuid();

    @Nullable
    public String getName();

    public void setName(@Nullable String name);

    public void send(@Nonnull Message message) throws IllegalArgumentException;

    public void send(@Nonnull Message message, @Nonnegative long timeout, @Nonnull TimeUnit unit) throws IllegalArgumentException;

    public boolean isConnected();

    @Nonnull
    public Set<? extends N> getNodes();

    @Nonnull
    public N getLocalNode();

}

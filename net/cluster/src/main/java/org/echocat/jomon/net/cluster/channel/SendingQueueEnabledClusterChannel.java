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
import javax.annotation.Nullable;

public interface SendingQueueEnabledClusterChannel<ID, N extends Node<ID>> extends ClusterChannel<ID, N> {

    /**
     * @param capacity if <code>0</code> this clusterChannel will be send in blocking mode immediately.
     */
    public void setSendingQueueCapacity(@Nonnegative int capacity);

    /**
     * @return if <code>0</code> this clusterChannel will be send in blocking mode immediately.
     */
    @Nonnegative
    public int getSendingQueueCapacity();

    /**
     * @return if <code>null</code> the queue is disabled.
     */
    @Nullable
    public Integer getSendingQueueSize();

}

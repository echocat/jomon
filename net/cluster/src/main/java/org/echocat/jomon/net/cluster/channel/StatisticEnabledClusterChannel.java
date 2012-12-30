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
import java.util.Date;

public interface StatisticEnabledClusterChannel<ID, N extends Node<ID>> extends ClusterChannel<ID, N> {

    @Nullable
    public Date getLastMessageReceived();

    @Nonnegative
    @Nullable
    public Double getMessagesReceivedPerSecond();

    @Nonnegative
    @Nullable
    public Long getMessagesReceived();

    @Nullable
    public Date getLastMessageSend();

    @Nonnegative
    @Nullable
    public Double getMessagesSendPerSecond();

    @Nonnegative
    @Nullable
    public Long getMessagesSend();

}

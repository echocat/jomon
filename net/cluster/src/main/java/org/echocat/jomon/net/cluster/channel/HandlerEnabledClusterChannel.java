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

import javax.annotation.Nonnull;

public interface HandlerEnabledClusterChannel<ID, N extends Node<ID>> extends ClusterChannel<ID, N> {

    public void register(@Nonnull Handler handler);

    public void unregister(@Nonnull Handler handler);

    public interface Handler {}

    public interface MessageHandler extends Handler {
        public void handle(@Nonnull HandlerEnabledClusterChannel<?, ?> clusterChannel, @Nonnull ReceivedMessage<?> message);
    }

    public interface PresenceHandler extends Handler {
        public void nodeEnter(@Nonnull HandlerEnabledClusterChannel<?, ?> clusterChannel, @Nonnull Node<?> entry);
        public void nodeLeft(@Nonnull HandlerEnabledClusterChannel<?, ?> clusterChannel, @Nonnull Node<?> entry);
    }
}

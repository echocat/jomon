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

import org.echocat.jomon.runtime.math.OverPeriodCounter;
import org.echocat.jomon.runtime.util.Duration;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import java.util.Date;

import static java.lang.System.currentTimeMillis;

public interface StatisticEnabledNode<ID> extends Node<ID> {

    @Nullable
    public Boolean getIsInboundConnected();

    @Nonnegative
    @Nullable
    public Long getNumberOfInboundMessages();

    @Nonnegative
    @Nullable
    public Double getNumberOfInboundMessagesPerSecond();

    @Nullable
    public Date getLastInboundMessage();

    @Nullable
    public Boolean getIsOutboundConnected();

    @Nonnegative
    @Nullable
    public Long getNumberOfOutboundMessages();

    @Nonnegative
    @Nullable
    public Double getNumberOfOutboundMessagesPerSecond();

    @Nullable
    public Date getLastOutboundMessage();

    public abstract class Impl<ID> extends Node.Impl<ID> implements StatisticEnabledNode<ID> {

        private final OverPeriodCounter _numberOfInboundMessagesPerSecond = new OverPeriodCounter(new Duration("1m"), new Duration("1s"));
        private volatile long _numberOfInboundMessages;
        private volatile long _lastInboundMessageInMillis;

        private final OverPeriodCounter _numberOfOutboundMessagesPerSecond = new OverPeriodCounter(new Duration("1m"), new Duration("1s"));
        private volatile long _numberOfOutboundMessages;
        private volatile long _lastOutboundMessageInMillis;

        private volatile long _lastSeenInMillis;

        @Nonnegative
        public long getLastSeenInMillis() {
            return _lastSeenInMillis;
        }

        @Override
        @Nullable
        public Date getLastSeen() {
            final long lastSeenInMillis = getLastSeenInMillis();
            return lastSeenInMillis > 0 ? new Date(lastSeenInMillis) : null;
        }

        public void recordVitalSign() {
            _lastSeenInMillis = currentTimeMillis();
        }

        @Override
        @Nullable
        public Boolean getIsInboundConnected() {
            return null;
        }

        @Override
        @Nonnegative
        public Double getNumberOfInboundMessagesPerSecond() {
            return _numberOfInboundMessagesPerSecond.getAsDouble();
        }

        @Nonnegative
        public long getLastInboundMessageInMillis() {
            return _lastInboundMessageInMillis;
        }

        @Override
        @Nullable
        public Date getLastInboundMessage() {
            final long lastInboundMessageInMillis = getLastInboundMessageInMillis();
            return lastInboundMessageInMillis > 0 ? new Date(lastInboundMessageInMillis) : null;
        }

        @Override
        @Nonnegative
        @Nullable
        public Long getNumberOfInboundMessages() {
            return _numberOfInboundMessages;
        }

        public void recordInbound() {
            _numberOfInboundMessagesPerSecond.record();
            _numberOfInboundMessages++;
            final long millis = currentTimeMillis();
            _lastInboundMessageInMillis = millis;
            _lastSeenInMillis = millis;
        }

        @Override
        @Nullable
        public Boolean getIsOutboundConnected() {
            return null;
        }

        @Override
        @Nonnegative
        @Nullable
        public Double getNumberOfOutboundMessagesPerSecond() {
            return _numberOfOutboundMessagesPerSecond.getAsDouble();
        }

        @Nonnegative
        public long getLastOutboundMessageInMillis() {
            return _lastOutboundMessageInMillis;
        }

        @Override
        @Nullable
        public Date getLastOutboundMessage() {
            final long lastOutboundMessageInMillis = getLastOutboundMessageInMillis();
            return lastOutboundMessageInMillis > 0 ? new Date(lastOutboundMessageInMillis) : null;
        }

        @Override
        @Nonnegative
        @Nullable
        public Long getNumberOfOutboundMessages() {
            return _numberOfOutboundMessages;
        }

        public void recordOutbound() {
            _numberOfOutboundMessagesPerSecond.record();
            _numberOfOutboundMessages++;
            _lastOutboundMessageInMillis = currentTimeMillis();
        }

    }


}

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

package org.echocat.jomon.net.cluster.channel.tcp;

import org.echocat.jomon.net.cluster.channel.Node;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.UUID;

public class TcpNodeInfo extends Node.Impl<UUID> implements TcpNode {

    private final UUID _uuid;
    private final InetSocketAddress _address;

    private InboundTcpNode _inbound;
    private OutboundTcpNode _outbound;

    public TcpNodeInfo(@Nonnull UUID uuid, @Nonnull InetSocketAddress address) {
        _uuid = uuid;
        _address = address;
    }

    @Nonnull
    @Override
    public UUID getId() {
        return getUuid();
    }

    @Nonnull
    @Override
    public UUID getUuid() {
        return _uuid;
    }

    @Nonnull
    @Override
    public InetSocketAddress getAddress() {
        return _address;
    }

    public InboundTcpNode getInbound() {
        return _inbound;
    }

    public void setInbound(InboundTcpNode inbound) {
        _inbound = inbound;
    }

    public OutboundTcpNode getOutbound() {
        return _outbound;
    }

    public void setOutbound(OutboundTcpNode outbound) {
        _outbound = outbound;
    }

    @Override
    @Nullable
    public Date getLastSeen() {
        final InboundTcpNode inbound = _inbound;
        final OutboundTcpNode outbound = _outbound;
        final Date inboundLastSeen = inbound != null ? inbound.getLastSeen() : null;
        final Date outboundLastSeen = outbound != null ? outbound.getLastSeen() : null;
        final Date result;
        if (inboundLastSeen == null && outboundLastSeen == null) {
            result = null;
        } else if (inboundLastSeen != null && outboundLastSeen == null) {
            result = inboundLastSeen;
        } else if (outboundLastSeen != null && inboundLastSeen == null) {
            result = outboundLastSeen;
        } else {
            result = inboundLastSeen.after(outboundLastSeen) ? inboundLastSeen : outboundLastSeen;
        }
        return result;
    }

    @Override
    public Boolean getIsInboundConnected() {
        final InboundTcpNode inbound = _inbound;
        return inbound != null && inbound.isConnected();
    }

    @Override
    public Long getNumberOfInboundMessages() {
        final InboundTcpNode inbound = _inbound;
        return inbound != null ? inbound.getNumberOfInboundMessages() : null;
    }

    @Override
    public Double getNumberOfInboundMessagesPerSecond() {
        final InboundTcpNode inbound = _inbound;
        return inbound != null ? inbound.getNumberOfInboundMessagesPerSecond() : null;
    }

    @Override
    public Date getLastInboundMessage() {
        final InboundTcpNode inbound = _inbound;
        return inbound != null ? inbound.getLastInboundMessage() : null;
    }
    
    @Override
    public Boolean getIsOutboundConnected() {
        final OutboundTcpNode outbound = _outbound;
        return outbound != null && outbound.isConnected();
    }

    @Override
    public Long getNumberOfOutboundMessages() {
        final OutboundTcpNode outbound = _outbound;
        return outbound != null ? outbound.getNumberOfOutboundMessages() : null;
    }

    @Override
    public Double getNumberOfOutboundMessagesPerSecond() {
        final OutboundTcpNode outbound = _outbound;
        return outbound != null ? outbound.getNumberOfOutboundMessagesPerSecond() : null;
    }

    @Override
    public Date getLastOutboundMessage() {
        final OutboundTcpNode outbound = _outbound;
        return outbound != null ? outbound.getLastOutboundMessage() : null;
    }
}

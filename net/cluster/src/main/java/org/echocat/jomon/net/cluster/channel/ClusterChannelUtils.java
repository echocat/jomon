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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.util.Locale.US;

public class ClusterChannelUtils {

    private static final DecimalFormatSymbols SYMBOLS = new DecimalFormatSymbols(US);

    @Nonnull
    public static String formatNodesStatusOf(@Nonnull ClusterChannel<?, ?> clusterChannel) {
        return formatNodesStatusOf(clusterChannel.getNodes());
    }

    @Nonnull
    public static String formatNodesStatusOf(@Nonnull Iterable<? extends Node<?>> nodes) {
        final StringBuilder sb = new StringBuilder();
        for (Node<?> node : nodes) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(node);

            if (node instanceof StatisticEnabledNode) {
                final StatisticEnabledNode<?> statistic = (StatisticEnabledNode) node;

                final Date lastSeen = statistic.getLastSeen();
                sb.append("{Last seen: ").append(lastSeen != null ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(lastSeen) : "never");
                formatInbound(sb, statistic);
                formatOutbound(sb, statistic);
                sb.append('}');
            }

        }
        return sb.toString();
    }

    protected static void formatInbound(@Nonnull StringBuilder to, @Nonnull StatisticEnabledNode<?> of) {
        formatDetails("Inbound", to, of.getLastInboundMessage(), of.getNumberOfInboundMessages(), of.getNumberOfInboundMessagesPerSecond());
    }

    protected static void formatOutbound(@Nonnull StringBuilder to, @Nonnull StatisticEnabledNode<?> of) {
        formatDetails("Outbound", to, of.getLastOutboundMessage(), of.getNumberOfOutboundMessages(), of.getNumberOfOutboundMessagesPerSecond());
    }

    protected static void formatDetails(@Nonnull String prefix, @Nonnull StringBuilder to, @Nullable Date lastMessage, @Nullable Long numberOfMessages, @Nullable Double numberOfMessagesPerSecond) {
        if (lastMessage != null || numberOfMessages != null || numberOfMessagesPerSecond != null) {
            to.append(" / ").append(prefix).append(": ");
            if (numberOfMessages != null) {
                to.append(new DecimalFormat("#,##0", SYMBOLS).format(numberOfMessages)).append(" total");
            }
            if (numberOfMessagesPerSecond != null) {
                if (numberOfMessages != null) {
                    to.append(", ");
                }
                to.append(new DecimalFormat("#,##0.00", SYMBOLS).format(numberOfMessagesPerSecond)).append(" m/s");
            }
            if (lastMessage != null) {
                if (numberOfMessages != null || numberOfMessagesPerSecond != null) {
                    to.append(", ");
                }
            }
            to.append("last at ").append(lastMessage != null ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(lastMessage) : "never");
        }
    }

    private ClusterChannelUtils() {}
}

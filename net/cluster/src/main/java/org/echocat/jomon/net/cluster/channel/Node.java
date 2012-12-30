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
import java.net.InetSocketAddress;
import java.util.Comparator;
import java.util.Date;
import java.util.UUID;

public interface Node<ID> {

    public static final Comparator<Node<?>> ADDRESS_BASED_COMPARATOR = new Comparator<Node<?>>() { @Override public int compare(Node<?> o1, Node<?> o2) {
        final int result;
        if (o1 == null && o2 == null) {
            result = 0;
        } else if (o1 != null) {
            result = 1;
        } else if (o2 != null) {
            result = -1;
        } else {
            final InetSocketAddress a1 = o1.getAddress();
            final InetSocketAddress a2 = o2.getAddress();
            if (a1 == null && a2 == null) {
                result = 0;
            } else if (a1 != null) {
                result = 1;
            } else if (a2 != null) {
                result = -1;
            } else {
                result = a1.toString().compareTo(a2.toString());
            }
        }
        return result;
    }};

    @Nonnull
    public ID getId();

    @Nonnull
    public UUID getUuid();

    @Nonnull
    public InetSocketAddress getAddress();

    @Nullable
    public Date getLastSeen();

    public abstract class Impl<ID> implements Node<ID> {

        @Override
        public boolean equals(Object o) {
            final boolean result;
            if (this == o) {
                result = true;
            } else if (!(o instanceof Impl)) {
                result = false;
            } else {
                final Node<?> that = (Node) o;
                result = getUuid().equals(that.getUuid());
            }
            return result;
        }

        @Override
        public int hashCode() {
            return getUuid().hashCode();
        }

        @Override
        public String toString() {
            return getId() + ":" + getAddress();
        }
    }
}

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

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.nio.charset.Charset;

public class ReceivedMessage<N extends Node<?>> extends Message {

    private final N _from;

    public ReceivedMessage(byte command, @Nonnull String data, @Nonnull Charset charset, @Nonnull N from) {
        super(command, data, charset);
        _from = from;
    }

    public ReceivedMessage(byte command, @Nonnull byte[] data, @Nonnull N from) {
        super(command, data);
        _from = from;
    }

    public ReceivedMessage(byte command, @Nonnull byte[] data, @Nonnegative int length, @Nonnull N from) {
        super(command, data, length);
        _from = from;
    }

    public ReceivedMessage(byte command, @Nonnull byte[] data, @Nonnegative int offset, @Nonnegative int length, @Nonnull N from) {
        super(command, data, offset, length);
        _from = from;
    }

    @Nonnull
    public N getFrom() {
        return _from;
    }
}

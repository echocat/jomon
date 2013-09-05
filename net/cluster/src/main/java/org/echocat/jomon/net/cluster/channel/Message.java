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
import java.util.Arrays;

import static java.util.Arrays.copyOfRange;
import static org.apache.commons.codec.binary.Hex.encodeHex;

public class Message {

    private final byte _command;
    private final byte[] _data;
    private final int _offset;
    private final int _length;

    public Message(byte command, @Nonnull String data, @Nonnull Charset charset) {
        this(command, data.getBytes(charset));
    }

    public Message(byte command, @Nonnull byte[] data) {
        this(command, data, data.length);
    }

    public Message(byte command, @Nonnull byte[] data, @Nonnegative int length) {
        this(command, data, 0, length);
    }

    public Message(byte command, @Nonnull byte[] data, @Nonnegative int offset, @Nonnegative int length) {
        if (offset < 0 || length < offset) {
            throw new IllegalArgumentException();
        }
        if (offset + length > data.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        _command = command;
        _data = data;
        _offset = offset;
        _length = length;
    }

    public byte getCommand() {
        return _command;
    }

    @Nonnull
    public byte[] getData() {
        return _data;
    }

    @Nonnegative
    public int getOffset() {
        return _offset;
    }

    @Nonnegative
    public int getLength() {
        return _length;
    }

    @Nonnull
    public String getDataAsString(@Nonnull Charset charset) {
        return new String(_data, _offset, _length, charset);
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else  if (!(o instanceof Message)) {
            result = false;
        } else {
            final Message that = (Message) o;
            result = getCommand() == that.getCommand()
                && Arrays.equals(getData(), that.getData())
                && getOffset() == that.getOffset()
                && getLength() == that.getLength();
        }
        return result;
    }

    @Override
    public int hashCode() {
        int result = (int) getCommand();
        result = 31 * result + Arrays.hashCode(getData());
        result = 31 * result + getOffset();
        result = 31 * result + getLength();
        return result;
    }

    @Override
    public String toString() {
        final int length = getLength();
        final int offset = getOffset();
        final char[] trimmedData = encodeHex(copyOfRange(getData(), offset, offset + (length > 80 ? 80 : length)), false);
        // noinspection StringBufferReplaceableByString
        return new StringBuilder().append((int)getCommand() + ((int)Byte.MAX_VALUE * -1)).append(":").append(trimmedData).append(length > 80 ? "..." : "").toString();
    }


}

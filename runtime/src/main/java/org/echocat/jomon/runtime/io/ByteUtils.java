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

package org.echocat.jomon.runtime.io;

import org.echocat.jomon.runtime.util.ByteCount;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.charset.Charset;

import static java.lang.System.arraycopy;
import static java.nio.charset.Charset.forName;
import static org.echocat.jomon.runtime.util.ByteCount.byteCountOf;

public class ByteUtils {

    public static final Charset DEFAULT_CHARSET = forName("UTF-8");

    public static final int BOOLEAN_CHUNK_SIZE = 1;
    public static final int BYTE_CHUNK_SIZE = 1;
    public static final int SHORT_CHUNK_SIZE = 2;
    public static final int INTEGER_CHUNK_SIZE = 4;
    public static final int LONG_CHUNK_SIZE = 8;
    public static final int FLOAT_CHUNK_SIZE = 4;
    public static final int DOUBLE_CHUNK_SIZE = 8;

    public static final int DEFAULT_STRING_BUFFER_SIZE = 255;
    public static final int DEFAULT_STRING_CHUNK_SIZE = getStringChunkSizeFor(DEFAULT_STRING_BUFFER_SIZE);

    public static final ByteCount DEFAULT_BUFFER_SIZE = byteCountOf("1k");

    private ByteUtils() {}

    /**
     * @return the number of putted bytes.
     */
    @Nonnegative
    public static int putByte(@Nonnull byte[] b, @Nonnegative int offset, byte val) {
        checkBufferEnd(b, offset, BYTE_CHUNK_SIZE);
        b[offset] = val;
        return BYTE_CHUNK_SIZE;
    }

    public static byte getByte(@Nonnull byte[] b, @Nonnegative int offset) {
        checkBufferEnd(b, offset, BYTE_CHUNK_SIZE);
        return b[offset];
    }

    /**
     * @return the number of putted bytes.
     */
    @Nonnegative
    public static int putShort(@Nonnull byte[] b, @Nonnegative int offset, short val) {
        checkBufferEnd(b, offset, SHORT_CHUNK_SIZE);
        b[offset + 1] = (byte) (val      );
        b[offset    ] = (byte) (val >>> 8);
        return SHORT_CHUNK_SIZE;
    }

    public static short getShort(@Nonnull byte[] b, @Nonnegative int offset) {
        checkBufferEnd(b, offset, SHORT_CHUNK_SIZE);
        return (short) ((b[offset + 1] & 0xFF) +
                        (b[offset] << 8));
    }

    /**
     * @return the number of putted bytes.
     */
    @Nonnegative
    public static int putInteger(@Nonnull byte[] b, @Nonnegative int offset, int value) {
        checkBufferEnd(b, offset, INTEGER_CHUNK_SIZE);
        b[offset + 3] = (byte) (value);
        b[offset + 2] = (byte) (value >>>  8);
        b[offset + 1] = (byte) (value >>> 16);
        b[offset    ] = (byte) (value >>> 24);
        return INTEGER_CHUNK_SIZE;
    }

    public static int getInteger(@Nonnull byte[] b, @Nonnegative int offset) {
        checkBufferEnd(b, offset, INTEGER_CHUNK_SIZE);
        return ((b[offset + 3] & 0xFF)      ) +
               ((b[offset + 2] & 0xFF) <<  8) +
               ((b[offset + 1] & 0xFF) << 16) +
               ((b[offset    ]       ) << 24);
    }

    /**
     * @return the number of putted bytes.
     */
    @Nonnegative
    public static int putLong(@Nonnull byte[] b, @Nonnegative int offset, long value) {
        checkBufferEnd(b, offset, LONG_CHUNK_SIZE);
        b[offset + 7] = (byte) (value);
        b[offset + 6] = (byte) (value >>>  8);
        b[offset + 5] = (byte) (value >>> 16);
        b[offset + 4] = (byte) (value >>> 24);
        b[offset + 3] = (byte) (value >>> 32);
        b[offset + 2] = (byte) (value >>> 40);
        b[offset + 1] = (byte) (value >>> 48);
        b[offset    ] = (byte) (value >>> 56);
        return LONG_CHUNK_SIZE;
    }

    public static long getLong(@Nonnull byte[] b, @Nonnegative int offset) {
        checkBufferEnd(b, offset, LONG_CHUNK_SIZE);
        return ((b[offset + 7] & 0xFFL)      ) +
            ((b[offset + 6] & 0xFFL) <<  8) +
            ((b[offset + 5] & 0xFFL) << 16) +
            ((b[offset + 4] & 0xFFL) << 24) +
            ((b[offset + 3] & 0xFFL) << 32) +
            ((b[offset + 2] & 0xFFL) << 40) +
            ((b[offset + 1] & 0xFFL) << 48) +
            (((long) b[offset])      << 56);
    }

    @Nonnegative
    public static int getStringChunkSizeFor(@Nonnegative int bufferSize) {
        return INTEGER_CHUNK_SIZE + bufferSize;
    }

    @Nonnegative
    public static byte[] allocateStringChunkFor(@Nonnegative int bufferSize) {
        return new byte[getStringChunkSizeFor(bufferSize)];
    }

    /**
     * @return the number of putted bytes.
     */
    @Nonnegative
    public static int putString(@Nonnull byte[] b, @Nonnegative int bufferSize, @Nonnegative int offset, @Nullable String value, @Nonnull Charset charset) {
        final int chunkSize = getStringChunkSizeFor(bufferSize);
        checkBufferEnd(b, offset, chunkSize);
        if (value != null) {
            final byte[] bytes = value.getBytes(charset);
            if (bytes.length > bufferSize) {
                throw new IndexOutOfBoundsException("The bytes of the specified string (" + bytes.length + ") exceeded the maximum number of bytes of " + bufferSize + ".");
            }
            putInteger(b, offset, bytes.length);
            final int offsetForBytes = offset + INTEGER_CHUNK_SIZE;
            arraycopy(bytes, 0, b, offsetForBytes, bytes.length);
            final int offsetForZeros = offsetForBytes + bytes.length;
            putZeros(b, offsetForZeros, chunkSize - offsetForZeros);
        } else {
            putInteger(b, offset, -1);
            putZeros(b, offset + INTEGER_CHUNK_SIZE, chunkSize - INTEGER_CHUNK_SIZE);
        }
        return chunkSize;
    }

    @Nullable
    public static String getString(@Nonnull byte[] b, @Nonnegative int bufferSize, @Nonnegative int offset, @Nonnull Charset charset) {
        final int chunkSize = getStringChunkSizeFor(bufferSize);
        checkBufferEnd(b, offset, chunkSize);
        final int byteCount = getInteger(b, offset);
        final String result;
        if (byteCount >= 0) {
            result = new String(b, offset + INTEGER_CHUNK_SIZE, byteCount, charset);
        } else {
            result = null;
        }
        return result;
    }

    /**
     * @return the number of putted bytes.
     */
    @Nonnegative
    public static int putZeros(@Nonnull byte[] b, @Nonnegative int offset, @Nonnegative int length) {
        checkBufferEnd(b, offset, length);
        for (int i = offset; i < (offset + length); i++) {
            b[i] = 0;
        }
        return length;
    }

    public static void checkBufferEnd(@Nonnull byte[] b, @Nonnegative int offset, @Nonnegative int length) {
        checkOffset(offset);
        final int end = offset + length;
        if (b.length < end) {
            throw new IndexOutOfBoundsException("Byte buffer (" + b.length + ") is smaller then expected read end at " + end + ".");
        }
    }

    public static void checkOffset(int offset) {
        if (offset < 0) {
            throw new IndexOutOfBoundsException("Negative offset: " + offset);
        }
    }

}

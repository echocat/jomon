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

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.Charset;

import static java.lang.Byte.MIN_VALUE;
import static org.echocat.jomon.runtime.io.ByteUtils.DEFAULT_BUFFER_SIZE;
import static org.echocat.jomon.runtime.io.Serializers.stringSerializer;
import static org.echocat.jomon.runtime.util.ByteCount.byteCountOf;

public class StreamUtils {

    private StreamUtils() {}

    /**
     * @return number of written bytes.
     */
    @Nonnegative
    public static <T> int writeObject(@Nonnull Class<T> type, @Nullable T value, @Nonnull DataOutput to) throws IOException {
        final ChunkAwareSerializer<T> serializer = Serializers.getChunkAwareSerializerOf(type);
        serializer.write(value, to);
        return serializer.getChunkSize();
    }

    /**
     * @return number of written bytes.
     */
    @Nonnegative
    public static <T> int writeObject(@Nonnull Class<T> type, @Nullable T value, @Nonnull OutputStream to) throws IOException {
        return writeObject(type, value, toDataOutput(to));
    }

    public static void writeBoolean(boolean value, @Nonnull DataOutput to) throws IOException {
        to.writeBoolean(value);
    }

    public static void writeBoolean(boolean value, @Nonnull OutputStream to) throws IOException {
        writeBoolean(value, toDataOutput(to));
    }

    public static void writeByte(byte value, @Nonnull DataOutput to) throws IOException {
        to.writeByte(value);
    }

    public static void writeByte(byte value, @Nonnull OutputStream to) throws IOException {
        writeByte(value, toDataOutput(to));
    }

    public static void writeShort(short value, @Nonnull DataOutput to) throws IOException {
        to.writeShort(value);
    }

    public static void writeShort(short value, @Nonnull OutputStream to) throws IOException {
        writeShort(value, toDataOutput(to));
    }

    public static void writeInteger(int value, @Nonnull DataOutput to) throws IOException {
        to.writeInt(value);
    }

    public static void writeInteger(int value, @Nonnull OutputStream to) throws IOException {
        writeInteger(value, toDataOutput(to));
    }

    public static void writeLong(long value, @Nonnull DataOutput to) throws IOException {
        to.writeLong(value);
    }

    public static void writeLong(long value, @Nonnull OutputStream to) throws IOException {
        writeLong(value, toDataOutput(to));
    }

    public static void writeFloat(float value, @Nonnull DataOutput to) throws IOException {
        to.writeFloat(value);
    }

    public static void writeFloat(float value, @Nonnull OutputStream to) throws IOException {
        writeFloat(value, toDataOutput(to));
    }

    public static void writeDouble(double value, @Nonnull DataOutput to) throws IOException {
        to.writeDouble(value);
    }

    public static void writeDouble(double value, @Nonnull OutputStream to) throws IOException {
        writeDouble(value, toDataOutput(to));
    }

    /**
     * @return number of written bytes.
     */
    @Nonnegative
    public static int writeString(@Nonnegative int bufferSize, @Nullable String value, @Nonnull Charset charset, @Nonnull DataOutput to) throws IOException {
        final ChunkAwareSerializer<String> serializer = stringSerializer(bufferSize, charset);
        serializer.write(value, to);
        return serializer.getChunkSize();
    }

    /**
     * @return number of written bytes.
     */
    @Nonnegative
    public static int writeString(@Nonnegative int bufferSize, @Nullable String value, @Nonnull Charset charset, @Nonnull OutputStream to) throws IOException {
        return writeString(bufferSize, value, charset, toDataOutput(to));
    }

    /**
     * @return number of written bytes.
     */
    @Nonnegative
    public static int writeZeros(@Nonnegative int numberOfZeros, @Nonnull DataOutput to) throws IOException {
        final byte[] buffer = DEFAULT_BUFFER_SIZE.allocate();
        int totalWritten = 0;
        while (totalWritten < numberOfZeros) {
            final int numberOfBytesWriteNow = ((totalWritten + buffer.length) <= numberOfZeros) ? buffer.length : (numberOfZeros - totalWritten);
            to.write(buffer, 0, numberOfBytesWriteNow);
            totalWritten += numberOfBytesWriteNow;
        }
        if (totalWritten != numberOfZeros) {
            throw new IllegalStateException("Write to much from stream? Write " + byteCountOf(totalWritten) + " but expected was " + byteCountOf(numberOfZeros) + ".");
        }
        return numberOfZeros;
    }

    /**
     * @return number of written bytes.
     */
    @Nonnegative
    public static int writeZeros(@Nonnegative int numberOfZeros, @Nonnull OutputStream to) throws IOException {
        return writeZeros(numberOfZeros, toDataOutput(to));
    }

    public static boolean readBoolean(@Nonnull DataInput from) throws IOException {
        return from.readBoolean();
    }

    public static boolean readBoolean(@Nonnull InputStream from) throws IOException {
        return readBoolean(toDataInput(from));
    }

    public static byte readByte(@Nonnull DataInput from) throws IOException {
        return from.readByte();
    }

    public static byte readByte(@Nonnull InputStream from) throws IOException {
        return readByte(toDataInput(from));
    }

    public static short readShort(@Nonnull DataInput from) throws IOException {
        return from.readShort();
    }

    public static short readShort(@Nonnull InputStream from) throws IOException {
        return readShort(toDataInput(from));
    }

    public static int readInteger(@Nonnull DataInput from) throws IOException {
        return from.readInt();
    }

    public static int readInteger(@Nonnull InputStream from) throws IOException {
        return readInteger(toDataInput(from));
    }

    public static long readLong(@Nonnull DataInput from) throws IOException {
        return from.readLong();
    }

    public static long readLong(@Nonnull InputStream from) throws IOException {
        return readLong(toDataInput(from));
    }

    public static float readFloat(@Nonnull DataInput from) throws IOException {
        return from.readFloat();
    }

    public static float readFloat(@Nonnull InputStream from) throws IOException {
        return readFloat(toDataInput(from));
    }

    public static double readDouble(@Nonnull DataInput from) throws IOException {
        return from.readDouble();
    }

    public static double readDouble(@Nonnull InputStream from) throws IOException {
        return readDouble(toDataInput(from));
    }

    @Nullable
    public static String readString(@Nonnull DataInput from, @Nonnegative int bufferSize, @Nonnull Charset charset) throws IOException {
        return stringSerializer(bufferSize, charset).read(from);
    }

    public static void readZeros(@Nonnull DataInput from, @Nonnegative int numberOfZeros) throws IOException {
        final byte[] buffer = DEFAULT_BUFFER_SIZE.allocate();
        int totalRead = 0;
        while (totalRead < numberOfZeros) {
            final int read = ((totalRead + buffer.length) <= numberOfZeros) ? buffer.length : (numberOfZeros - totalRead);
            from.readFully(buffer, 0, read);
            for (int i = 0; i < read; i++) {
                if (buffer[i] != MIN_VALUE) {
                    throw new IOException("Found an non zero byte at a position where one is expected.");
                }
            }
            totalRead += read;
        }
        if (totalRead != numberOfZeros) {
            throw new IllegalStateException("Read to much from stream? Read " + byteCountOf(totalRead) + " but expected was " + byteCountOf(numberOfZeros) + ".");
        }
    }

    public static void readZeros(@Nonnull InputStream from, @Nonnegative int numberOfZeros) throws IOException {
        readZeros(toDataInput(from), numberOfZeros);
    }

    @Nullable
    public static <T> T readObject(@Nonnull Class<T> type, @Nonnull DataInput from) throws IOException {
        final ChunkAwareSerializer<T> serializer = Serializers.getChunkAwareSerializerOf(type);
        return serializer.read(from);
    }

    @Nullable
    public static <T> T readObject(@Nonnull Class<T> type, @Nonnull InputStream from) throws IOException {
        return readObject(type, toDataInput(from));
    }

    @Nonnull
    public static byte[] read(@Nonnull DataInput from, @Nonnegative int numberOfBytes) throws IOException {
        final byte[] buffer = new byte[numberOfBytes];
        from.readFully(buffer, 0, numberOfBytes);
        return buffer;
    }

    public static void readFixedByteCount(@Nonnull InputStream from, @Nonnull byte[] to, @Nonnegative int offsetInTo, @Nonnegative int length) throws IOException {
        int totalRead = 0;
        while (totalRead < length) {
            final int read = from.read(to, offsetInTo + totalRead, length - totalRead);
            if (read <= -1) {
                throw new EOFException("Stream ended unexpected. Read " + byteCountOf(totalRead + read) + " but expected was " + byteCountOf(length) + ".");
            }
            totalRead += read;
        }
    }

    @Nonnull
    public static DataOutput toDataOutput(@Nonnull OutputStream outputStream) {
        return new DataOutputStream(outputStream);
    }

    @Nonnull
    public static DataInput toDataInput(@Nonnull InputStream inputStream) {
        return new DataInputStream(inputStream);
    }

}

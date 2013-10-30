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
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.WeakHashMap;

import static java.nio.charset.Charset.defaultCharset;
import static org.echocat.jomon.runtime.CollectionUtils.asMap;
import static org.echocat.jomon.runtime.io.ByteUtils.*;

public class Serializers {

    @Nonnull
    private static final ChunkAwareSerializer<Boolean> BOOLEAN_SERIALIZER = new BooleanSerializer();
    @Nonnull
    private static final ChunkAwareSerializer<Byte> BYTE_SERIALIZER = new ByteSerializer();
    @Nonnull
    private static final ChunkAwareSerializer<Short> SHORT_SERIALIZER = new ShortSerializer();
    @Nonnull
    private static final ChunkAwareSerializer<Integer> INTEGER_SERIALIZER = new IntegerSerializer();
    @Nonnull
    private static final ChunkAwareSerializer<Long> LONG_SERIALIZER = new LongSerializer();
    @Nonnull
    private static final ChunkAwareSerializer<Float> FLOAT_SERIALIZER = new FloatSerializer();
    @Nonnull
    private static final ChunkAwareSerializer<Double> DOUBLE_SERIALIZER = new DoubleSerializer();
    @Nonnull
    private static final Serializer<String> STRING_SERIALIZER = new StringSerializer();
    @Nonnull
    private static final Map<Class<?>, Serializer<?>> TYPE_TO_SERIALIZER_CACHE = new WeakHashMap<>();

    @Nonnull
    private static final Map<Class<?>, ChunkAwareSerializer<?>> TYPE_TO_CHUNK_AWARE_SERIALIZER = asMap(
        Boolean.class, BOOLEAN_SERIALIZER,
        boolean.class, BOOLEAN_SERIALIZER,
        Byte.class, BYTE_SERIALIZER,
        byte.class, BYTE_SERIALIZER,
        Short.class, SHORT_SERIALIZER,
        short.class, SHORT_SERIALIZER,
        Integer.class, INTEGER_SERIALIZER,
        int.class, INTEGER_SERIALIZER,
        Long.class, LONG_SERIALIZER,
        long.class, LONG_SERIALIZER,
        Float.class, FLOAT_SERIALIZER,
        float.class, FLOAT_SERIALIZER,
        Double.class, DOUBLE_SERIALIZER,
        double.class, DOUBLE_SERIALIZER
    );

    @Nonnull
    private static final Map<Class<?>, Serializer<?>> TYPE_TO_SERIALIZER = asMap(
        Boolean.class, BOOLEAN_SERIALIZER,
        boolean.class, BOOLEAN_SERIALIZER,
        Byte.class, BYTE_SERIALIZER,
        byte.class, BYTE_SERIALIZER,
        Short.class, SHORT_SERIALIZER,
        short.class, SHORT_SERIALIZER,
        Integer.class, INTEGER_SERIALIZER,
        int.class, INTEGER_SERIALIZER,
        Long.class, LONG_SERIALIZER,
        long.class, LONG_SERIALIZER,
        Float.class, FLOAT_SERIALIZER,
        float.class, FLOAT_SERIALIZER,
        Double.class, DOUBLE_SERIALIZER,
        double.class, DOUBLE_SERIALIZER,
        String.class, STRING_SERIALIZER
    );

    @Nonnull
    public static ChunkAwareSerializer<Boolean> booleanSerializer() {
        return BOOLEAN_SERIALIZER;
    }

    @Nonnull
    public static ChunkAwareSerializer<Byte> byteSerializer() {
        return BYTE_SERIALIZER;
    }

    @Nonnull
    public static ChunkAwareSerializer<Short> shortSerializer() {
        return SHORT_SERIALIZER;
    }

    @Nonnull
    public static ChunkAwareSerializer<Integer> integerSerializer() {
        return INTEGER_SERIALIZER;
    }

    @Nonnull
    public static ChunkAwareSerializer<Long> longSerializer() {
        return LONG_SERIALIZER;
    }

    @Nonnull
    public static ChunkAwareSerializer<Float> floatSerializer() {
        return FLOAT_SERIALIZER;
    }

    @Nonnull
    public static ChunkAwareSerializer<Double> doubleSerializer() {
        return DOUBLE_SERIALIZER;
    }

    @Nonnull
    public static Serializer<String> stringSerializer() {
        return STRING_SERIALIZER;
    }

    @Nonnull
    public static ChunkAwareSerializer<String> stringSerializer(@Nonnegative int bufferSize, @Nonnull Charset charset) {
        return new ChunkAwareStringSerializer(bufferSize, charset);
    }

    @Nonnull
    public static ChunkAwareSerializer<String> stringSerializer(@Nonnegative int bufferSize) {
        return stringSerializer(bufferSize, defaultCharset());
    }

    /**
     * @return size of the chunk for given <code>type</code>.
     * @throws IllegalArgumentException if there is no {@link org.echocat.jomon.runtime.io.ChunkAwareSerializer} available for given <code>type</code>.
     * In this case the type was not annotated with {@link org.echocat.jomon.runtime.io.SerializableBy}.
     */
    @Nonnegative
    public static int getChunkSizeOf(@Nonnull Class<?> type) throws IllegalArgumentException {
        final ChunkAwareSerializer<?> serializer = getChunkAwareSerializerOf(type);
        return serializer.getChunkSize();
    }

    /**
     * @return <code>null</code> if there is no {@link org.echocat.jomon.runtime.io.Serializer} available for given <code>type</code>. In this case
     * the type was not annotated with {@link org.echocat.jomon.runtime.io.SerializableBy}. Otherwise the chunk size for the given <code>type</code>.
     */
    @Nullable
    @Nonnegative
    public static Integer findChunkSizeOf(@Nonnull Class<?> type) {
        final ChunkAwareSerializer<?> serializer = findChunkAwareSerializerOf(type);
        return serializer != null ? serializer.getChunkSize() : null;
    }

    /**
     * @throws IllegalArgumentException if there is no {@link org.echocat.jomon.runtime.io.ChunkAwareSerializer} available for given <code>type</code>.
     * In this case the type was not annotated with {@link org.echocat.jomon.runtime.io.SerializableBy}.
     */
    @Nonnull
    public static <T> ChunkAwareSerializer<T> getChunkAwareSerializerOf(@Nonnull Class<T> type) throws IllegalArgumentException {
        final ChunkAwareSerializer<T> result = findChunkAwareSerializerOf(type);
        if (result == null) {
            throw new IllegalArgumentException("There is no serializer available for " + type.getName() + ".");
        }
        return result;
    }

    /**
     * @throws IllegalArgumentException if there is no {@link org.echocat.jomon.runtime.io.Serializer} available for given <code>type</code>.
     * In this case the type was not annotated with {@link org.echocat.jomon.runtime.io.SerializableBy}.
     */
    @Nonnull
    public static <T> Serializer<T> getSerializerOf(@Nonnull Class<T> type) throws IllegalArgumentException {
        final Serializer<T> result = findSerializerOf(type);
        if (result == null) {
            throw new IllegalArgumentException("There is no serializer available for " + type.getName() + ".");
        }
        return result;
    }

    /**
     * @return <code>null</code> if there is no {@link org.echocat.jomon.runtime.io.Serializer} available for given <code>type</code>. In this case
     * the type was not annotated with {@link org.echocat.jomon.runtime.io.SerializableBy}.
     */
    @Nullable
    public static <T> Serializer<T> findSerializerOf(@Nonnull Class<T> type) {
        Serializer<?> result = TYPE_TO_SERIALIZER.get(type);
        if (result == null) {
            synchronized (TYPE_TO_SERIALIZER_CACHE) {
                result = TYPE_TO_SERIALIZER_CACHE.get(type);
                if (result == null) {
                    final Class<Serializer<T>> serializerType = findSerializerTypeOf(type);
                    if (serializerType != null) {
                        try {
                            result = serializerType.newInstance();
                        } catch (Exception e) {
                            throw new RuntimeException("Could not create an instance of " + serializerType.getName() + " to serialize " + type.getName() + ".", e);
                        }
                        TYPE_TO_SERIALIZER_CACHE.put(type, result);
                    } else {
                        result = null;
                    }
                }
            }
        }
        // noinspection unchecked
        return (Serializer<T>) result;
    }

    /**
     * @return <code>null</code> if there is no {@link org.echocat.jomon.runtime.io.ChunkAwareSerializer} available for given <code>type</code>. In this case
     * the type was not annotated with {@link org.echocat.jomon.runtime.io.SerializableBy}.
     */
    @Nullable
    public static <T> ChunkAwareSerializer<T> findChunkAwareSerializerOf(@Nonnull Class<T> type) {
        ChunkAwareSerializer<?> result = TYPE_TO_CHUNK_AWARE_SERIALIZER.get(type);
        if (result == null) {
            final Serializer<T> serializer = findSerializerOf(type);
            result = serializer instanceof ChunkAwareSerializer ? (ChunkAwareSerializer<?>) serializer : null;
        }
        // noinspection unchecked
        return (ChunkAwareSerializer<T>) result;
    }

    /**
     * @return <code>null</code> if there is no {@link org.echocat.jomon.runtime.io.Serializer} available for given <code>type</code>. In this case
     * the type was not annotated with {@link org.echocat.jomon.runtime.io.SerializableBy}.
     */
    @Nullable
    public static <T> Class<Serializer<T>> findSerializerTypeOf(@Nonnull Class<T> type) {
        final SerializableBy annotation = type.getAnnotation(SerializableBy.class);
        // noinspection unchecked
        return annotation != null ? (Class) annotation.value() : null;
    }

    @ThreadSafe
    @Immutable
    public static class BooleanSerializer implements ChunkAwareSerializer<Boolean> {
        @Override public int getChunkSize() { return BOOLEAN_CHUNK_SIZE; }
        @Nonnull @Override public Boolean read(@Nonnull DataInput from) throws IOException { return from.readBoolean(); }
        @Override public void write(@Nonnull Boolean value, @Nonnull DataOutput to) throws IOException { to.writeBoolean(value); }
    }

    @ThreadSafe
    @Immutable
    public static class ByteSerializer implements ChunkAwareSerializer<Byte> {
        @Override public int getChunkSize() { return BYTE_CHUNK_SIZE; }
        @Nonnull @Override public Byte read(@Nonnull DataInput from) throws IOException { return from.readByte(); }
        @Override public void write(@Nonnull Byte value, @Nonnull DataOutput to) throws IOException { to.writeByte(value); }
    }

    @ThreadSafe
    @Immutable
    public static class ShortSerializer implements ChunkAwareSerializer<Short> {
        @Override public int getChunkSize() { return SHORT_CHUNK_SIZE; }
        @Nonnull @Override public Short read(@Nonnull DataInput from) throws IOException { return from.readShort(); }
        @Override public void write(@Nonnull Short value, @Nonnull DataOutput to) throws IOException { to.writeShort(value); }
    }

    @ThreadSafe
    @Immutable
    public static class IntegerSerializer implements ChunkAwareSerializer<Integer> {
        @Override public int getChunkSize() { return INTEGER_CHUNK_SIZE; }
        @Nonnull @Override public Integer read(@Nonnull DataInput from) throws IOException { return from.readInt(); }
        @Override public void write(@Nonnull Integer value, @Nonnull DataOutput to) throws IOException { to.writeInt(value); }
    }

    @ThreadSafe
    @Immutable
    public static class LongSerializer implements ChunkAwareSerializer<Long> {
        @Override public int getChunkSize() { return LONG_CHUNK_SIZE; }
        @Nonnull @Override public Long read(@Nonnull DataInput from) throws IOException { return from.readLong(); }
        @Override public void write(@Nonnull Long value, @Nonnull DataOutput to) throws IOException { to.writeLong(value); }
    }

    @ThreadSafe
    @Immutable
    public static class FloatSerializer implements ChunkAwareSerializer<Float> {
        @Override public int getChunkSize() { return FLOAT_CHUNK_SIZE; }
        @Nonnull @Override public Float read(@Nonnull DataInput from) throws IOException { return from.readFloat(); }
        @Override public void write(@Nonnull Float value, @Nonnull DataOutput to) throws IOException { to.writeFloat(value); }
    }

    @ThreadSafe
    @Immutable
    public static class DoubleSerializer implements ChunkAwareSerializer<Double> {
        @Override public int getChunkSize() { return DOUBLE_CHUNK_SIZE; }
        @Nonnull @Override public Double read(@Nonnull DataInput from) throws IOException { return from.readDouble(); }
        @Override public void write(@Nonnull Double value, @Nonnull DataOutput to) throws IOException { to.writeDouble(value); }
    }

    @ThreadSafe
    @Immutable
    public static class ChunkAwareStringSerializer implements ChunkAwareSerializer<String> {

        @Nonnull
        private final Charset _charset;
        @Nonnegative
        private final int _bufferSize;
        @Nonnegative
        private final int _chunkSize;

        public ChunkAwareStringSerializer(@Nonnegative int bufferSize, @Nonnull Charset charset) {
            _bufferSize = bufferSize;
            _charset = charset;
            _chunkSize = getStringChunkSizeFor(bufferSize);
        }

        @Override
        public int getChunkSize() {
            return _chunkSize;
        }

        @Nullable
        @Override
        public String read(@Nonnull DataInput from) throws IOException {
            final byte[] buffer = new byte[_chunkSize];
            from.readFully(buffer);
            return getString(buffer, _bufferSize, 0, _charset);
        }

        @Override
        public void write(@Nullable String value, @Nonnull DataOutput to) throws IOException {
            final byte[] buffer = new byte[_chunkSize];
            putString(buffer, _bufferSize, 0, value, _charset);
            to.write(buffer);
        }

    }

    @ThreadSafe
    @Immutable
    public static class StringSerializer implements Serializer<String> {

        @Nullable
        @Override
        public String read(@Nonnull DataInput from) throws IOException {
            return from.readUTF();
        }

        @Override
        public void write(@Nullable String value, @Nonnull DataOutput to) throws IOException {
            to.writeUTF(value);
        }

    }

    private Serializers() {}
}

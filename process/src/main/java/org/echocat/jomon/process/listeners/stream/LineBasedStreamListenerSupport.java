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

package org.echocat.jomon.process.listeners.stream;

import org.echocat.jomon.process.GeneratedProcess;
import org.echocat.jomon.runtime.CollectionUtils;
import org.echocat.jomon.runtime.format.MessageFormatter;
import org.echocat.jomon.runtime.io.StreamType;
import org.echocat.jomon.runtime.util.ByteCount;
import org.echocat.jomon.runtime.util.Consumer;
import org.echocat.jomon.runtime.util.Duration;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.currentTimeMillis;
import static java.nio.ByteBuffer.wrap;
import static java.nio.charset.Charset.defaultCharset;
import static java.nio.charset.Charset.forName;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.echocat.jomon.runtime.format.DefaultMessageFormatter.messageFormatterFor;
import static org.echocat.jomon.runtime.util.ByteCount.byteCount;
import static org.echocat.jomon.runtime.util.Duration.duration;

@ThreadSafe
public abstract class LineBasedStreamListenerSupport<P extends GeneratedProcess<?, ?>, T extends LineBasedStreamListenerSupport<P, T>> implements StreamListener<P>, Closeable {

    public static final String[] KEYS = new String[]{"timeStamp", "streamType", "message", "process"};

    private final Map<BufferKey<P>, ByteBuffer> _outputBuffers = new HashMap<>();
    private final Lock _lock = new ReentrantLock();
    private final Condition _condition = _lock.newCondition();

    @Nonnull
    private MessageFormatter _formatter = messageFormatterFor("[{timeStamp,date,yyyy-MM-dd HH:mm:ss} {streamType}] {message}\n", KEYS);

    private volatile boolean _closed;

    @Nonnull
    private Duration _maximumAllowedTimeWithoutFlush = duration("10s");
    @Nonnull
    private ByteCount _bufferSize = byteCount("1k");
    @Nonnegative
    private int _maximumNumberOfBuffers = StreamType.values().length;
    @Nonnull
    private Charset _charset = defaultCharset();

    @Override
    public boolean canHandleReferenceType(@Nonnull Class<?> type) {
        return true;
    }

    @Nonnull
    public T whichFormatsMessagesWith(@Nonnull MessageFormatter formatter) {
        _formatter = formatter;
        return thisObject();
    }

    @Nonnull
    public T whichFormatsMessagesWith(@Nonnull String pattern, @Nonnull Locale locale) {
        return whichFormatsMessagesWith(messageFormatterFor(pattern, locale, KEYS));
    }

    @Nonnull
    public T whichFormatsMessagesWith(@Nonnull String pattern) {
        return whichFormatsMessagesWith(messageFormatterFor(pattern, KEYS));
    }

    @Nonnull
    public T withMaximumNumberOfBuffers(@Nonnegative int value) {
        _maximumNumberOfBuffers = value;
        return thisObject();
    }

    @Nonnull
    public T withMaximumAllowedTimeWithoutFlush(@Nonnull Duration duration) {
        _maximumAllowedTimeWithoutFlush = duration;
        return thisObject();
    }

    @Nonnull
    public T withMaximumAllowedTimeWithoutFlush(@Nonnull String duration) {
        return withMaximumAllowedTimeWithoutFlush(duration(duration));
    }

    @Nonnull
    public T withBufferSize(@Nonnull String byteCount) {
        return withBufferSize(byteCount(byteCount));
    }

    @Nonnull
    public T withBufferSize(@Nonnull ByteCount byteCount) {
        _bufferSize = byteCount;
        return thisObject();
    }

    @Nonnull
    public T withCharset(@Nonnull Charset charset) {
        _charset = charset;
        return thisObject();
    }

    @Nonnull
    public T withCharset(@Nonnull String charset) {
        return withCharset(forName(charset));
    }

    @Override public void notifyProcessStarted(@Nonnull P process) {}
    @Override public void notifyProcessStartupDone(@Nonnull P process) {}
    @Override public void notifyProcessTerminated(@Nonnull P process, boolean regular) {}

    @Override
    public void notifyOutput(@Nonnull P process, @Nonnull byte[] data, @Nonnegative int offset, @Nonnegative int length, @Nonnull StreamType streamType) {
        formatAndWrite(process, data, offset, length, streamType);
    }

    @Override
    public void flushOutput(@Nonnull P process, @Nonnull StreamType streamType) {
        final BufferKey<P> key = new BufferKey<>(process, streamType);
        final ByteBuffer buffer = getBufferFor(key);
        // noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (key) {
            flush(key, buffer);
        }
    }

    protected void formatAndWrite(@Nonnull P process, @Nonnull String line, @Nonnull StreamType streamType) {
        if (_closed) {
            throw new IllegalStateException("Already closed.");
        }
        write(process, format(process, line, streamType), streamType);
    }

    protected void formatAndWrite(@Nonnull P process, @Nonnull byte[] data, @Nonnegative int offset, @Nonnegative int length, @Nonnull StreamType streamType) {
        final BufferKey<P> key = new BufferKey<>(process, streamType);
        final ByteBuffer buffer = getBufferFor(key);
        each(data, offset, length, new Consumer<ByteBuffer, RuntimeException>() { @Override public void consume(@Nullable ByteBuffer value) {
            synchronized (key) {
                buffer.put(value);
                if (endsWithNewLine(buffer)) {
                    flush(key, buffer);
                }
            }
        }});
    }

    protected void flush(@Nonnull BufferKey<P> key, @Nonnull ByteBuffer buffer) {
        buffer.flip();
        final CharBuffer decoded = _charset.decode(buffer);
        formatAndWrite(key.getProcess(), decoded.toString(), key.getStreamType());
        buffer.clear();
        key.notifyFlushed();
    }

    protected boolean endsWithNewLine(@Nonnull ByteBuffer buffer) {
        return buffer.position() > 0 && buffer.get(buffer.position() - 1) == '\n';
    }

    @Nonnull
    protected void each(@Nonnull byte[] data, @Nonnegative int offset, @Nonnegative int length, @Nonnull Consumer<ByteBuffer, RuntimeException> consumer) {
        int currentStart = offset;
        for (int i = offset; i < length; i++) {
            if (data[i] == '\n') {
                consumer.consume(wrap(data, currentStart, (i + 1) - currentStart));
                currentStart = i + 1;
            }
        }
        if (length > currentStart) {
            consumer.consume(wrap(data, currentStart, length - currentStart));
        }
    }

    protected abstract void write(@Nonnull P process, @Nonnull String content, @Nonnull StreamType streamType);

    @Nonnull
    protected String format(@Nonnull P process, @Nonnull String line, @Nonnull StreamType streamType) {
        return _formatter.format(CollectionUtils.<String, Object>asMap(
            "process", process,
            "timeStamp", new Date(),
            "streamType", streamType,
            "message", removeLastLineBreakFrom(line)
        ));
    }

    @Nonnull
    protected String removeLastLineBreakFrom(@Nonnull String step0) {
        final String step1 = step0.endsWith("\n") ? step0.substring(0, step0.length() - 1) : step0;
        final String step2 = step1.endsWith("\r") ? step1.substring(0, step1.length() - 1) : step1;
        final String step3 = step2.endsWith("\n") ? step2.substring(0, step1.length() - 2) : step2;
        return step3;
    }

    protected void flush(boolean force) {
        final Duration maximumAllowedTimeWithoutFlush = _maximumAllowedTimeWithoutFlush;
        synchronized (_outputBuffers) {
            for (final Entry<BufferKey<P>, ByteBuffer> keyAndBuffer : _outputBuffers.entrySet()) {
                final BufferKey<P> key = keyAndBuffer.getKey();
                final ByteBuffer buffer = keyAndBuffer.getValue();
                // noinspection SynchronizationOnLocalVariableOrMethodParameter
                synchronized (key) {
                    if (force || key.isFlushNeeded(maximumAllowedTimeWithoutFlush)) {
                        flush(key, buffer);
                    }
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        _closed = true;
        _lock.lock();
        try {
            flush(true);
            _outputBuffers.clear();
        } finally {
            _lock.unlock();
        }
    }

    @Nonnull
    public MessageFormatter getFormatter() {
        return _formatter;
    }

    public boolean isClosed() {
        return _closed;
    }

    @Nonnull
    public Duration getMaximumAllowedTimeWithoutFlush() {
        return _maximumAllowedTimeWithoutFlush;
    }

    @Nonnull
    public ByteCount getBufferSize() {
        return _bufferSize;
    }

    @Nonnegative
    public int getMaximumNumberOfBuffers() {
        return _maximumNumberOfBuffers;
    }

    @Nonnull
    public Charset getCharset() {
        return _charset;
    }

    @Nonnull
    protected T thisObject() {
        // noinspection unchecked
        return (T) this;
    }

    @Nonnull
    protected ByteBuffer getBufferFor(@Nonnull BufferKey<P> key) {
        synchronized (_outputBuffers) {
            ByteBuffer buffer = _outputBuffers.get(key);
            if (buffer == null) {
                buffer = _bufferSize.allocateBuffer();
                if (_outputBuffers.size() >= _maximumNumberOfBuffers && !_outputBuffers.isEmpty()) {
                    final BufferKey<P> firstKey = _outputBuffers.keySet().iterator().next();
                    final ByteBuffer old = _outputBuffers.remove(firstKey);
                    flush(key, old);
                }
                if (_maximumNumberOfBuffers >= 1) {
                    _outputBuffers.put(key, buffer);
                }
            }
            return buffer;
        }
    }

    @Nonnull
    protected Lock lock() {
        return _lock;
    }

    @Nonnull
    protected Condition condition() {
        return _condition;
    }

    protected static class BufferKey<P extends GeneratedProcess<?, ?>> {

        @Nonnull
        private final P _process;
        @Nonnull
        private final StreamType _streamType;
        @Nonnegative
        private long _lastFlushAtInMillis;

        public BufferKey(@Nonnull P process, @Nonnull StreamType streamType) {
            _process = process;
            _streamType = streamType;
            notifyFlushed();
        }

        @Nonnull
        public P getProcess() {
            return _process;
        }

        @Nonnull
        public StreamType getStreamType() {
            return _streamType;
        }

        @Nonnegative
        public long getLastFlushAtInMillis() {
            return _lastFlushAtInMillis;
        }

        public boolean isFlushNeeded(@Nonnull Duration maximumAllowedTimeWithoutFlush) {
            final long flushRequiredAt = _lastFlushAtInMillis + maximumAllowedTimeWithoutFlush.in(MILLISECONDS);
            return flushRequiredAt <= currentTimeMillis();
        }

        public void notifyFlushed() {
            _lastFlushAtInMillis = currentTimeMillis();
        }

        @Override
        public boolean equals(Object o) {
            final boolean result;
            if (this == o) {
                result = true;
            } else if (o == null || getClass() != o.getClass()) {
                result = false;
            } else {
                // noinspection unchecked
                final BufferKey<?> that = (BufferKey) o;
                result = _process.equals(that._process) && _streamType == that._streamType;
            }
            return result;
        }

        @Override
        public int hashCode() {
            int result = _process.hashCode();
            result = 31 * result + _streamType.hashCode();
            return result;
        }

    }

}

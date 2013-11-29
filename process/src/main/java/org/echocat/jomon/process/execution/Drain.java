package org.echocat.jomon.process.execution;

import org.echocat.jomon.runtime.logging.LogLevel;
import org.echocat.jomon.runtime.util.ByteCount;
import org.echocat.jomon.runtime.util.Consumer;
import org.slf4j.Logger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import static java.lang.System.err;
import static java.lang.System.out;
import static java.nio.ByteBuffer.wrap;
import static java.nio.charset.Charset.defaultCharset;
import static java.nio.charset.Charset.forName;
import static org.echocat.jomon.process.execution.Drain.ForOutputStream.drainForOutputStream;
import static org.echocat.jomon.process.execution.Drain.Noop.drainThatDoNothing;
import static org.echocat.jomon.runtime.logging.LogLevel.info;
import static org.echocat.jomon.runtime.logging.Slf4jUtils.log;
import static org.echocat.jomon.runtime.util.ByteCount.byteCountOf;
import static org.slf4j.LoggerFactory.getLogger;

@SuppressWarnings({"UseOfSystemOutOrSystemErr", "ConstantNamingConvention"})
public interface Drain extends AutoCloseable {

    @Nonnull
    public static final Drain stdout = drainForOutputStream(out);
    @Nonnull
    public static final Drain stderr = drainForOutputStream(err);
    @Nonnull
    public static final Drain noop = drainThatDoNothing();

    public void drain(@Nonnull byte[] buffer, @Nonnegative int offset, @Nonnegative int length) throws Exception;

    public static class Noop implements Drain {

        @Nonnull
        public static Noop noop() {
            return drainThatDoNothing();
        }

        @Nonnull
        public static Noop drainThatDoNothing() {
            return new Noop();
        }

        @Override
        public void drain(@Nonnull byte[] buffer, @Nonnegative int offset, @Nonnegative int length) throws Exception {}

        @Override
        public void close() throws Exception {}
    }

    public static class AsBuffering implements Drain {

        @Nonnull
        public static AsBuffering drainThatBuffers() {
            return new AsBuffering();
        }

        private final ByteArrayOutputStream _buffer = new ByteArrayOutputStream();

        @Override
        public void drain(@Nonnull byte[] buffer, @Nonnegative int offset, @Nonnegative int length) throws Exception {
            _buffer.write(buffer, offset, length);
        }

        @Override
        public void close() throws Exception {}

        @Nonnull
        public byte[] toByteArray() {
            return _buffer.toByteArray();
        }

        @Nonnull
        public String toString() {
            return _buffer.toString();
        }

        @Nonnull
        public String toString(@Nonnull Charset charset) {
            try {
                return _buffer.toString(charset.name());
            } catch (final UnsupportedEncodingException e) {
                throw new RuntimeException("Could not be.", e);
            }
        }

        @Nonnull
        public String toString(@Nonnull String charset) throws UnsupportedEncodingException {
            return _buffer.toString(charset);
        }

    }

    public static class ForOutputStream implements Drain {

        @Nonnull
        public static ForOutputStream drainForOutputStream(@Nonnull OutputStream stream) {
            return new ForOutputStream(stream);
        }

        @Nonnull
        private final OutputStream _stream;

        public ForOutputStream(@Nonnull OutputStream stream) {
            _stream = stream;
        }

        @Override
        public void drain(@Nonnull byte[] buffer, @Nonnegative int offset, @Nonnegative int length) throws Exception {
            _stream.write(buffer, offset, length);
        }

        @Override
        public void close() throws Exception {
            _stream.close();
        }

    }

    public static class ForLogger implements Drain {

        @Nonnull
        protected static final Logger DEFAULT_LOGGER = getLogger(Drain.class); 
        @Nonnull
        protected static final LogLevel DEFAULT_LOG_LEVEL = info;
        @Nonnull
        protected static final Charset DEFAULT_CHARSET = defaultCharset();
        @Nonnull
        protected static final ByteCount DEFAULT_BUFFER_SIZE = byteCountOf("4k");

        @Nonnull
        public static ForLogger drainForLogger() {
            return drainForLogger((Logger) null);
        }
        
        @Nonnull
        public static ForLogger drainForLogger(@Nullable Class<?> clazz) {
            return drainForLogger(toLogger(clazz));
        }

        @Nonnull
        public static ForLogger drainForLogger(@Nullable String loggerName) {
            return drainForLogger(toLogger(loggerName));
        }
        
        @Nonnull
        public static ForLogger drainForLogger(@Nullable Logger logger) {
            return new ForLogger(logger != null ? logger : DEFAULT_LOGGER);
        }

        @Nonnull
        protected static Logger toLogger(@Nullable String loggerName) {
            return loggerName != null ? getLogger(loggerName) : DEFAULT_LOGGER;
        }

        @Nonnull
        protected static Logger toLogger(@Nullable Class<?> clazz) {
            return clazz != null ? getLogger(clazz) : DEFAULT_LOGGER;
        }

        @Nonnull
        private final Logger _logger;
        @Nonnull
        @GuardedBy("this")
        private ByteBuffer _buffer = DEFAULT_BUFFER_SIZE.allocateBuffer();
        @Nonnull
        private volatile Charset _charset = DEFAULT_CHARSET;

        @Nonnull
        private volatile LogLevel _logLevel = DEFAULT_LOG_LEVEL;

        public ForLogger(@Nonnull Logger logger) {
            _logger = logger;
        }

        @Override
        public void drain(@Nonnull byte[] data, @Nonnegative int offset, @Nonnegative int length) throws Exception {
            synchronized (this) {
                each(data, offset, length, new Consumer<ByteBuffer, RuntimeException>() { @Override public void consume(@Nullable ByteBuffer value) {
                    _buffer.put(value);
                    if (endsWithNewLine(_buffer)) {
                        flush(_buffer);
                    }
                }});
            }
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

        protected boolean endsWithNewLine(@Nonnull ByteBuffer buffer) {
            return buffer.position() > 0 && buffer.get(buffer.position() - 1) == '\n';
        }

        protected void flush(@Nonnull ByteBuffer buffer) {
            buffer.flip();
            final CharBuffer decoded = _charset.decode(buffer);
            log(_logger, _logLevel, decoded.toString());
            buffer.clear();
        }

        @Nonnull
        public ForLogger bufferingWith(@Nonnull ByteBuffer buffer) {
            synchronized (this) {
                _buffer = buffer;
            }
            return this;
        }

        @Nonnull
        public ForLogger buffering(@Nonnull ByteCount bytes) {
            return bufferingWith(bytes.allocateBuffer());
        }

        @Nonnull
        public ForLogger buffering(@Nonnull String byteCount) {
            return buffering(byteCountOf(byteCount));
        }

        @Nonnull
        public ForLogger decodedBy(@Nonnull String charset) {
            return decodedBy(forName(charset));
        }

        @Nonnull
        public ForLogger decodedBy(@Nonnull Charset charset) {
            _charset = charset;
            return this;
        }

        @Nonnull
        public ForLogger loggingOn(@Nonnull LogLevel level) {
            _logLevel = level;
            return this;
        }

        @Override
        public void close() throws Exception {}

    }

}

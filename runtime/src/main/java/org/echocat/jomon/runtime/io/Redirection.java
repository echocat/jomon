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

import org.echocat.jomon.runtime.logging.LogLevel;
import org.echocat.jomon.runtime.logging.Slf4jUtils;
import org.slf4j.Logger;
import org.slf4j.spi.LocationAwareLogger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static org.echocat.jomon.runtime.CollectionUtils.asImmutableList;

@SuppressWarnings("ConstantNamingConvention")
public interface Redirection extends Closeable {

    public static interface In extends Redirection {

        public static final In stdin = new StreamBasedSupport(System.in, false);
        public static final In noop = new In() {
            @Override public int receive(@Nonnull byte[] bytes, @Nonnegative int offset, @Nonnegative int length) throws IOException { return -1; }
            @Override public void close() throws IOException { }
        };

        public int receive(@Nonnull byte[] bytes, @Nonnegative int offset, @Nonnegative int length) throws IOException;

        public static class StreamBasedSupport extends Redirection.StreamBasedSupport<InputStream> implements In {

            public StreamBasedSupport(@Nonnull InputStream stream) {
                this(stream, true);
            }

            public StreamBasedSupport(@Nonnull InputStream stream, boolean closeStream) {
                super(stream, closeStream);
            }

            @Override
            public int receive(@Nonnull byte[] bytes, @Nonnegative int offset, @Nonnegative int length) throws IOException {
                return getStream().read(bytes, offset, length);
            }

        }

        public static class StreamAdapter extends InputStream {

            @Nonnull
            public static InputStream inputStreamFor(@Nonnull In in) {
                // noinspection OverlyStrongTypeCast
                return in instanceof StreamBasedSupport ? ((StreamBasedSupport) in).getStream() : new StreamAdapter(in);
            }

            private final In _in;

            public StreamAdapter(@Nonnull In in) {
                _in = in;
            }

            @Override
            public int read() throws IOException {
                final byte[] bytes = new byte[1];
                final int read = read(bytes, 0, 1);
                return read < 0 ? -1 : 0xff & bytes[0];
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                return _in.receive(b, off, len);
            }

            @Override
            public void close() throws IOException {
                try {
                    super.close();
                } finally {
                    _in.close();
                }
            }
        }

    }

    public static interface Out extends Redirection {

        @SuppressWarnings("UseOfSystemOutOrSystemErr")
        public static final Out stdout = new StreamBasedSupport(System.out, false);
        @SuppressWarnings("UseOfSystemOutOrSystemErr")
        public static final Out stderr = new StreamBasedSupport(System.err, false);
        public static final Out noop = new Out() {
            @Override public void send(@Nonnull byte[] bytes, @Nonnegative int offset, @Nonnegative int length) throws IOException {}
            @Override public void flush() throws IOException {}
            @Override public void close() throws IOException {}
        };

        public void send(@Nonnull byte[] bytes, @Nonnegative int offset, @Nonnegative int length) throws IOException;
        public void flush() throws IOException;

        public static class ByteArrayOut extends StreamBasedSupport {

            public ByteArrayOut() {
                super(new ByteArrayOutputStream());
            }

            @Nonnull
            public byte[] toByteArray() {
                return ((ByteArrayOutputStream)getStream()).toByteArray();
            }

            @Override
            public String toString() {
                return getStream().toString();
            }

            @Nonnull
            public String toString(@Nonnull Charset charset) {
                final byte[] bytes = toByteArray();
                return new String(bytes, 0, bytes.length, charset);
            }

        }

        public static class StreamBasedSupport extends Redirection.StreamBasedSupport<OutputStream> implements Out {

            public StreamBasedSupport(@Nonnull OutputStream stream) {
                this(stream, true);
            }

            public StreamBasedSupport(@Nonnull OutputStream stream, boolean closeStream) {
                super(stream, closeStream);
            }

            @Override
            public void send(@Nonnull byte[] bytes, @Nonnegative int offset, @Nonnegative int length) throws IOException {
                getStream().write(bytes, offset, length);
            }

            @Override
            public void flush() throws IOException {
                getStream().flush();
            }
        }

        public static class LoggerBasedSupport extends Redirection.LoggerBasedSupport implements Out {

            public LoggerBasedSupport(@Nonnull Logger logger, @Nonnull LogLevel logLevel) {
                super(logger, logLevel);
            }

            @Override
            public void send(@Nonnull byte[] bytes, @Nonnegative int offset, @Nonnegative int length) throws IOException {
                log(new String(bytes, offset, length));
            }

            @Override public void flush() throws IOException {}
        }

        public static class Compound extends CompoundSupport<Out> implements Out {

            @Nonnull
            public static Out outOf(@Nullable Out... delegates) {
                return new Compound(delegates);
            }

            @Nonnull
            public static Out outOf(@Nullable Iterable<Out> delegates) {
                return new Compound(delegates);
            }

            public Compound(@Nullable Out... delegates) {
                this(asImmutableList(delegates));
            }

            public Compound(@Nullable Iterable<Out> delegates) {
                super(delegates);
            }

            @Override
            public void send(@Nonnull byte[] bytes, @Nonnegative int offset, @Nonnegative int length) throws IOException {
                for (Out delegate : getDelegates()) {
                    delegate.send(bytes, offset, length);
                }
            }

            @Override
            public void flush() throws IOException {
                for (Out delegate : getDelegates()) {
                    delegate.flush();
                }
            }
        }

        public static class StreamAdapter extends OutputStream {

            @Nonnull
            public static OutputStream outputStreamFor(@Nonnull Out out) {
                // noinspection OverlyStrongTypeCast
                return out instanceof StreamBasedSupport ? ((StreamBasedSupport) out).getStream() : new StreamAdapter(out);
            }

            private final Out _out;

            public StreamAdapter(@Nonnull Out out) {
                _out = out;
            }

            @Override
            public void write(int b) throws IOException {
                write(new byte[]{(byte) b});
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                _out.send(b, off, len);
            }

            @Override
            public void close() throws IOException {
                try {
                    super.close();
                } finally {
                    _out.close();
                }
            }
        }

    }

    public abstract static class StreamBasedSupport<S extends Closeable> implements Redirection {

        @Nonnull
        private final S _stream;
        private final boolean _closeStream;

        public StreamBasedSupport(@Nonnull S stream, boolean closeStream) {
            _stream = stream;
            _closeStream = closeStream;
        }

        @Nonnull
        public S getStream() {
            return _stream;
        }

        @Override
        public void close() throws IOException {
            if (_closeStream) {
                _stream.close();
            }
        }
    }

    public abstract static class LoggerBasedSupport implements Redirection {

        @Nonnull
        private final String _fqcn = getClass().getName();
        @Nonnull
        private final Logger _logger;
        @Nonnull
        private final LogLevel _logLevel;

        public LoggerBasedSupport(@Nonnull Logger logger, @Nonnull LogLevel logLevel) {
            _logger = logger;
            _logLevel = logLevel;
        }

        @Nonnull
        protected Logger getLogger() {
            return _logger;
        }

        protected void log(@Nullable String message) {
            if (_logger instanceof LocationAwareLogger) {
                Slf4jUtils.log((LocationAwareLogger) _logger, _logLevel, _fqcn, null, message, null, null);
            } else {
                Slf4jUtils.log(_logger, _logLevel, message);
            }
        }

        @Override public void close() throws IOException {}

    }

    public abstract static class CompoundSupport<T extends Redirection> implements Redirection {

        private final List<T> _delegates;

        public CompoundSupport(@Nullable Iterable<T> delegates) {
            _delegates = asImmutableList(delegates);
        }

        @Nonnull
        protected List<T> getDelegates() {
            return _delegates;
        }

        @Override
        public void close() throws IOException {
            final List<Throwable> throwables = new ArrayList<>();
            for (T delegate : _delegates) {
                try {
                    delegate.close();
                } catch (Throwable e) {
                    throwables.add(e);
                }
            }
            if (!throwables.isEmpty()) {
                final Throwable first = throwables.get(0);
                if (first instanceof IOException) {
                    throw (IOException) first;
                } else if (first instanceof RuntimeException) {
                    throw (RuntimeException) first;
                } else if (first instanceof Error) {
                    throw (Error) first;
                } else {
                    throw new IOException(first.getMessage(), first);
                }
            }
        }

    }

}

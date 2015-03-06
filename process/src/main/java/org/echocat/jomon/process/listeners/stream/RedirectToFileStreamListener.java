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
import org.echocat.jomon.runtime.concurrent.RetryForSpecifiedCountStrategy;
import org.echocat.jomon.runtime.io.StreamType;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import java.io.*;
import java.util.Map;
import java.util.concurrent.Callable;

import static java.lang.Integer.parseInt;
import static org.apache.commons.io.FileUtils.openOutputStream;
import static org.echocat.jomon.runtime.concurrent.Retryer.executeWithRetry;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

@ThreadSafe
public class RedirectToFileStreamListener<P extends GeneratedProcess<?, ?>> extends LineBasedAndStateEnabledStreamListenerSupport<P, RedirectToFileStreamListener<P>> {

    @Nonnull
    public static <P extends GeneratedProcess<?, ?>> RedirectToFileStreamListener<P> redirectToFile(@Nonnull File file) {
        return new RedirectToFileStreamListener<>(file);
    }

    @Nonnull
    private final File _file;
    @Nonnull
    private final RetryForSpecifiedCountStrategy<Void> _strategy = RetryForSpecifiedCountStrategy.<Void>retryForSpecifiedCountOf(5).withExceptionsThatForceRetry(IOException.class);

    private boolean _append;

    @Nullable
    private Writer _writer;

    public RedirectToFileStreamListener(@Nonnull File file) {
        _file = file;
    }

    @Override
    protected void write(@Nonnull P process, @Nonnull final String content, @Nonnull StreamType streamType) {
        try {
            synchronized (this) {
                writeThrowingIOException(content);
            }
        } catch (final IOException e) {
            throw new RuntimeException("Could not write message to file: " + _file, e);
        }
    }

    @GuardedBy("this")
    protected void writeThrowingIOException(@Nonnull final String content) throws IOException {
        executeWithRetry(new Callable<Void>() { @Override public Void call() throws IOException {
            final Writer writer = getWriter();
            try {
                writer.write(content);
            } catch (final IOException e) {
                _writer = null;
                throw e;
            }
            try {
                writer.flush();
            } catch (final IOException ignored) {}
            return null;
        }}, _strategy, IOException.class);
    }

    @Nonnull
    @GuardedBy("this")
    protected Writer getWriter() throws IOException {
        if (_writer == null) {
            boolean success = false;
            final OutputStream os = openOutputStream(_file, _append);
            try {
                _writer = new OutputStreamWriter(os, getCharset());
                success = true;
            } finally {
                if (!success) {
                    closeQuietly(os);
                }
            }
        }
        return _writer;
    }

    @Nonnull
    public RedirectToFileStreamListener<P> whichAppendsContent(boolean append) {
        _append = append;
        return thisObject();
    }

    @Nonnull
    public RedirectToFileStreamListener<P> whichAppendsContent() {
        return whichAppendsContent(true);
    }

    @Nonnull
    public RedirectToFileStreamListener<P> whichNotAppendContent() {
        return whichAppendsContent(false);
    }

    @Nonnull
    public RedirectToFileStreamListener<P> withMaximumNumberOfWriteTries(@Nonnegative int tries) {
        _strategy.withMaxNumberOfRetries(tries);
        return thisObject();
    }

    @Nonnull
    public File getFile() {
        return _file;
    }

    @Nonnegative
    public int getMaxNumberOfWriteTries() {
        return _strategy.getMaxNumberOfRetries();
    }

    @Override
    public boolean isClosed() {
        try {
            return super.isClosed();
        } finally {
            synchronized (this) {
                try {
                    closeQuietly(_writer);
                } finally {
                    _writer = null;
                }
            }
        }
    }

    public static class Provider extends StreamListenerProviderSupport {

        public Provider() {
            super(RedirectToFileStreamListener.class, "redirectToFile", "file");
        }

        @Nonnull
        @Override
        protected LineBasedStreamListenerSupport<?, ?> createInstanceBy(@Nonnull Class<? extends LineBasedStreamListenerSupport<?, ?>> type, @Nonnull Map<String, String> parameters) {
            return new RedirectToFileStreamListener(new File(parameters.get("file")));
        }

        @Override
        protected void configure(@Nonnull LineBasedStreamListenerSupport<?, ?> instance, @Nonnull Map<String, String> parameters) {
            super.configure(instance, parameters);
            configureCharset((RedirectToFileStreamListener<?>) instance, parameters);
            configureAppend((RedirectToFileStreamListener<?>) instance, parameters);
            configureMaxNumberOfWriteTries((RedirectToFileStreamListener<?>) instance, parameters);
        }

        protected void configureCharset(@Nonnull RedirectToFileStreamListener<?> instance, @Nonnull Map<String, String> parameters) {
            final String value = parameters.get("charset");
            if (value != null) {
                instance.withCharset(value);
            }
        }

        protected void configureAppend(@Nonnull RedirectToFileStreamListener<?> instance, @Nonnull Map<String, String> parameters) {
            instance.whichAppendsContent(getBooleanValue(parameters, "append", false));
        }

        protected void configureMaxNumberOfWriteTries(@Nonnull RedirectToFileStreamListener<?> instance, @Nonnull Map<String, String> parameters) {
            final String value = parameters.get("maxNumberOfWriteTries");
            if (value != null) {
                instance.withMaximumNumberOfWriteTries(parseInt(value.trim()));
            }
        }

    }

}

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

package org.echocat.jomon.process.daemon.listeners.stream;

import org.echocat.jomon.process.GeneratedProcess;
import org.echocat.jomon.process.daemon.StreamType;
import org.echocat.jomon.runtime.concurrent.RetryForSpecifiedCountStrategy;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.Callable;

import static java.lang.Integer.parseInt;
import static java.nio.charset.Charset.defaultCharset;
import static java.nio.charset.Charset.forName;
import static org.apache.commons.io.FileUtils.openOutputStream;
import static org.echocat.jomon.runtime.concurrent.Retryer.executeWithRetry;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

@ThreadSafe
public class RedirectToFileStreamListener extends StreamListenerSupport<RedirectToFileStreamListener> {

    @Nonnull
    public static RedirectToFileStreamListener redirectToFile(@Nonnull File file) {
        return new RedirectToFileStreamListener(file);
    }

    @Nonnull
    private final File _file;
    @Nonnull
    private final RetryForSpecifiedCountStrategy<Void> _strategy = RetryForSpecifiedCountStrategy.<Void>retryForSpecifiedCountOf(5).withExceptionsThatForceRetry(IOException.class);

    @Nonnull
    private Charset _charset = defaultCharset();
    private boolean _append;

    @Nullable
    private Writer _writer;

    public RedirectToFileStreamListener(@Nonnull File file) {
        _file = file;
    }

    @Override
    protected void write(@Nonnull GeneratedProcess process, @Nonnull final String content, @Nonnull StreamType streamType) {
        try {
            synchronized (this) {
                writeThrowingIOException(content);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not write message to file: " + _file, e);
        }
    }

    @GuardedBy("this")
    protected void writeThrowingIOException(@Nonnull final String content) throws IOException {
        executeWithRetry(new Callable<Void>() { @Override public Void call() throws IOException {
            final Writer writer = getWriter();
            try {
                writer.write(content);
            } catch (IOException e) {
                _writer = null;
                throw e;
            }
            try {
                writer.flush();
            } catch (IOException ignored) {}
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
                _writer = new OutputStreamWriter(os, _charset);
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
    public RedirectToFileStreamListener withCharset(@Nonnull Charset charset) {
        _charset = charset;
        return thisObject();
    }

    @Nonnull
    public RedirectToFileStreamListener withCharset(@Nonnull String charset) {
        return withCharset(forName(charset));
    }

    @Nonnull
    public RedirectToFileStreamListener whichAppendsContent(boolean append) {
        _append = append;
        return thisObject();
    }

    @Nonnull
    public RedirectToFileStreamListener whichAppendsContent() {
        return whichAppendsContent(true);
    }

    @Nonnull
    public RedirectToFileStreamListener whichNotAppendContent() {
        return whichAppendsContent(false);
    }

    @Nonnull
    public RedirectToFileStreamListener withMaximumNumberOfWriteTries(@Nonnegative int tries) {
        _strategy.withMaxNumberOfRetries(tries);
        return thisObject();
    }

    @Nonnull
    public Charset getCharset() {
        return _charset;
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

    public static class Provider extends StreamListenerProviderSupport<RedirectToFileStreamListener> {

        public Provider() {
            super(RedirectToFileStreamListener.class, "redirectToFile", "file");
        }

        @Nonnull
        @Override
        protected RedirectToFileStreamListener createInstanceBy(@Nonnull Class<RedirectToFileStreamListener> type, @Nonnull Map<String, String> parameters) {
            return new RedirectToFileStreamListener(new File(parameters.get("file")));
        }

        @Override
        protected void configure(@Nonnull RedirectToFileStreamListener instance, @Nonnull Map<String, String> parameters) {
            super.configure(instance, parameters);
            configureCharset(instance, parameters);
            configureAppend(instance, parameters);
            configureMaxNumberOfWriteTries(instance, parameters);
        }

        protected void configureCharset(@Nonnull RedirectToFileStreamListener instance, @Nonnull Map<String, String> parameters) {
            final String value = parameters.get("charset");
            if (value != null) {
                instance.withCharset(value);
            }
        }

        protected void configureAppend(@Nonnull RedirectToFileStreamListener instance, @Nonnull Map<String, String> parameters) {
            instance.whichAppendsContent(getBooleanValue(parameters, "append", false));
        }

        protected void configureMaxNumberOfWriteTries(@Nonnull RedirectToFileStreamListener instance, @Nonnull Map<String, String> parameters) {
            final String value = parameters.get("maxNumberOfWriteTries");
            if (value != null) {
                instance.withMaximumNumberOfWriteTries(parseInt(value.trim()));
            }
        }

    }

}

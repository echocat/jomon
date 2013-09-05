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
import org.echocat.jomon.process.daemon.listeners.support.MessageFormatter;
import org.echocat.jomon.runtime.CollectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.io.Closeable;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

import static org.echocat.jomon.process.ProcessUtils.toEscapedCommandLine;
import static org.echocat.jomon.process.daemon.StreamType.system;
import static org.echocat.jomon.process.daemon.listeners.support.DefaultMessageFormatter.messageFormatterFor;

@ThreadSafe
public abstract class StreamListenerSupport<T extends StreamListenerSupport<T>> implements StreamListener, Closeable {


    public static final String[] KEYS = new String[]{"timeStamp", "streamType", "message", "pid"};

    @Nonnull
    private MessageFormatter _formatter = messageFormatterFor("[{timeStamp,date,yyyy-MM-dd HH:mm:ss} {streamType}] {message}\n", KEYS);
    private boolean _recordProcessStarted;
    private boolean _recordProcessTerminated;

    private volatile boolean _closed;
    private boolean _started;
    private boolean _terminated;

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
    public T whichRecordsProcessStart(boolean record) {
        _recordProcessStarted = record;
        return thisObject();
    }

    @Nonnull
    public T whichRecordsProcessStart() {
        return whichRecordsProcessStart(true);
    }

    @Nonnull
    public T whichNotRecordsProcessStart() {
        return whichRecordsProcessStart(false);
    }

    @Nonnull
    public T whichRecordsProcessTermination(boolean record) {
        _recordProcessTerminated = record;
        return thisObject();
    }

    @Nonnull
    public T whichRecordsProcessTermination() {
        return whichRecordsProcessTermination(true);
    }

    @Nonnull
    public T whichNotRecordsProcessTermination() {
        return whichRecordsProcessTermination(false);
    }

    @Override
    public void notifyProcessStarted(@Nonnull GeneratedProcess process) {
        synchronized (this) {
            if (!_started) {
                if (_recordProcessStarted) {
                    formatAndWrite(process, "Process #" + process.getId() + " started: " + toEscapedCommandLine(process.getCommandLine()), system);
                }
                _started = true;
            }
        }
    }

    @Override
    public void notifyLineOutput(@Nonnull GeneratedProcess process, @Nonnull String line, @Nonnull StreamType streamType) {
        formatAndWrite(process, line, streamType);
    }

    @Override
    public void notifyProcessTerminated(@Nonnull GeneratedProcess process) {
        synchronized (this) {
            if (!_terminated) {
                if (_recordProcessTerminated) {
                    formatAndWrite(process, "Process #" + process.getId() + " ended with exitCode: " + process.exitValue(), system);
                }
                _terminated = true;
            }
        }
    }

    protected void formatAndWrite(@Nonnull GeneratedProcess process, @Nonnull String line, @Nonnull StreamType streamType) {
        if (_closed) {
            throw new IllegalStateException("Already closed.");
        }
        write(process, format(process, line, streamType), streamType);
    }

    protected abstract void write(@Nonnull GeneratedProcess process, @Nonnull String content, @Nonnull StreamType streamType);

    @Nonnull
    protected String format(@Nonnull GeneratedProcess process, @Nonnull String line, @Nonnull StreamType streamType) {
        return _formatter.format(CollectionUtils.<String, Object>asMap(
            "pid", process.getId(),
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

    @Override
    public void close() throws IOException {
        _closed = true;
    }

    @Nonnull
    public MessageFormatter getFormatter() {
        return _formatter;
    }

    public boolean isRecordProcessStarted() {
        return _recordProcessStarted;
    }

    public boolean isRecordProcessTerminated() {
        return _recordProcessTerminated;
    }

    public boolean isClosed() {
        return _closed;
    }

    @Nonnull
    protected T thisObject() {
        // noinspection unchecked
        return (T) this;
    }

}

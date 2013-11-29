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
import org.echocat.jomon.runtime.io.StreamType;
import org.echocat.jomon.runtime.logging.LogLevel;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static org.echocat.jomon.runtime.CollectionUtils.asImmutableMap;
import static org.echocat.jomon.runtime.io.StreamType.*;
import static org.echocat.jomon.runtime.logging.LogLevel.error;
import static org.echocat.jomon.runtime.logging.LogLevel.info;
import static org.echocat.jomon.runtime.logging.Slf4jUtils.log;
import static org.slf4j.LoggerFactory.getLogger;

public class RedirectToLoggerStreamListener<P extends GeneratedProcess<?, ?>> extends LineBasedAndStateEnabledStreamListenerSupport<P, RedirectToLoggerStreamListener<P>> {

    @Nonnull
    public static final Map<StreamType, LogLevel> DEFAULT_STREAM_TYPE_TO_LOG_LEVEL = asImmutableMap(
        stdout, info,
        stderr, error,
        system, info
    );

    @Nonnull
    private final Map<StreamType, LogLevel> _streamTypeToLogLevel = new HashMap<>(DEFAULT_STREAM_TYPE_TO_LOG_LEVEL);

    @Nonnull
    private final Logger _logger;

    public RedirectToLoggerStreamListener() {
        this((Logger) null);
    }

    public RedirectToLoggerStreamListener(@Nullable Class<?> clazz) {
        this(clazz != null ? getLogger(clazz) : null);
    }

    public RedirectToLoggerStreamListener(@Nullable String loggerName) {
        this(loggerName != null ? getLogger(loggerName) : null);
    }

    public RedirectToLoggerStreamListener(@Nullable Logger logger) {
        _logger = logger != null ? logger : getLogger(RedirectToLoggerStreamListener.class);
        whichFormatsMessagesWith("{message}");
    }

    @Nonnull
    public RedirectToLoggerStreamListener<P> whichLogsStreamTypeOn(@Nonnull StreamType streamType, @Nonnull LogLevel on) {
        _streamTypeToLogLevel.put(streamType, on);
        return this;
    }

    @Nonnull
    public LogLevel getLogLevelFor(@Nonnull StreamType streamType) {
        final LogLevel logLevel = _streamTypeToLogLevel.get(streamType);
        if (logLevel == null) {
            throw new IllegalArgumentException("Could not handle streamType: " + streamType);
        }
        return logLevel;
    }

    @Nonnull
    public Map<StreamType, LogLevel> getStreamTypeToLogLevel() {
        return asImmutableMap(_streamTypeToLogLevel);
    }

    @Override
    protected void write(@Nonnull P process, @Nonnull String content, @Nonnull StreamType streamType) {
        final String line = removeLastLineBreakFrom(content);
        final LogLevel logLevel = getLogLevelFor(streamType);
        log(_logger, logLevel, line);
    }

    @Nonnull
    protected String getDisplayFor(@Nonnull P reference) {
        final Object plain = reference.getId();
        return plain != null ? plain.toString() : "<unknown>";
    }

    public static class Provider extends StreamListenerProviderSupport {

        public Provider() {
            super(RedirectToLoggerStreamListener.class, "redirectToLogger");
        }

        @Nonnull
        @Override
        protected LineBasedStreamListenerSupport<?, ?> createInstanceBy(@Nonnull Class<? extends LineBasedStreamListenerSupport<?, ?>> type, @Nonnull Map<String, String> parameters) {
            return new RedirectToLoggerStreamListener();
        }

    }

}

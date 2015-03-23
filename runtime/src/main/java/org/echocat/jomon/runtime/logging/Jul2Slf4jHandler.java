/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012-2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.runtime.logging;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class Jul2Slf4jHandler extends Handler {

    private static final String FQCN = java.util.logging.Logger.class.getName();
    private static final String UNKNOWN_LOGGER_NAME = "unknown.jul.logger";

    private static final int TRACE_LEVEL_THRESHOLD = Level.FINEST.intValue();
    private static final int DEBUG_LEVEL_THRESHOLD = Level.FINE.intValue();
    private static final int INFO_LEVEL_THRESHOLD = Level.INFO.intValue();
    private static final int WARN_LEVEL_THRESHOLD = Level.WARNING.intValue();

    @Nullable
    private final ILoggerFactory _loggerFactory;

    public Jul2Slf4jHandler() {
        this(null);
    }

    public Jul2Slf4jHandler(@Nullable ILoggerFactory loggerFactory) {
        _loggerFactory = loggerFactory;
    }

    @Override
    public void close() {}

    @Override
    public void flush() {}

    @Nullable
    protected Logger getSLF4JLogger(@Nonnull LogRecord record) {
        final String name = record.getLoggerName();
        return logger(name != null ? name : UNKNOWN_LOGGER_NAME);
    }

    protected void callLocationAwareLogger(LocationAwareLogger lal, LogRecord record) {
        final int julLevelValue = record.getLevel().intValue();
        final int slf4jLevel;
        if (julLevelValue <= TRACE_LEVEL_THRESHOLD) {
            slf4jLevel = LocationAwareLogger.TRACE_INT;
        } else if (julLevelValue <= DEBUG_LEVEL_THRESHOLD) {
            slf4jLevel = LocationAwareLogger.DEBUG_INT;
        } else if (julLevelValue <= INFO_LEVEL_THRESHOLD) {
            slf4jLevel = LocationAwareLogger.INFO_INT;
        } else if (julLevelValue <= WARN_LEVEL_THRESHOLD) {
            slf4jLevel = LocationAwareLogger.WARN_INT;
        } else {
            slf4jLevel = LocationAwareLogger.ERROR_INT;
        }
        final String i18nMessage = getMessageI18N(record);
        lal.log(null, FQCN, slf4jLevel, i18nMessage, null, record.getThrown());
    }

    protected void callPlainSLF4JLogger(@Nonnull Logger slf4jLogger, LogRecord record) {
        final String i18nMessage = getMessageI18N(record);
        final int julLevelValue = record.getLevel().intValue();
        if (julLevelValue <= TRACE_LEVEL_THRESHOLD) {
            slf4jLogger.trace(i18nMessage, record.getThrown());
        } else if (julLevelValue <= DEBUG_LEVEL_THRESHOLD) {
            slf4jLogger.debug(i18nMessage, record.getThrown());
        } else if (julLevelValue <= INFO_LEVEL_THRESHOLD) {
            slf4jLogger.info(i18nMessage, record.getThrown());
        } else if (julLevelValue <= WARN_LEVEL_THRESHOLD) {
            slf4jLogger.warn(i18nMessage, record.getThrown());
        } else {
            slf4jLogger.error(i18nMessage, record.getThrown());
        }
    }

    @Nullable
    protected String getMessageI18N(@Nonnull LogRecord record) {
        String message = record.getMessage();
        if (message != null) {
            final ResourceBundle bundle = record.getResourceBundle();
            if (bundle != null) {
                try {
                    message = bundle.getString(message);
                } catch (final MissingResourceException ignored) {
                }
            }
            final Object[] params = record.getParameters();
            if (params != null && params.length > 0) {
                message = MessageFormat.format(message, params);
            }
        }
        return message;
    }

    @Override
    public void publish(@Nullable LogRecord record) {
        if (record != null) {
            final Logger slf4jLogger = getSLF4JLogger(record);
            if (slf4jLogger instanceof LocationAwareLogger) {
                callLocationAwareLogger((LocationAwareLogger) slf4jLogger, record);
            } else {
                callPlainSLF4JLogger(slf4jLogger, record);
            }
        }
    }

    @Nonnull
    protected Logger logger(@Nonnull String name) {
        return _loggerFactory != null ? _loggerFactory.getLogger(name) : LoggerFactory.getLogger(name);
    }
}

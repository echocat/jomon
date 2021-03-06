/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012-2014 echocat
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
import org.slf4j.Marker;
import org.slf4j.spi.LocationAwareLogger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Handler;
import java.util.logging.LogManager;

import static java.util.logging.LogManager.getLogManager;
import static org.echocat.jomon.runtime.CollectionUtils.asImmutableList;
import static org.echocat.jomon.runtime.logging.LogLevel.*;
import static org.echocat.jomon.runtime.logging.LogLevels.allLogLevels;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietlyIfAutoCloseable;
import static org.slf4j.spi.LocationAwareLogger.*;

public class Slf4jUtils {

    private static final String FQCN = Slf4jUtils.class.getName();
    private static final Map<LogLevel, LogLevel> NORMALIZED_LEVEL_CACHE = new ConcurrentHashMap<>();

    public static void log(@Nonnull LocationAwareLogger logger, @Nonnull LogLevel level, @Nullable String fqcn, @Nullable Marker marker, @Nullable String message, @Nullable Object[] argArray, @Nullable Throwable t) {
        if (trace.equals(level)) {
            logger.log(marker, fqcn, TRACE_INT, message, argArray, t);
        } else if (debug.equals(level)) {
            logger.log(marker, fqcn, DEBUG_INT, message, argArray, t);
        } else if (info.equals(level)) {
            logger.log(marker, fqcn, INFO_INT, message, argArray, t);
        } else if (warning.equals(level)) {
            logger.log(marker, fqcn, WARN_INT, message, argArray, t);
        } else if (error.equals(level) || fatal.equals(level)) {
            logger.log(marker, fqcn, ERROR_INT, message, argArray, t);
        } else {
            log(logger, normalize(level), fqcn, marker, message, argArray, t);
        }
    }


    /**
     * Is the logger instance enabled for the given <code>level</code>?
     *
     * @return <code>true</code> if this Logger is enabled for the given <code>level</code>, <code>false</code> otherwise.
     */
    public static boolean isEnabled(@Nonnull Logger logger, @Nonnull LogLevel level) {
        final boolean result;
        if (trace.equals(level)) {
            result = logger.isTraceEnabled();
        } else if (debug.equals(level)) {
            result = logger.isDebugEnabled();
        } else if (info.equals(level)) {
            result = logger.isInfoEnabled();
        } else if (warning.equals(level)) {
            result = logger.isWarnEnabled();
        } else if (error.equals(level) || fatal.equals(level)) {
            result = logger.isErrorEnabled();
        } else {
            result = isEnabled(logger, normalize(level));
        }
        return result;
    }


    /**
     * Log a message at the given <code>level</code>.
     *
     * @param msg the message string to be logged
     */
    public static void log(@Nonnull Logger logger, @Nonnull LogLevel level, @Nullable String msg) {
        if (logger instanceof LocationAwareLogger) {
            log((LocationAwareLogger) logger, level, FQCN, null, msg, null, null);
        } else if (trace.equals(level)) {
            logger.trace(msg);
        } else if (debug.equals(level)) {
            logger.debug(msg);
        } else if (info.equals(level)) {
            logger.info(msg);
        } else if (warning.equals(level)) {
            logger.warn(msg);
        } else if (error.equals(level) || fatal.equals(level)) {
            logger.error(msg);
        } else {
            log(logger, normalize(level), msg);
        }
    }


    /**
     * <p>Log a message at the given <code>level</code> according to the specified format and argument.</p>
     *
     * <p>This form avoids superfluous object creation when the logger is disabled for the given <code>level</code>.</p>
     *
     * @param format the format string
     * @param arg the argument
     */
    public static void log(@Nonnull Logger logger, @Nonnull LogLevel level, @Nonnull String format, @Nullable Object arg) {
        if (logger instanceof LocationAwareLogger) {
            log((LocationAwareLogger) logger, level, FQCN, null, format, new Object[]{arg}, null);
        } else if (trace.equals(level)) {
            logger.trace(format, arg);
        } else if (debug.equals(level)) {
            logger.debug(format, arg);
        } else if (info.equals(level)) {
            logger.info(format, arg);
        } else if (warning.equals(level)) {
            logger.warn(format, arg);
        } else if (error.equals(level) || fatal.equals(level)) {
            logger.error(format, arg);
        } else {
            log(logger, normalize(level), format, arg);
        }
    }


    /**
     * <p>Log a message at the given <code>level</code> according to the specified format and arguments.</p>
     *
     * <p>This form avoids superfluous object creation when the logger is disabled for the given <code>level</code>.</p>
     *
     * @param format the format string
     * @param arg1 the first argument
     * @param arg2 the second argument
     */
    public static void log(@Nonnull Logger logger, @Nonnull LogLevel level, @Nonnull String format, @Nullable Object arg1, @Nullable Object arg2) {
        if (logger instanceof LocationAwareLogger) {
            log((LocationAwareLogger) logger, level, FQCN, null, format, new Object[]{arg1, arg2}, null);
        } else if (trace.equals(level)) {
            logger.trace(format, arg1, arg2);
        } else if (debug.equals(level)) {
            logger.debug(format, arg1, arg2);
        } else if (info.equals(level)) {
            logger.info(format, arg1, arg2);
        } else if (warning.equals(level)) {
            logger.warn(format, arg1, arg2);
        } else if (error.equals(level) || fatal.equals(level)) {
            logger.error(format, arg1, arg2);
        } else {
            log(logger, normalize(level), format, arg1, arg2);
        }
    }

    /**
     * <p>Log a message at the given <code>level</code> according to the specified format and arguments.</p>
     *
     * <p>This form avoids superfluous object creation when the logger is disabled for the given <code>level</code>.</p>
     *
     * @param format the format string
     * @param arguments a list of 3 or more arguments
     */
    public static void log(@Nonnull Logger logger, @Nonnull LogLevel level, @Nonnull String format, @Nullable Object... arguments) {
        if (logger instanceof LocationAwareLogger) {
            log((LocationAwareLogger) logger, level, FQCN, null, format, arguments, null);
        } else if (trace.equals(level)) {
            logger.trace(format, arguments);
        } else if (debug.equals(level)) {
            logger.debug(format, arguments);
        } else if (info.equals(level)) {
            logger.info(format, arguments);
        } else if (warning.equals(level)) {
            logger.warn(format, arguments);
        } else if (error.equals(level) || fatal.equals(level)) {
            logger.error(format, arguments);
        } else {
            log(logger, normalize(level), format, arguments);
        }
    }

    /**
     * Log an exception (throwable) at the given <code>level</code> with an accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param t the exception (throwable) to log
     */
    public static void log(@Nonnull Logger logger, @Nonnull LogLevel level, @Nullable String msg, @Nullable Throwable t) {
        if (logger instanceof LocationAwareLogger) {
            log((LocationAwareLogger) logger, level, FQCN, null, msg, null, t);
        } else if (trace.equals(level)) {
            logger.trace(msg, t);
        } else if (debug.equals(level)) {
            logger.debug(msg, t);
        } else if (info.equals(level)) {
            logger.info(msg, t);
        } else if (warning.equals(level)) {
            logger.warn(msg, t);
        } else if (error.equals(level) || fatal.equals(level)) {
            logger.error(msg, t);
        } else {
            log(logger, normalize(level), msg, t);
        }
    }


    /**
     * Similar to {@link #isEnabled} method except that the marker data is also taken into account.
     *
     * @param marker The marker data to take into consideration
     * @return <code>true</code> if this Logger is enabled for the given <code>level</code>, <code>false</code> otherwise.
     */
    public static boolean isEnabled(@Nonnull Logger logger, @Nonnull LogLevel level, @Nonnull Marker marker) {
        final boolean result;
        if (trace.equals(level)) {
            result = logger.isTraceEnabled(marker);
        } else if (debug.equals(level)) {
            result = logger.isDebugEnabled(marker);
        } else if (info.equals(level)) {
            result = logger.isInfoEnabled(marker);
        } else if (warning.equals(level)) {
            result = logger.isWarnEnabled(marker);
        } else if (error.equals(level) || fatal.equals(level)) {
            result = logger.isErrorEnabled(marker);
        } else {
            result = isEnabled(logger, normalize(level), marker);
        }
        return result;
    }

    /**
     * Log a message with the specific Marker at the given <code>level</code>.
     *
     * @param marker the marker data specific to this log statement
     * @param msg the message string to be logged
     */
    public static void log(@Nonnull Logger logger, @Nonnull LogLevel level, @Nonnull Marker marker, @Nullable String msg) {
        if (logger instanceof LocationAwareLogger) {
            log((LocationAwareLogger) logger, level, FQCN, marker, msg, null, null);
        } else if (trace.equals(level)) {
            logger.trace(marker, msg);
        } else if (debug.equals(level)) {
            logger.debug(marker, msg);
        } else if (info.equals(level)) {
            logger.info(marker, msg);
        } else if (warning.equals(level)) {
            logger.warn(marker, msg);
        } else if (error.equals(level) || fatal.equals(level)) {
            logger.error(marker, msg);
        } else {
            log(logger, normalize(level), marker, msg);
        }
    }

    /**
     * This method is similar to {@link #log(Logger, LogLevel, String, Object)} method except that the marker data is also taken into consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg the argument
     */
    public static void log(@Nonnull Logger logger, @Nonnull LogLevel level, @Nonnull Marker marker, @Nonnull String format, @Nullable Object arg) {
        if (logger instanceof LocationAwareLogger) {
            log((LocationAwareLogger) logger, level, FQCN, marker, format, new Object[]{arg}, null);
        } else if (trace.equals(level)) {
            logger.trace(marker, format, arg);
        } else if (debug.equals(level)) {
            logger.debug(marker, format, arg);
        } else if (info.equals(level)) {
            logger.info(marker, format, arg);
        } else if (warning.equals(level)) {
            logger.warn(marker, format, arg);
        } else if (error.equals(level) || fatal.equals(level)) {
            logger.error(marker, format, arg);
        } else {
            log(logger, normalize(level), marker, format, arg);
        }
    }


    /**
     * This method is similar to {@link #log(Logger, LogLevel, String, Object, Object)} method except that the marker data is also taken into consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg1 the first argument
     * @param arg2 the second argument
     */
    public static void log(@Nonnull Logger logger, @Nonnull LogLevel level, @Nonnull Marker marker, @Nonnull String format, @Nullable Object arg1, @Nullable Object arg2) {
        if (logger instanceof LocationAwareLogger) {
            log((LocationAwareLogger) logger, level, FQCN, marker, format, new Object[]{arg1, arg2}, null);
        } else if (trace.equals(level)) {
            logger.trace(marker, format, arg1, arg2);
        } else if (debug.equals(level)) {
            logger.debug(marker, format, arg1, arg2);
        } else if (info.equals(level)) {
            logger.info(marker, format, arg1, arg2);
        } else if (warning.equals(level)) {
            logger.warn(marker, format, arg1, arg2);
        } else if (error.equals(level) || fatal.equals(level)) {
            logger.error(marker, format, arg1, arg2);
        } else {
            log(logger, normalize(level), marker, format, arg1, arg2);
        }
    }

    /**
     * This method is similar to {@link #log(Logger, LogLevel, String, Object...)} method except that the marker data is also taken into consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arguments an array of arguments
     */
    public static void log(@Nonnull Logger logger, @Nonnull LogLevel level, @Nonnull Marker marker, @Nonnull String format, @Nullable Object... arguments) {
        if (logger instanceof LocationAwareLogger) {
            log((LocationAwareLogger) logger, level, FQCN, marker, format, arguments, null);
        } else if (trace.equals(level)) {
            logger.trace(marker, format, arguments);
        } else if (debug.equals(level)) {
            logger.debug(marker, format, arguments);
        } else if (info.equals(level)) {
            logger.info(marker, format, arguments);
        } else if (warning.equals(level)) {
            logger.warn(marker, format, arguments);
        } else if (error.equals(level) || fatal.equals(level)) {
            logger.error(marker, format, arguments);
        } else {
            log(logger, normalize(level), marker, format, arguments);
        }
    }


    /**
     * This method is similar to {@link #log(Logger, LogLevel, String, Throwable)} method except that the marker data is also taken into consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param msg the message accompanying the exception
     * @param t the exception (throwable) to log
     */
    public static void log(@Nonnull Logger logger, @Nonnull LogLevel level, @Nonnull Marker marker, @Nullable String msg, @Nullable Throwable t) {
        if (logger instanceof LocationAwareLogger) {
            log((LocationAwareLogger) logger, level, FQCN, marker, msg, null, t);
        } else if (trace.equals(level)) {
            logger.trace(marker, msg, t);
        } else if (debug.equals(level)) {
            logger.debug(marker, msg, t);
        } else if (info.equals(level)) {
            logger.info(marker, msg, t);
        } else if (warning.equals(level)) {
            logger.warn(marker, msg, t);
        } else if (error.equals(level) || fatal.equals(level)) {
            logger.error(marker, msg, t);
        } else {
            log(logger, normalize(level), marker, msg, t);
        }
    }

    @Nonnull
    protected static LogLevel normalize(@Nonnull LogLevel level) {
        LogLevel result = NORMALIZED_LEVEL_CACHE.get(level);
        if (result == null) {
            result = debug;
            int lastDifference = differenceOf(debug, level);
            for (final LogLevel logLevel : allLogLevels()) {
                final int difference = differenceOf(logLevel, level);
                if (difference < lastDifference) {
                    result = logLevel;
                    lastDifference = difference;
                }
            }
            NORMALIZED_LEVEL_CACHE.put(level, result);
        }
        return result;
    }

    @Nonnegative
    protected static int differenceOf(@Nonnull LogLevel a, @Nonnull LogLevel b) {
        final int pa = a.getPriority();
        final int pb = b.getPriority();
        final int result;
        if (pa == pb) {
            result = 0;
        } else if (pa > pb) {
            result = pa - pb;
        } else {
            result = pb - pa;
        }
        return result;
    }

    private Slf4jUtils() {}

    @Nonnull
    public static Installation tryInstallSlf4jBridges(@Nullable ILoggerFactory loggerFactory) {
        return new CombinedInstallation(
            tryInstallClToSlf4jBridge(loggerFactory),
            tryInstallJulToSlf4jBridge(loggerFactory)
        );
    }

    @Nonnull
    public static Installation tryInstallClToSlf4jBridge(@Nullable ILoggerFactory loggerFactory) {
        Installation result = new NoopInstallation();
        try {
            final Class<?> type = Slf4jUtils.class.getClassLoader().loadClass("org.apache.commons.logging.LogFactory");
            final Field field = type.getDeclaredField("factories");
            field.setAccessible(true);
            if (Map.class.isAssignableFrom(field.getType())) {
                // noinspection unchecked
                final Map<ClassLoader, Object> factories = (Map<ClassLoader, Object>) field.get(null);
                if (factories != null) {
                    // noinspection SynchronizationOnLocalVariableOrMethodParameter
                    synchronized (factories) {
                        final ClassLoader classLoader = classLoader();
                        factories.put(classLoader, createClToSlf4jLoggerFactoryFor(loggerFactory));
                        result = new Cl2Slf4jInstallation(factories, classLoader);
                    }
                }
            }
        } catch (ClassNotFoundException | InstantiationException  | IllegalAccessException  |InvocationTargetException | NoSuchMethodException | NoSuchFieldException ignored) {}
        return result;
    }

    @Nonnull
    private static Object createClToSlf4jLoggerFactoryFor(@Nullable ILoggerFactory loggerFactory) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        try {
            final Class<?> type = classLoader().loadClass("org.echocat.jomon.runtime.logging.Cl2Slf4jLoggerFactory");
            return type.getConstructor(ILoggerFactory.class).newInstance(loggerFactory);
        } catch (ClassNotFoundException | InstantiationException  | IllegalAccessException  |InvocationTargetException | NoSuchMethodException e) {
            // noinspection UseOfSystemOutOrSystemErr
            final PrintStream stream = System.err;
            stream.print("WARN Could not initiate instance of org.echocat.jomon.runtime.logging.Cl2Slf4jLoggerFactory.");
            e.printStackTrace(stream);
            throw e;
        }
    }

    @Nonnull
    protected static ClassLoader classLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    @Nonnull
    public static Installation tryInstallJulToSlf4jBridge() {
        return tryInstallJulToSlf4jBridge(null);
    }

    @Nonnull
    public static Installation tryInstallJulToSlf4jBridge(@Nullable ILoggerFactory loggerFactory) {
        return tryInstallJulToSlf4jBridge(loggerFactory, null);
    }

    @Nonnull
    public static Installation tryInstallJulToSlf4jBridge(@Nullable ILoggerFactory loggerFactory, @Nullable LogManager logManager) {
        final Jul2Slf4jHandler newHandler = new Jul2Slf4jHandler(loggerFactory);
        final LogManager manager = logManager != null ? logManager : getLogManager();
        manager.reset();
        final java.util.logging.Logger logger = manager.getLogger("");
        final List<Handler> originalHandlers = new ArrayList<>();
        for (final Handler oldHandlers : logger.getHandlers()) {
            logger.removeHandler(oldHandlers);
            originalHandlers.add(newHandler);
        }
        logger.addHandler(newHandler);
        return new Jul2Slf4jInstallation(originalHandlers, newHandler, manager);
    }

    public static void tryFixMdcInSlf4j() {
        try {
            final ClassLoader classLoader = Log4JUtils.class.getClassLoader();
            final Class<?> mdc = classLoader.loadClass("org.slf4j.MDC");
            final Class<?> mdcAdapter = classLoader.loadClass("org.slf4j.spi.MDCAdapter");
            final Field mdcAdapterField = mdc.getDeclaredField("mdcAdapter");
            if (mdcAdapterField.getType().equals(mdcAdapter)) {
                mdcAdapterField.setAccessible(true);
                final Object delegate = mdcAdapterField.get(null);
                final Object fixed = classLoader.loadClass("org.echocat.jomon.runtime.logging.FixingSlf4jMDCAdapter").getConstructor(mdcAdapter).newInstance(delegate);
                mdcAdapterField.set(null, fixed);
            }
        } catch (final Exception ignored) {}
    }

    public static interface Installation extends AutoCloseable {}

    private static class NoopInstallation implements Installation {

        @Override
        public void close() throws Exception {}

    }

    private static class CombinedInstallation implements Installation {

        @Nonnull
        private final Iterable<Installation> _installations;

        private CombinedInstallation(@Nullable Installation... installations) {
            this(asImmutableList(installations));
        }

        private CombinedInstallation(@Nonnull Iterable<Installation> installations) {
            _installations = installations;
        }

        @Override
        public void close() throws Exception {
            closeQuietlyIfAutoCloseable(_installations);
        }

    }

    private static class Cl2Slf4jInstallation implements Installation {

        @Nonnull
        private final Map<ClassLoader, Object> _factories;
        @Nonnull
        private final ClassLoader _classLoader;

        private Cl2Slf4jInstallation(@Nonnull Map<ClassLoader, Object> factories, @Nonnull ClassLoader classLoader) {
            _factories = factories;
            _classLoader = classLoader;
        }

        @Override
        public void close() throws Exception {
            synchronized (_factories) {
                _factories.remove(_classLoader);
            }
        }
    }

    private static class Jul2Slf4jInstallation implements Installation {

        @Nonnull
        private final Iterable<Handler> _originalHandlers;
        @Nonnull
        private final Handler _installedHandler;
        @Nonnull
        private final LogManager _logManager;

        private Jul2Slf4jInstallation(@Nonnull Iterable<Handler> originalHandlers, @Nonnull Handler installedHandler, @Nonnull LogManager logManager) {
            _originalHandlers = originalHandlers;
            _installedHandler = installedHandler;
            _logManager = logManager;
        }

        @Override
        public void close() throws Exception {
            final java.util.logging.Logger root = _logManager.getLogger("");
            _logManager.reset();
            root.removeHandler(_installedHandler);
            for (final Handler originalHandler : _originalHandlers) {
                root.addHandler(originalHandler);
            }
        }
    }

}

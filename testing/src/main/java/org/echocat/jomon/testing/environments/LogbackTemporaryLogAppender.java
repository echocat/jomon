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

package org.echocat.jomon.testing.environments;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.rules.ExternalResource;
import org.echocat.jomon.runtime.logging.LoggingEnvironment;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;

/**
 * <p>A JUnit {@link org.junit.rules.TestRule} to create a temporary log appender and it to {@link Logger} for the given class.
 * The appender will record all log messages. </p>
 *
 * <p>In order to run it requires an initialized {@link LoggingEnvironment} instance as part of the constructor. The {@link LoggingEnvironment} must be
 * initialized before creating the {@link LogbackTemporaryLogAppender}. This can be achieved by using an {@link org.junit.rules.RuleChain} (see sample below).</p>
 *
 * <p><b>Note</b>: Only logback is supported at the moment as logging framework.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * public class FooTest {
 *    public LoggingEnvironment _loggingEnvironment = new LoggingEnvironment(LoggingEnvironment.Type.logback);
 *    public LogbackTemporaryLogAppender _temporaryLogAppender = new LogbackTemporaryLogAppender(_loggingEnvironment, FooTest.class);
 *    &#064;Rule
 *    public TestRule _chain = RuleChain.outerRule(_loggingEnvironment).around(_temporaryLogAppender);
 *
 *    &#064;Test
 *    public void test() {
 *        LoggerFactory.getLogger(getClass()).info("Expected log message");
 *        assertThat(_temporaryLogAppender.toString(), contains("Expected log message"));
 *    }
 * }
 * }</pre>
 */
public class LogbackTemporaryLogAppender extends ExternalResource {

    @Nonnull
    private final LoggingEnvironment _loggingEnvironment;
    @Nonnull
    private final String _forLogger;
    @Nonnull
    private final ListAppender<ILoggingEvent> _temporaryAppender = new ListAppender<>();

    public LogbackTemporaryLogAppender(@Nonnull LoggingEnvironment loggingEnvironment, @Nonnull Class<?> forLogger) {
        this(loggingEnvironment, forLogger.getName());
    }

    public LogbackTemporaryLogAppender(@Nonnull LoggingEnvironment loggingEnvironment, @Nonnull String forLogger) {
        _loggingEnvironment = loggingEnvironment;
        _forLogger = forLogger;
        final org.slf4j.Logger loggerInstance = getSlf4jLogger();
        if (!Logger.class.isInstance(loggerInstance)) {
            throw new UnsupportedOperationException("Logger instance '" + loggerInstance + "' not supported at the moment. Only instances of " + Logger.class + " are supported, yet.");
        }
    }

    @Override
    protected void before() throws Throwable {
        _temporaryAppender.start();
        getLogbackLogger().addAppender(_temporaryAppender);
    }

    @Override
    protected void after() {
        _temporaryAppender.stop();
        try {
            getLogbackLogger().detachAppender(_temporaryAppender);
        } catch (final RuntimeException ignored) {}
    }

    @Nonnull
    private Logger getLogbackLogger() {
        return (Logger) getSlf4jLogger();
    }

    @Nonnull
    private org.slf4j.Logger getSlf4jLogger() {return _loggingEnvironment.getLogger(_forLogger);}

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        final Collection<ILoggingEvent> loggingEvents = new ArrayList<>(_temporaryAppender.list);
        for (final ILoggingEvent loggingEvent : loggingEvents) {
            sb.append(loggingEvent).append("\n");
        }
        return sb.toString();
    }
}
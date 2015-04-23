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


import org.echocat.jomon.testing.environments.LoggingEnvironment.Type;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import static org.echocat.jomon.testing.Assert.assertThat;
import static org.echocat.jomon.testing.BaseMatchers.isEqualTo;
import static org.echocat.jomon.testing.StringMatchers.contains;
import static org.echocat.jomon.testing.StringMatchers.startsWith;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class LogbackTemporaryLogAppenderUnitTest {

    private static final String LOGGER_NAME = LogbackTemporaryLogAppenderUnitTest.class.getName();

    private final LoggingEnvironment _loggingEnvironment = new LoggingEnvironment(Type.logback);
    private final LogbackTemporaryLogAppender _temporaryLogAppender = new LogbackTemporaryLogAppender(_loggingEnvironment, getClass());

    @Rule
    public final TestRule _testRule = RuleChain.outerRule(_loggingEnvironment).around(_temporaryLogAppender);

    @Test(expected = UnsupportedOperationException.class)
    public void nonLogbackLoggerInstanceIsNotSupported() throws Throwable {
        final LoggingEnvironment mockedLoggingEnvironment = mock(LoggingEnvironment.class);
        //noinspection UnnecessaryFullyQualifiedName
        final org.slf4j.Logger mockedLogger = mock(org.slf4j.Logger.class);
        doReturn(mockedLogger).when(mockedLoggingEnvironment).getLogger(LOGGER_NAME);
        final LogbackTemporaryLogAppender logAppender = new LogbackTemporaryLogAppender(mockedLoggingEnvironment, LoggingEnvironment.class.getName());
        logAppender.before();
    }

    @Test
    public void logMessageIsReturnedInToString() throws Throwable {
        _loggingEnvironment.getLogger(LOGGER_NAME).info("Test message");
        assertThat(_temporaryLogAppender.toString(), isEqualTo("[INFO] Test message\n"));
    }

    @Test
    public void onceAfterMethodWasInvokedNoMoreMessagesWillBeLogged() throws Throwable {
        _loggingEnvironment.getLogger(LOGGER_NAME).info("This invocation is expected");
        _temporaryLogAppender.after();
        _loggingEnvironment.getLogger(LOGGER_NAME).error("UNEXPECTED");
        assertThat(_temporaryLogAppender.toString(), isEqualTo("[INFO] This invocation is expected\n"));
    }

    @Test
    public void logMessageWithExceptionWithCause() throws Throwable {
        final UnsupportedOperationException causingException = new UnsupportedOperationException("I caused a crash and I liked it!");
        final RuntimeException exception = new RuntimeException("MyMessage", causingException);
        _loggingEnvironment.getLogger(LOGGER_NAME).error("Test message", exception);
        final String logMessages = _temporaryLogAppender.toString();
        final StringBuilder sb = new StringBuilder();
        final OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                sb.append((char) b);
            }
        };
        try (final PrintStream s = new PrintStream(out)) {
            exception.printStackTrace(s);
        }
        assertThat(logMessages, startsWith("[ERROR] Test message\n" + sb.toString()));
        assertThat(logMessages, contains(exception.getClass().getName() + ": " + exception.getMessage()));
        assertThat(logMessages, contains("Caused by: " + causingException.getClass().getName() + ": " + causingException.getMessage()));
    }
}
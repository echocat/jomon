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

import static org.echocat.jomon.testing.Assert.assertThat;
import static org.echocat.jomon.testing.BaseMatchers.isEqualTo;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class LogbackTemporaryLogAppenderUnitTest {

    private static final String LOGGER_NAME = LogbackTemporaryLogAppenderUnitTest.class.getName();

    @Rule
    public LoggingEnvironment _loggingEnvironment = new LoggingEnvironment(Type.logback);

    @Test(expected = UnsupportedOperationException.class)
    public void nonLogbackLoggerInstanceIsNotSupported() {
        final LoggingEnvironment mockedLoggingEnvironment = mock(LoggingEnvironment.class);
        //noinspection UnnecessaryFullyQualifiedName
        final org.slf4j.Logger mockedLogger = mock(org.slf4j.Logger.class);
        doReturn(mockedLogger).when(mockedLoggingEnvironment).getLogger(LOGGER_NAME);
        new LogbackTemporaryLogAppender(mockedLoggingEnvironment, LoggingEnvironment.class);
    }

    @Test
    public void logMessageIsReturnedInToString() throws Throwable {
        final LogbackTemporaryLogAppender logAppender = new LogbackTemporaryLogAppender(_loggingEnvironment, getClass());
        logAppender.before();
        _loggingEnvironment.getLogger(LOGGER_NAME).info("Test message");
        assertThat(logAppender.toString(), isEqualTo("[INFO] Test message\n"));
    }

    @Test
    public void onceAfterMethodWasInvokedNoMoreMessagesWillBeLogged() throws Throwable {
        final LogbackTemporaryLogAppender logAppender = new LogbackTemporaryLogAppender(_loggingEnvironment, getClass());
        logAppender.before();
        _loggingEnvironment.getLogger(LOGGER_NAME).info("This invocation is expected");
        logAppender.after();
        _loggingEnvironment.getLogger(LOGGER_NAME).error("UNEXPECTED");
        assertThat(logAppender.toString(), isEqualTo("[INFO] This invocation is expected\n"));
    }
}
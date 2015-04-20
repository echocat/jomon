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

import static org.echocat.jomon.testing.Assert.assertThat;
import static org.echocat.jomon.testing.BaseMatchers.isEqualTo;
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
}
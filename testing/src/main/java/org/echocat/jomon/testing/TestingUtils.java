/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.testing;

import org.echocat.jomon.runtime.concurrent.RetryForSpecifiedTimeStrategy;
import org.echocat.jomon.runtime.util.Duration;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;

import static org.echocat.jomon.runtime.concurrent.Retryer.executeWithRetry;

public class TestingUtils {

    @Nullable
    public static void waitFor(@Nonnull Runnable what, @Nonnull String maxWaitTime) throws AssertionError {
        waitFor(what, new Duration(maxWaitTime));
    }

    @Nullable
    public static void waitFor(@Nonnull Runnable what, @Nonnegative long maxWaitTime) throws AssertionError {
        waitFor(what, new Duration(maxWaitTime));
    }

    @Nullable
    public static void waitFor(@Nonnull Runnable what, @Nonnull Duration maxWaitTime) throws AssertionError {
        executeWithRetry(what, RetryForSpecifiedTimeStrategy.<Void>retryForSpecifiedTimeOf(maxWaitTime).withExceptionsThatForceRetry(AssertionError.class));
    }

    @Nullable
    public static <T> T waitFor(@Nonnull Callable<T> what, @Nonnull String maxWaitTime) throws AssertionError {
        return waitFor(what, new Duration(maxWaitTime));
    }

    @Nullable
    public static <T> T waitFor(@Nonnull Callable<T> what, @Nonnegative long maxWaitTime) throws AssertionError {
        return waitFor(what, new Duration(maxWaitTime));
    }

    @Nullable
    public static <T> T waitFor(@Nonnull Callable<T> what, @Nonnull Duration maxWaitTime) throws AssertionError {
        return executeWithRetry(what, RetryForSpecifiedTimeStrategy.<T>retryForSpecifiedTimeOf(maxWaitTime).withExceptionsThatForceRetry(AssertionError.class));
    }

    private TestingUtils() {}

}

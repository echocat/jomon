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

package org.echocat.jomon.runtime.concurrent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;

public class Retryer {

    @Nullable
    public static void executeWithRetry(@Nonnull Runnable what, @Nonnull RetryingStrategy<Void> with) {
        executeWithRetry(what, with, RuntimeException.class);
    }

    @Nullable
    public static <E extends Throwable> void executeWithRetry(@Nonnull Runnable what, @Nonnull RetryingStrategy<Void> with, @Nonnull Class<E> throwable) throws E {
        executeWithRetry(toCallable(what), with, throwable);
    }

    @Nullable
    public static <T> T executeWithRetry(@Nonnull Callable<T> what, @Nonnull RetryingStrategy<T> with) {
        return executeWithRetry(what, with, RuntimeException.class);
    }

    @Nullable
    public static <T, E extends Throwable> T executeWithRetry(@Nonnull Callable<T> what, @Nonnull RetryingStrategy<T> with, @Nonnull Class<E> throwable) throws E {
        final StopWatch stopWatch = new StopWatch();
        long currentTry = 1;
        boolean retry;
        T result = null;
        do {
            with.beforeTry(new RetryingStatus(currentTry, stopWatch.getCurrentDuration()));
            try {
                result = what.call();
                retry = with.isRetryRequiredForResult(result, new RetryingStatus(currentTry, stopWatch.getCurrentDuration()));
            } catch (Throwable e) {
                if (with.isRetryRequiredForException(e, new RetryingStatus(currentTry, stopWatch.getCurrentDuration()))) {
                    retry = true;
                } else if (throwable.isInstance(e)) {
                    throw throwable.cast(e);
                } else if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else if (e instanceof Error) {
                    throw (Error) e;
                } else {
                    throw new RuntimeException("Could not execute " + what + " with " + with + ".", e);
                }
            }
            currentTry++;
        } while (retry);
        return result;
    }

    @Nonnull
    protected static Callable<Void> toCallable(@Nonnull final Runnable what) {
        return new Callable<Void>() { @Override public Void call() throws Exception {
            what.run();
            return null;
        }};
    }

    private Retryer() {}

}

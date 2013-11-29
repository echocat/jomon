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

import org.echocat.jomon.runtime.util.Duration;
import org.echocat.jomon.runtime.util.GotInterruptedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietlyIfAutoCloseable;

public class ThreadUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ThreadUtils.class);

    public static void stop(@Nullable Thread thread) {
        if (thread != null) {
            closeQuietlyIfAutoCloseable(thread);
            thread.interrupt();
            try {
                while (thread.isAlive()) {
                    thread.join(SECONDS.toMillis(10));
                    if (thread.isAlive()) {
                        final Throwable throwable = new Throwable();
                        throwable.setStackTrace(thread.getStackTrace());
                        LOG.info("Still wait for termination of '" + thread.getName() + "'...", throwable);
                        thread.interrupt();
                    }
                }
            } catch (final InterruptedException ignored) {
                currentThread().interrupt();
                LOG.debug("Could not wait for termination of '" + thread.getName() + "' - but this thread was interrupted.");
            }
        }
    }

    public static void stop(@Nullable Iterable<? extends Thread> threads) {
        if (threads != null) {
            for (final Thread thread : threads) {
                stop(thread);
            }
        }
    }

    public static void stop(@Nullable Thread... threads) {
        if (threads != null) {
            for (final Thread thread : threads) {
                stop(thread);
            }
        }
    }

    public static void join(@Nullable Duration maximumWaitTime, @Nullable Thread thread) throws InterruptedException {
        if (thread != null) {
            if (maximumWaitTime != null) {
                thread.join(maximumWaitTime.in(MILLISECONDS));
            } else {
                thread.join();
            }
        }
    }

    public static void join(@Nullable Duration maximumWaitTime, @Nullable Iterable<? extends Thread> threads) throws InterruptedException {
        if (threads != null) {
            for (final Thread thread : threads) {
                join(maximumWaitTime, thread);
            }
        }
    }

    public static void join(@Nullable Duration maximumWaitTime, @Nullable Thread... threads) throws InterruptedException {
        if (threads != null) {
            for (final Thread thread : threads) {
                join(maximumWaitTime, thread);
            }
        }
    }

    public static void join( @Nullable Thread thread) throws InterruptedException {
        join(null, thread);
    }

    public static void join(@Nullable Iterable<? extends Thread> threads) throws InterruptedException {
        join(null, threads);
    }

    public static void join(@Nullable Thread... threads) throws InterruptedException {
        join(null, threads);
    }

    public static void joinSafe(@Nullable Duration maximumWaitTime, @Nullable Thread thread) throws GotInterruptedException {
        try {
            join(maximumWaitTime, thread);
        } catch (final InterruptedException e) {
            currentThread().interrupt();
            throw new GotInterruptedException("Got interrupted while waiting for thread " + thread + ".", e);
        }
    }

    public static void joinSafe(@Nullable Duration maximumWaitTime, @Nullable Iterable<? extends Thread> threads) throws GotInterruptedException {
        if (threads != null) {
            for (final Thread thread : threads) {
                joinSafe(maximumWaitTime, thread);
            }
        }
    }

    public static void joinSafe(@Nullable Duration maximumWaitTime, @Nullable Thread... threads) throws GotInterruptedException {
        if (threads != null) {
            for (final Thread thread : threads) {
                joinSafe(maximumWaitTime, thread);
            }
        }
    }

    public static void joinSafe( @Nullable Thread thread) throws GotInterruptedException {
        joinSafe(null, thread);
    }

    public static void joinSafe(@Nullable Iterable<? extends Thread> threads) throws GotInterruptedException {
        joinSafe(null, threads);
    }

    public static void joinSafe(@Nullable Thread... threads) throws GotInterruptedException {
        joinSafe(null, threads);
    }

    private ThreadUtils() {}
}

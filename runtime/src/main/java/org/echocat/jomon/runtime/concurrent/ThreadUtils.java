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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietlyIfAutoCloseable;

public class ThreadUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ThreadUtils.class);

    public static void stop(@Nullable Iterable<? extends Thread> threads) {
        if (threads != null) {
            for (Thread thread : threads) {
                stop(thread);
            }
        }
    }

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
            } catch (InterruptedException ignored) {
                currentThread().interrupt();
                LOG.debug("Could not wait for termination of '" + thread.getName() + "' - but this thread was interrupted.");
            }
        }
    }

    private ThreadUtils() {}
}

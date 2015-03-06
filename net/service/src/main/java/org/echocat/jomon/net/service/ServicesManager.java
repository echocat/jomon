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

package org.echocat.jomon.net.service;

import org.echocat.jomon.runtime.util.Duration;
import org.echocat.jomon.runtime.util.ServiceTemporaryUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Collection;
import java.util.Collections;

import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

@ThreadSafe
public abstract class ServicesManager<I, O> implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(ServicesManager.class);

    public static final Duration DEFAULT_CHECK_INTERVAL = new Duration("10s");

    private final Thread _checkThread;

    private volatile Collection<I> _inputs;
    private volatile Duration _checkInterval = DEFAULT_CHECK_INTERVAL;


    protected ServicesManager() {
        _checkThread = new Thread(new Checker(), toString() + ".Checker");
        _checkThread.setDaemon(true);
        _checkThread.start();
    }

    protected ServicesManager(Duration checkInterval) {
        this();
        setCheckInterval(checkInterval);
    }

    public Collection<I> getInputs() {
        return _inputs;
    }

    public void setInputs(Collection<I> inputs) {
        _inputs = inputs;
    }

    @Nonnull
    public Duration getCheckInterval() {
        return _checkInterval;
    }

    public void setCheckInterval(@Nonnull Duration checkInterval) {
        _checkInterval = checkInterval;
    }

    protected void setCheckerThreadName(@Nonnull String threadName) {
        _checkThread.setName(threadName);
    }

    @SuppressWarnings("DuplicateThrows")
    public void check() throws Exception, InterruptedException {
        final Collection<I> inputs = _inputs;
        check(inputs != null ? inputs : Collections.<I>emptySet());
    }

    @Nullable
    protected abstract void check(@Nonnull Collection<I> inputs) throws Exception;

    @Nullable
    protected abstract O tryTake();

    public abstract void markAsGone(@Nonnull O service) throws InterruptedException;

    @Nonnull
    public O take() throws InterruptedException {
        O output = tryTake();
        if (output == null) {
            try {
                check();
            } catch (final InterruptedException e) {
                throw e;
            } catch (final Exception e) {
                throw new RuntimeException("Could not take next connection.", e);
            }
            output = tryTake();
        }
        if (output == null) {
            throw new ServiceTemporaryUnavailableException("There is no services available. Are all services gone?");
        }
        return output;
    }

    @Override
    public void close() {
        if (_checkThread.isAlive()) {
            do {
                _checkThread.interrupt();
                try {
                    _checkThread.join(10);
                } catch (final InterruptedException ignored) {
                    currentThread().interrupt();
                    LOG.warn("Try to wait for the end of checkThread but got interrupt signal. Ignoring this for now. We hope that this thread will go to the end by his own. If not we have a zombie thread in the JVM.");
                }
            } while (!currentThread().isInterrupted() && _checkThread.isAlive());
        }
    }

    protected class Checker implements Runnable { @Override public void run() {
        long lastError = 0;
        while (!currentThread().isInterrupted()) {
            if (!currentThread().isInterrupted()) {
                try {
                    sleep(_checkInterval.in(MILLISECONDS));
                } catch (final InterruptedException ignored) {
                    currentThread().interrupt();
                }
            }
            try {
                check();
            } catch (final InterruptedException ignored) {
                currentThread().interrupt();
            } catch (final Exception e) {
                final long now = currentTimeMillis();
                if (lastError + MINUTES.toMillis(10) < now) {
                    LOG.error("While the check of the service inputs we got an error. This could be temporary but normally this is an serious error. The application tries to continue working with the old service inputs but it could be that this inputs are out of date. You will see this message max. every 10 minutes.", e);
                    lastError = now;
                }
            }
        }
    }}

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}

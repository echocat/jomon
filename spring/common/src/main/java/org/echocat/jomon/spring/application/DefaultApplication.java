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

package org.echocat.jomon.spring.application;

import org.echocat.jomon.runtime.concurrent.StopWatch;
import org.echocat.jomon.runtime.logging.LoggingEnvironment;
import org.echocat.jomon.runtime.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.lang.reflect.Field;

import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

@ThreadSafe
public class DefaultApplication implements Application {

    @Nonnull
    private static final Logger LOG = LoggerFactory.getLogger(DefaultApplication.class);

    @Nonnull
    private final ConfigurableApplicationContext _applicationContext;
    @Nonnull
    private final ApplicationInformation _information;
    @Nonnull
    private final LoggingEnvironment _loggingEnvironment;
    @Nonnull
    private final Thread _shutdownHook;

    @Nullable
    private StopWatch _stopWatch;

    public DefaultApplication(@Nonnull ConfigurableApplicationContext applicationContext, @Nonnull ApplicationInformation information, @Nonnull LoggingEnvironment loggingEnvironment) {
        _applicationContext = applicationContext;
        _information = information;
        _loggingEnvironment = loggingEnvironment;
        _shutdownHook = new Thread(getTitle() + " destroyer") {
            @Override
            public void run() {
                closeQuietly(DefaultApplication.this);
            }
        };
        if (applicationContext instanceof AbstractApplicationContext) {
            setShutdownHook(_shutdownHook, (AbstractApplicationContext) applicationContext);
        }
        getRuntime().addShutdownHook(_shutdownHook);
    }

    @Override
    public void init() {
        synchronized (this) {
            if (!_applicationContext.isActive()) {
                _applicationContext.refresh();
                _stopWatch = new StopWatch();
            }
        }
    }

    @Nonnull
    @Override
    public ApplicationContext getApplicationContext() {
        return _applicationContext;
    }

    @Nonnull
    @Override
    public ApplicationInformation getInformation() {
        return _information;
    }

    @Nonnull
    @Override
    public LoggingEnvironment getLoggingEnvironment() {
        return _loggingEnvironment;
    }

    @Override
    public void close() throws Exception {
        synchronized (this) {
            try {
                try {
                    try {
                        if (_applicationContext.isActive()) {
                            final Duration uptime = getUptime();
                            LOG.info("Stopping " + getTitle() + (uptime != null ? " (was up for: " + uptime.toPattern(MILLISECONDS) + ")" : "") + "...");
                            final StopWatch stopWatch = new StopWatch();
                            ((DisposableBean) _applicationContext).destroy();
                            LOG.info("Stopping " + getTitle() + "... DONE! (after: " + stopWatch.toCurrentPattern(MILLISECONDS) + ")");
                        }
                    } finally {
                        _stopWatch = null;
                    }
                } finally {
                    getRuntime().removeShutdownHook(_shutdownHook);
                }
            } finally {
                closeQuietly(_loggingEnvironment);
            }
        }
    }

    @Nonnull
    protected String getTitle() {
        return getInformation().getTitle();
    }

    @Override
    public String toString() {
        return getTitle();
    }

    protected void setShutdownHook(@Nonnull Thread thread, @Nonnull AbstractApplicationContext to) {
        try {
            final Field field = AbstractApplicationContext.class.getDeclaredField("shutdownHook");
            field.setAccessible(true);
            field.set(to, thread);
        } catch (final Exception e) {
            LOG.warn("Could not register shutdownHook at " + to + " this could cause to much memory consume of the JVM.", e);
        }
    }

    @Nonnull
    @Override
    public Duration getUptime() {
        final StopWatch stopWatch = _stopWatch;
        return stopWatch != null ? stopWatch.getCurrentDuration() : null;
    }
}

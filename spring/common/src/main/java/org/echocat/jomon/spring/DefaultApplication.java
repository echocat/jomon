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

package org.echocat.jomon.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.lang.reflect.Field;

import static java.lang.Runtime.getRuntime;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

@ThreadSafe
public class DefaultApplication implements Application {

    @Nonnull
    private static final Logger LOG = LoggerFactory.getLogger(DefaultApplication.class);

    @Nonnull
    private final ConfigurableApplicationContext _applicationContext;
    @Nonnull
    private final String _title;
    @Nullable
    private final Thread _shutdownHook;

    public DefaultApplication(@Nonnull ConfigurableApplicationContext applicationContext, @Nonnull String title) {
        _applicationContext = applicationContext;
        _title = title;
        _shutdownHook = new Thread(_title + " destroyer") {
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

    @Nonnull
    @Override
    public ConfigurableApplicationContext getApplicationContext() {
        return _applicationContext;
    }

    @Nonnull
    @Override
    public String getTitle() {
        return _title;
    }

    @Override
    public void close() throws Exception {
        synchronized (_applicationContext) {
            try {
                if (_applicationContext.isActive()) {
                    LOG.info("Stopping " + _title + "...");
                    ((DisposableBean) _applicationContext).destroy();
                    LOG.info("Stopping " + _title + "... DONE!");
                }
            } finally {
                getRuntime().removeShutdownHook(_shutdownHook);
            }
        }
    }

    @Override
    public String toString() {
        return _title;
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

}

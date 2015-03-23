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

package org.echocat.jomon.runtime.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactoryRegistry;
import org.slf4j.LoggerFactoryRegistry.Registration;
import org.xml.sax.InputSource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Reader;

import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

public class LogbackBasedLoggingEnvironment implements LoggingEnvironment {

    @Nullable
    private Registration _loggerFactoryRegistration;
    @Nullable
    private LoggerContext _loggerContext;
    @Nullable
    private Slf4jUtils.Installation _installation;

    public void init(@Nonnull LogbackBasedLoggingEnvironmentConfiguration with) {
        synchronized (this) {
            if (_loggerFactoryRegistration == null) {
                _loggerContext = createContextFor(with);
                _loggerFactoryRegistration = LoggerFactoryRegistry.register(_loggerContext);
                if (with.isInstallSl4jRequired()) {
                    _installation = Slf4jUtils.tryInstallSlf4jBridges(_loggerContext);
                }
            }
        }
    }

    @Nonnull
    protected LoggerContext createContextFor(@Nonnull LogbackBasedLoggingEnvironmentConfiguration requirement) {
        final LoggerContext context = new LoggerContext();
        configure(context, requirement);
        return context;
    }

    @Nonnull
    protected void configure(@Nonnull LoggerContext context, @Nonnull LogbackBasedLoggingEnvironmentConfiguration requirement) {
        try (final Reader reader = requirement.openAsReader()) {
            configure(context, requirement, reader);
        } catch (final Exception e) {
            throw new RuntimeException("Could not configure log4j.", e);
        }
    }

    protected void configure(@Nonnull LoggerContext context, @SuppressWarnings("UnusedParameters") @Nonnull LogbackBasedLoggingEnvironmentConfiguration with, @Nonnull Reader reader) throws Exception {
        final JoranConfigurator result = new JoranConfigurator();
        result.setContext(context);
        context.reset();
        result.doConfigure(new InputSource(reader));
    }

    @Override
    public void close() throws Exception {
        synchronized (this) {
            try {
                try {
                    closeQuietly(_installation);
                } finally {
                    _installation = null;
                }
            } finally {
                try {
                    try {
                        closeQuietly(_loggerFactoryRegistration);
                    } finally {
                        _loggerFactoryRegistration = null;
                    }
                } finally {
                    _loggerContext = null;
                }
            }
        }
    }

    @Nonnull
    protected ILoggerFactory loggerFactory() {
        final ILoggerFactory loggerFactory = _loggerContext;
        if (loggerFactory == null) {
            throw new IllegalStateException("Logging environment currently not initialized.");
        }
        return loggerFactory;
    }

    @Nonnull
    @Override
    public Logger getLogger(@Nonnull String name) {
        return loggerFactory().getLogger(name);
    }

    @Nonnull
    @Override
    public Logger getLogger(@Nonnull Class<?> type) {
        return getLogger(type.getName());
    }

}

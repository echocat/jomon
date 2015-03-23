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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.spi.LoggerContext;
import org.apache.logging.log4j.spi.LoggerContextFactory;
import org.apache.logging.slf4j.Log4jLogger;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactoryRegistry;
import org.slf4j.LoggerFactoryRegistry.Registration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

public class Log4J2BasedLoggingEnvironment implements LoggingEnvironment {

    protected static final String FQCN = Log4J2BasedLoggingEnvironment.class.getName();
    public static final ClassLoader CLASS_LOADER = Log4JBasedLoggingEnvironment.class.getClassLoader();

    @Nullable
    private LoggerContextFactory _factory;
    @Nullable
    private LoggerContextFactory _originalFactory;
    @Nullable
    private ILoggerFactory _loggerFactory;
    @Nullable
    private Registration _loggerFactoryRegistration;
    @Nullable
    private Slf4jUtils.Installation _installation;

    public void init(@Nonnull Log4J2BasedLoggingEnvironmentConfiguration with) {
        synchronized (this) {
            if (_factory == null) {
                _originalFactory = findOriginalFactory();
                _factory = new LoggerContextFactoryImpl(createLoggerContext(with));
                _loggerFactory = new LoggerFactory(_factory);
                LogManager.setFactory(_factory);
                _loggerFactoryRegistration = LoggerFactoryRegistry.register(_loggerFactory);
                if (with.isInstallSl4jRequired()) {
                    _installation = Slf4jUtils.tryInstallSlf4jBridges(_loggerFactory);
                }
            }
        }
    }

    @Nonnull
    protected LoggerContext createLoggerContext(@Nonnull Log4J2BasedLoggingEnvironmentConfiguration requirement) {
        final org.apache.logging.log4j.core.LoggerContext result = new org.apache.logging.log4j.core.LoggerContext(FQCN);
        result.start(createConfigurationFor(requirement));
        return result;
    }

    @Nonnull
    protected Configuration createConfigurationFor(@Nonnull Log4J2BasedLoggingEnvironmentConfiguration requirement) {
        return ConfigurationFactory.getInstance().getConfiguration(createConfigurationSourceFor(requirement));
    }

    @Nonnull
    protected ConfigurationSource createConfigurationSourceFor(@Nonnull Log4J2BasedLoggingEnvironmentConfiguration requirement) {
        try (final InputStream is = requirement.openAsInputStream()) {
            return createConfigurationSourceFor(requirement, is);
        } catch (final Exception e) {
            throw new RuntimeException("Could not configure log4j.", e);
        }
    }

    @Nonnull
    protected ConfigurationSource createConfigurationSourceFor(@SuppressWarnings("UnusedParameters") @Nonnull Log4J2BasedLoggingEnvironmentConfiguration requirement, @Nonnull InputStream is) throws IOException {
        return new ConfigurationSource(is);
    }

    @Nullable
    protected LoggerContextFactory findOriginalFactory() {
        return LogManager.getFactory();
    }


    @Override
    public void close() throws Exception {
        synchronized (this) {
            if (_factory != null) {
                try {
                    try {
                        try {
                            closeQuietly(_installation);
                        } finally {
                            _installation = null;
                        }
                    } finally {
                        try {
                            LogManager.setFactory(_originalFactory);
                        } finally {
                            try {
                                if (_loggerFactoryRegistration != null) {
                                    _loggerFactoryRegistration.close();
                                }
                            } finally {
                                _loggerFactoryRegistration = null;
                            }
                        }
                    }
                } finally {
                    _loggerFactory = null;
                    _factory = null;
                }
            }
        }
    }

    @Nonnull
    public LoggerContextFactory getFactory() {
        assertInitialized();
        return _factory;
    }

    @Nullable
    public LoggerContextFactory getOriginalFactory() {
        assertInitialized();
        return _originalFactory;
    }

    @Nonnull
    protected ILoggerFactory loggerFactory() {
        final ILoggerFactory loggerFactory = _loggerFactory;
        if (loggerFactory == null) {
            throw new IllegalStateException("Logging environment currently not initialized.");
        }
        return loggerFactory;
    }

    protected void assertInitialized() {
        if (_factory == null) {
            throw new IllegalStateException("Logging environment currently not initialized.");
        }
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

    protected static class LoggerContextFactoryImpl implements LoggerContextFactory {

        @Nonnull
        private final LoggerContext _loggerContext;

        public LoggerContextFactoryImpl(@Nonnull LoggerContext loggerContext) {
            _loggerContext = loggerContext;
        }

        @Override
        public LoggerContext getContext(String fqcn, ClassLoader loader, Object externalContext, boolean currentContext) {
            return _loggerContext;
        }

        @Override
        public LoggerContext getContext(String fqcn, ClassLoader loader, Object externalContext, boolean currentContext, URI configLocation, String name) {
            return _loggerContext;
        }

        @Override
        public void removeContext(LoggerContext context) {}

    }

    protected static class LoggerFactory implements ILoggerFactory {

        @Nonnull
        private final ConcurrentMap<String, Logger> _loggerMap = new ConcurrentHashMap<>();
        @Nonnull
        private final LoggerContextFactory _factory;

        public LoggerFactory(@Nonnull LoggerContextFactory factory) {
            _factory = factory;
        }

        @Override
        public Logger getLogger(String name) {
            Logger result = _loggerMap.get(name);
            if (result == null) {
                final ExtendedLogger log4jLogger = _factory.getContext(FQCN, CLASS_LOADER, null, true).getLogger(name);
                final Logger newInstance = new Log4jLogger(log4jLogger, name);
                final Logger oldInstance = _loggerMap.putIfAbsent(name, newInstance);
                result = oldInstance == null ? newInstance : oldInstance;
            }
            return result;
        }

    }

}

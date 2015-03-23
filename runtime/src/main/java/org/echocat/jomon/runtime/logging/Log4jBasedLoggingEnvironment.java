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

import org.apache.log4j.Hierarchy;
import org.apache.log4j.spi.DefaultRepositorySelector;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.RepositorySelector;
import org.apache.log4j.spi.RootLogger;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactoryRegistry;
import org.slf4j.LoggerFactoryRegistry.Registration;
import org.slf4j.impl.Log4jLoggerAdapter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.apache.log4j.Level.INFO;
import static org.apache.log4j.LogManager.getLoggerRepository;
import static org.apache.log4j.LogManager.setRepositorySelector;
import static org.echocat.jomon.runtime.logging.Log4JUtils.configure;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

public class Log4JBasedLoggingEnvironment implements LoggingEnvironment {

    @Nullable
    private RepositorySelector _selector;
    @Nullable
    private RepositorySelector _originalSelector;
    @Nullable
    private ILoggerFactory _loggerFactory;
    @Nullable
    private Registration _loggerFactoryRegistration;
    @Nullable
    private Slf4jUtils.Installation _installation;

    public void init(@Nonnull Log4JBasedLoggingEnvironmentConfiguration with) {
        synchronized (this) {
            if (_selector == null) {
                _originalSelector = findOriginalSelector();
                _selector = createSelectorFor(with);
                _loggerFactory = new LoggerFactory(_selector.getLoggerRepository());
                setRepositorySelector(_selector, null);
                _loggerFactoryRegistration = LoggerFactoryRegistry.register(_loggerFactory);
                if (with.isInstallSl4jRequired()) {
                    _installation = Slf4jUtils.tryInstallSlf4jBridges(_loggerFactory);
                }
            }
        }
    }

    @Nonnull
    protected RepositorySelector createSelectorFor(@Nonnull Log4JBasedLoggingEnvironmentConfiguration requirement) {
        return new DefaultRepositorySelector(createRepositoryFor(requirement));
    }

    @Nonnull
    protected LoggerRepository createRepositoryFor(@Nonnull Log4JBasedLoggingEnvironmentConfiguration requirement) {
        try (final Reader reader = requirement.openAsReader()) {
            return createRepositoryFor(requirement, reader);
        } catch (final Exception e) {
            throw new RuntimeException("Could not configure log4j.", e);
        }
    }

    @Nonnull
    protected LoggerRepository createRepositoryFor(@SuppressWarnings("UnusedParameters") @Nonnull Log4JBasedLoggingEnvironmentConfiguration requirement, @Nonnull Reader reader) throws IOException {
        final LoggerRepository result = new Hierarchy(new RootLogger(INFO));
        configure(reader, result);
        return result;
    }

    @Nullable
    protected RepositorySelector findOriginalSelector() {
        return new DefaultRepositorySelector(getLoggerRepository());
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
                            if (_selector != null) {
                                setRepositorySelector(_originalSelector, null);
                            }
                        } finally {
                            try {
                                closeQuietly(_loggerFactoryRegistration);
                            } finally {
                                _loggerFactoryRegistration = null;
                            }
                        }
                    } finally {
                        _loggerFactory = null;
                        _selector = null;
                    }
            }
        }
    }

    @Nonnull
    public RepositorySelector getSelector() {
        assertInitialized();
        return _selector;
    }

    @Nullable
    public RepositorySelector getOriginalSelector() {
        assertInitialized();
        return _originalSelector;
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
        if (_selector == null) {
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

    protected static class LoggerFactory implements ILoggerFactory {

        @Nonnull
        private final ConcurrentMap<String, Logger> _loggerMap = new ConcurrentHashMap<>();
        @Nonnull
        private final LoggerRepository _repository;

        public LoggerFactory(@Nonnull LoggerRepository repository) {
            _repository = repository;
        }

        @Override
        public Logger getLogger(String name) {
            Logger result = _loggerMap.get(name);
            if (result == null) {
                final org.apache.log4j.Logger log4jLogger;
                if(name.equalsIgnoreCase(ROOT_LOGGER_NAME)) {
                    log4jLogger = _repository.getRootLogger();
                } else {
                    log4jLogger = _repository.getLogger(name);
                }
                final Logger newInstance = createLoggerFor(log4jLogger);
                final Logger oldInstance = _loggerMap.putIfAbsent(name, newInstance);
                result = oldInstance == null ? newInstance : oldInstance;
            }
            return result;
        }

        @Nonnull
        protected Logger createLoggerFor(@Nonnull org.apache.log4j.Logger original) {
            try {
                final Constructor<Log4jLoggerAdapter> constructor = Log4jLoggerAdapter.class.getDeclaredConstructor(org.apache.log4j.Logger.class);
                constructor.setAccessible(true);
                return constructor.newInstance(original);
            } catch (final InvocationTargetException e) {
                // noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
                throw new RuntimeException("Could not create logger adapter for log4j.", e.getTargetException());
            } catch (final Exception e) {
                throw new RuntimeException("Could not access logger adapter for log4j.", e);
            }
        }

    }

}

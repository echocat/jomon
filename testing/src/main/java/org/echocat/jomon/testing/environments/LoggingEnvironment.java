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

package org.echocat.jomon.testing.environments;

import org.echocat.jomon.runtime.logging.Log4J2BasedLoggingEnvironmentConfiguration;
import org.echocat.jomon.runtime.logging.Log4JBasedLoggingEnvironmentConfiguration;
import org.echocat.jomon.runtime.logging.LogbackBasedLoggingEnvironmentConfiguration;
import org.echocat.jomon.runtime.logging.LoggingEnvironmentConfiguration;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.echocat.jomon.runtime.logging.Log4J2BasedLoggingEnvironmentConfiguration.ForClassLoader.log4J2BasedLoggingFor;
import static org.echocat.jomon.runtime.logging.Log4JBasedLoggingEnvironmentConfiguration.ForClassLoader.log4JBasedLoggingFor;
import static org.echocat.jomon.runtime.logging.LogbackBasedLoggingEnvironmentConfiguration.ForClassLoader.logbackBasedLoggingFor;
import static org.echocat.jomon.runtime.logging.LoggingEnvironmentGenerators.loggingEnvironmentGenerator;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;
import static org.echocat.jomon.testing.environments.LoggingEnvironment.Type.log4j;
import static org.echocat.jomon.testing.environments.LoggingEnvironment.Type.log4j2;
import static org.echocat.jomon.testing.environments.LoggingEnvironment.Type.logback;

public class LoggingEnvironment extends BaseEnvironment implements TestRule, org.echocat.jomon.runtime.logging.LoggingEnvironment {

    public static final Type DEFAULT_TYPE = Type.valueOf(System.getProperty(LoggingEnvironment.class.getName() + ".type", logback.name()));

    public static enum Type {
        log4j,
        log4j2,
        logback
    }

    @Nullable
    private final Class<?> _reference;
    @Nonnull
    private final LoggingEnvironmentConfiguration _configuration;
    @Nullable
    private org.echocat.jomon.runtime.logging.LoggingEnvironment _delegate;

    public LoggingEnvironment(@Nonnull LoggingEnvironmentConfiguration configuration) {
        _reference = null;
        _configuration = configuration;
    }

    public LoggingEnvironment() {
        this((Class<?>) null);
    }

    public LoggingEnvironment(@Nullable Type type) {
        this(null, type);
    }

    public LoggingEnvironment(@Nonnull Object object) {
        this(object, null);
    }

    public LoggingEnvironment(@Nonnull Object object, @Nullable Type type) {
        this(object instanceof Class ? (Class<?>) object : object.getClass(), type);
    }

    public LoggingEnvironment(@Nullable Class<?> clazz) {
        this(clazz, null);
    }

    public LoggingEnvironment(@Nullable Class<?> clazz, @Nullable Type type) {
        final Class<?> targetClass = clazz != null ? clazz : findTopFromCallStack();
        _reference = targetClass;
        _configuration = configurationFor(targetClass, type);
    }

    @Nonnull
    protected LoggingEnvironmentConfiguration configurationFor(@Nonnull Class<?> clazz, @Nullable Type type) {
        final LoggingEnvironmentConfiguration result;
        final Type targetType = type != null ? type : DEFAULT_TYPE;
        if (targetType == log4j) {
            result = log4jConfigurationFor(clazz);
        } else if (targetType == log4j2) {
            result = log4J2ConfigurationFor(clazz);
        } else if (targetType == logback) {
            result = logbackConfigurationFor(clazz);
        } else {
            throw new IllegalArgumentException("Could not handle type: " + targetType);
        }
        return result;
    }

    @Nonnull
    protected LoggingEnvironmentConfiguration log4jConfigurationFor(@Nonnull Class<?> clazz) {
        final String configurationFile = configurationFileFor(clazz, ".log4j.xml", "log4j.xml", Log4JBasedLoggingEnvironmentConfiguration.DEFAULT_CONFIGURATION_FILE);
        return log4JBasedLoggingFor(clazz.getClassLoader(), configurationFile);
    }

    @Nonnull
    protected LoggingEnvironmentConfiguration log4J2ConfigurationFor(@Nonnull Class<?> clazz) {
        final String configurationFile = configurationFileFor(clazz, ".log4j2.xml", "log4j2.xml", Log4J2BasedLoggingEnvironmentConfiguration.DEFAULT_CONFIGURATION_FILE);
        return log4J2BasedLoggingFor(clazz.getClassLoader(), configurationFile);
    }

    @Nonnull
    protected LoggingEnvironmentConfiguration logbackConfigurationFor(@Nonnull Class<?> clazz) {
        final String configurationFile = configurationFileFor(clazz, ".logback.xml", "logback.xml", LogbackBasedLoggingEnvironmentConfiguration.DEFAULT_CONFIGURATION_FILE);
        return logbackBasedLoggingFor(clazz.getClassLoader(), configurationFile);
    }

    @Nonnull
    protected String configurationFileFor(@Nonnull Class<?> clazz, @Nonnull String fileNameSuffixOfClasses, @Nonnull String fileNameInPackage, @Nonnull String defaultConfigurationFile) {
        String configuration = findFileFor(clazz, fileNameSuffixOfClasses, fileNameInPackage);
        if (configuration == null) {
            configuration = findFileFor(LoggingEnvironment.class, fileNameSuffixOfClasses, fileNameInPackage);
        }
        if (configuration == null) {
            configuration = defaultConfigurationFile;
        }
        return configuration;
    }

    public void init() {
        synchronized (this) {
            if (_delegate == null) {
                _delegate = loggingEnvironmentGenerator().generate(_configuration);
            }
        }
    }

    @Override
    public void close() {
        synchronized (this) {
            try {
                closeQuietly(_delegate);
            } finally {
                _delegate = null;
            }
        }
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() { @Override public void evaluate() throws Throwable {
            init();
            try {
                base.evaluate();
            } finally {
                closeQuietly(LoggingEnvironment.this);
            }
        }};
    }

    @Override
    @Nonnull
    public Logger getLogger(@Nonnull String name) {
        return _delegate.getLogger(name);
    }

    @Override
    @Nonnull
    public Logger getLogger(@Nonnull Class<?> reference) {
        return _delegate.getLogger(reference);
    }

    /**
     * @deprecated DO NOT USE! This is only for backwards capability of {@link LogEnvironment}.
     */
    @SuppressWarnings("deprecation")
    @Nullable
    @Deprecated
    protected Class<?> getReference() {
        return _reference;
    }

}

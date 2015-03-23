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

import org.echocat.jomon.runtime.logging.Log4JBasedLoggingEnvironmentConfiguration;
import org.echocat.jomon.runtime.logging.LoggingEnvironmentConfiguration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;

import static org.echocat.jomon.runtime.logging.LogbackBasedLoggingEnvironmentConfiguration.ForClassLoader.defaultLogbackBasedLogging;

public abstract class ApplicationRequirementSupport<T extends ApplicationRequirementSupport<T>> implements ApplicationRequirement {

    @Nonnull
    private final ApplicationContextRequirement _applicationContextRequirement;
    @Nonnull
    private LoggingEnvironmentConfiguration _loggingEnvironmentConfiguration = defaultLogbackBasedLogging();
    @Nullable
    private String _title;

    public ApplicationRequirementSupport(@Nonnull ApplicationContextRequirement applicationContextRequirement) {
        _applicationContextRequirement = applicationContextRequirement;
    }

    @Override
    @Nullable
    public String getTitle() {
        return _title;
    }

    public void setTitle(@Nullable String title) {
        _title = title;
    }

    @Nonnull
    public T withTitle(@Nullable String title) {
        setTitle(title);
        return thisObject();
    }

    @Nonnull
    public T withApplicationTitle(@Nonnull String defaultApplicationName) {
        setTitle(defaultApplicationName);
        return thisObject();
    }

    @Override
    @Nonnull
    public ApplicationContextRequirement getApplicationContextRequirement() {
        return _applicationContextRequirement;
    }

    public void setLoggingEnvironmentConfiguration(@Nonnull LoggingEnvironmentConfiguration loggingEnvironmentConfiguration) {
        if (loggingEnvironmentConfiguration == null) {
            throw new NullPointerException();
        }
        _loggingEnvironmentConfiguration = loggingEnvironmentConfiguration;
    }

    @Nonnull
    public T withLoggingConfiguration(@Nonnull LoggingEnvironmentConfiguration configuration) {
        setLoggingEnvironmentConfiguration(configuration);
        return thisObject();
    }

    @Nonnull
    public T withLog4jFor(@Nonnull Class<?> type, @Nonnull String fileName) {
        setLoggingEnvironmentConfiguration(Log4JBasedLoggingEnvironmentConfiguration.ForClass.log4JBasedLoggingFor(type, fileName));
        return thisObject();
    }

    @Nonnull
    public T withLog4jFor(@Nonnull Class<?> type) {
        setLoggingEnvironmentConfiguration(Log4JBasedLoggingEnvironmentConfiguration.ForClass.log4JBasedLoggingFor(type));
        return thisObject();
    }

    @Nonnull
    public T withLog4jFor(@Nonnull ClassLoader loader, @Nonnull String fileName) {
        setLoggingEnvironmentConfiguration(Log4JBasedLoggingEnvironmentConfiguration.ForClassLoader.log4JBasedLoggingFor(loader, fileName));
        return thisObject();
    }

    @Nonnull
    public T withLog4jFor(@Nonnull File file) {
        setLoggingEnvironmentConfiguration(Log4JBasedLoggingEnvironmentConfiguration.ForFile.log4JBasedLoggingFor(file));
        return thisObject();
    }

    @Nonnull
    @Override
    public LoggingEnvironmentConfiguration getLoggingEnvironmentConfiguration() {
        return _loggingEnvironmentConfiguration;
    }

    @Nonnull
    protected T thisObject() {
        // noinspection unchecked
        return (T) this;
    }
}

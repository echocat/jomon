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

import org.springframework.context.ApplicationContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.echocat.jomon.spring.ApplicationContextGenerators.applicationContextGenerator;

public abstract class ApplicationRequirementSupport<T extends ApplicationRequirementSupport<T>> implements ApplicationRequirement {

    @Nullable
    private String _defaultTitle;
    @Nullable
    private ApplicationContext _parentApplicationContext;

    @Nonnull
    private ApplicationContextGenerator _applicationContextGenerator = applicationContextGenerator();

    @Override
    @Nonnull
    public abstract String getBeanXmlInClassPath();

    @Override
    @Nullable
    public abstract String getLog4jConfigurationInClassPath();

    @Override
    @Nonnull
    public abstract ClassLoader getClassLoader();

    @Override
    @Nonnull
    public String getDefaultTitle() {
        final String defaultTitle = _defaultTitle;
        return defaultTitle != null ? defaultTitle : getBeanXmlInClassPath();
    }

    public void setDefaultTitle(@Nullable String defaultTitle) {
        _defaultTitle = defaultTitle;
    }

    @Override
    @Nullable
    public ApplicationContext getParentApplicationContext() {
        return _parentApplicationContext;
    }

    public void setParentApplicationContext(@Nullable ApplicationContext parentApplicationContext) {
        _parentApplicationContext = parentApplicationContext;
    }

    @Override
    @Nonnull
    public ApplicationContextGenerator getApplicationContextGenerator() {
        return _applicationContextGenerator;
    }

    public void setApplicationContextGenerator(@Nonnull ApplicationContextGenerator applicationContextGenerator) {
        _applicationContextGenerator = applicationContextGenerator;
    }

    @Nonnull
    public T withDefaultApplicationName(@Nonnull String defaultApplicationName) {
        setDefaultTitle(defaultApplicationName);
        return thisObject();
    }

    @Nonnull
    public T withParentApplicationContext(@Nonnull ApplicationContext applicationContext) {
        setParentApplicationContext(applicationContext);
        return thisObject();
    }

    @Nonnull
    public T withApplicationContextGenerator(@Nonnull ApplicationContextGenerator applicationContextGenerator) {
        setApplicationContextGenerator(applicationContextGenerator);
        return thisObject();
    }

    @Nonnull
    protected T thisObject() {
        // noinspection unchecked
        return (T) this;
    }
}

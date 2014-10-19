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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PathBasedApplicationRequirement extends ApplicationRequirementSupport<PathBasedApplicationRequirement> {

    @Nonnull
    private final ClassLoader _classLoader;
    @Nonnull
    private final String _beanXmlInClassPath;
    @Nullable
    private String _log4jConfigurationInClassPath;

    public PathBasedApplicationRequirement(@Nonnull ClassLoader classLoader, @Nonnull String beanXmlInClassPath) {
        _classLoader = classLoader;
        _beanXmlInClassPath = beanXmlInClassPath;
    }

    @Nonnull
    public static PathBasedApplicationRequirement applicationFor(@Nonnull ClassLoader classLoader, @Nonnull String beanXmlInClassPath) {
        return new PathBasedApplicationRequirement(classLoader, beanXmlInClassPath);
    }

    @Nonnull
    @Override
    public String getBeanXmlInClassPath() {
        return _beanXmlInClassPath;
    }

    @Nullable
    @Override
    public String getLog4jConfigurationInClassPath() {
        return _log4jConfigurationInClassPath;
    }

    public void setLog4jConfigurationInClassPath(@Nullable String log4jConfigurationInClassPath) {
        _log4jConfigurationInClassPath = log4jConfigurationInClassPath;
    }

    @Nonnull
    @Override
    public ClassLoader getClassLoader() {
        return _classLoader;
    }

    @Nonnull
    public PathBasedApplicationRequirement withLog4jConfigurationInClassPath(@Nonnull String fullConfigurationPath) {
        setLog4jConfigurationInClassPath(fullConfigurationPath);
        return thisObject();
    }

}

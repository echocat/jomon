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

public class ClassBasedApplicationRequirement extends ApplicationRequirementSupport<ClassBasedApplicationRequirement> {

    @Nonnull
    public static ClassBasedApplicationRequirement applicationBasedOn(@Nonnull Class<?> reference) {
        return new ClassBasedApplicationRequirement(reference);
    }

    @Nonnull
    private final Class<?> _reference;

    @Nonnull
    private ClassLoader _classLoader;
    @Nonnull
    private String _beanXmlFileName = "main.xml";
    @Nullable
    private String _log4jConfigurationFileName;

    public ClassBasedApplicationRequirement(@Nonnull Class<?> reference) {
        _reference = reference;
        _classLoader = reference.getClassLoader();
    }

    @Nonnull
    @Override
    public String getBeanXmlInClassPath() {
        return fullElementPath(_reference, _beanXmlFileName);
    }

    @Nullable
    @Override
    public String getLog4jConfigurationInClassPath() {
        final String log4jConigurationFileName = _log4jConfigurationFileName;
        return log4jConigurationFileName != null ? fullElementPath(_reference, log4jConigurationFileName) : null;
    }

    @Nonnull
    @Override
    public ClassLoader getClassLoader() {
        return _classLoader;
    }

    @Nonnull
    public Class<?> getReference() {
        return _reference;
    }

    public void setBeanXmlFileName(@Nonnull String beanXmlFileName) {
        _beanXmlFileName = beanXmlFileName;
    }

    @Nonnull
    public String getBeanXmlFileName() {
        return _beanXmlFileName;
    }

    public void setClassLoader(@Nonnull ClassLoader classLoader) {
        _classLoader = classLoader;
    }

    @Nullable
    public String getLog4jConfigurationFileName() {
        return _log4jConfigurationFileName;
    }

    public void setLog4jConfigurationFileName(@Nullable String log4jConfigurationFileName) {
        _log4jConfigurationFileName = log4jConfigurationFileName;
    }

    @Nonnull
    public ClassBasedApplicationRequirement withClassLoader(@Nonnull ClassLoader classLoader) {
        setClassLoader(classLoader);
        return thisObject();
    }

    @Nonnull
    public ClassBasedApplicationRequirement withLog4jConfigurationFileName(@Nonnull String fileName) {
        setLog4jConfigurationFileName(fileName);
        return thisObject();
    }

    @Nonnull
    public ClassBasedApplicationRequirement withBeanXmlFileName(@Nonnull String fileName) {
        setBeanXmlFileName(fileName);
        return thisObject();
    }

    @Nonnull
    protected String fullElementPath(@Nonnull Class<?> reference, @Nonnull String element) {
        return reference.getPackage().getName().replace('.', '/') + '/' + element;
    }

}

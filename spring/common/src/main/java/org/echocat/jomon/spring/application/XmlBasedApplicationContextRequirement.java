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

package org.echocat.jomon.spring.application;

import org.echocat.jomon.spring.application.ApplicationContextRequirement.Support;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import javax.annotation.Nonnull;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.echocat.jomon.runtime.CollectionUtils.addAll;
import static org.echocat.jomon.runtime.CollectionUtils.asImmutableList;

public class XmlBasedApplicationContextRequirement extends Support<XmlBasedApplicationContextRequirement> {

    @Nonnull
    public static XmlBasedApplicationContextRequirement applicationContextFor(@Nonnull Resource... resources) {
        return applicationContext().withConfigurations(resources);
    }

    @Nonnull
    public static XmlBasedApplicationContextRequirement applicationContextFor(@Nonnull File... files) {
        return applicationContext().withConfigurations(files);
    }

    @Nonnull
    public static XmlBasedApplicationContextRequirement applicationContextFor(@Nonnull URL... files) {
        return applicationContext().withConfigurations(files);
    }

    @Nonnull
    public static XmlBasedApplicationContextRequirement applicationContextFor(@Nonnull Class<?> relation, @Nonnull String file) {
        return applicationContext().withConfiguration(relation, file);
    }

    @Nonnull
    public static XmlBasedApplicationContextRequirement applicationContextFor(@Nonnull ClassLoader classLoader, @Nonnull String file) {
        return applicationContext().withConfiguration(classLoader, file);
    }

    @Nonnull
    private static XmlBasedApplicationContextRequirement applicationContext() {
        return new XmlBasedApplicationContextRequirement();
    }

    @Nonnull
    private final List<Resource> _configurationFiles = new ArrayList<>();

    @Nonnull
    public XmlBasedApplicationContextRequirement withConfiguration(@Nonnull Resource resource) {
        return withConfigurations(resource);
    }

    @Nonnull
    public XmlBasedApplicationContextRequirement withConfigurations(@Nonnull Resource... resources) {
        addAll(_configurationFiles, resources);
        return this;
    }

    @Nonnull
    public XmlBasedApplicationContextRequirement withConfigurations(@Nonnull File... files) {
        for (final File file : files) {
            _configurationFiles.add(new FileSystemResource(file));
        }
        return this;
    }

    @Nonnull
    public XmlBasedApplicationContextRequirement withConfiguration(@Nonnull File file) {
        return withConfigurations(file);
    }

    @Nonnull
    public XmlBasedApplicationContextRequirement withConfigurations(@Nonnull URL... files) {
        for (final URL file : files) {
            _configurationFiles.add(new UrlResource(file));
        }
        return this;
    }

    @Nonnull
    public XmlBasedApplicationContextRequirement withConfiguration(@Nonnull Class<?> relation, @Nonnull String file) {
        _configurationFiles.add(new ClassPathResource(relation.getPackage().getName().replace('.', '/') + "/" + file, relation));
        return this;
    }

    @Nonnull
    public XmlBasedApplicationContextRequirement withConfiguration(@Nonnull ClassLoader classLoader, @Nonnull String file) {
        _configurationFiles.add(new ClassPathResource(file, classLoader));
        return this;
    }

    @Nonnull
    public Collection<Resource> getConfigurationFiles() {
        return asImmutableList(_configurationFiles);
    }

    @Override
    @Nonnull
    public XmlBasedApplicationContextRequirement withClassLoader(@Nonnull ClassLoader classLoader) {
        setClassLoader(classLoader);
        return this;
    }

}

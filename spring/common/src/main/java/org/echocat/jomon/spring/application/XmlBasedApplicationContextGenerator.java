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

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.core.io.Resource;

import javax.annotation.Nonnull;
import java.util.Collection;

public class XmlBasedApplicationContextGenerator implements ApplicationContextGenerator {

    @Override
    public boolean supports(@Nonnull ApplicationContextRequirement configuration) {
        return configuration instanceof XmlBasedApplicationContextRequirement;
    }

    @Nonnull
    @Override
    public ConfigurableApplicationContext generate(@Nonnull ApplicationContextRequirement configuration) {
        if (configuration instanceof XmlBasedApplicationContextRequirement) {
            return generate((XmlBasedApplicationContextRequirement) configuration);
        } else {
            throw new IllegalArgumentException("Could not handle " + configuration + ".");
        }
    }

    @Nonnull
    protected ConfigurableApplicationContext generate(@Nonnull XmlBasedApplicationContextRequirement configuration) {
        final AbstractXmlApplicationContext result = new ApplicationContextImpl(configuration.getConfigurationFiles());
        result.setParent(configuration.getParentApplicationContext());
        result.setClassLoader(configuration.getClassLoader());
        return result;
    }

    protected static class ApplicationContextImpl extends AbstractXmlApplicationContext {

        @Nonnull
        private final Resource[] _configurations;

        public ApplicationContextImpl(@Nonnull Collection<Resource> configuration) {
            _configurations = configuration.toArray(new Resource[configuration.size()]);
        }

        @Override
        protected Resource[] getConfigResources() {
            return _configurations;
        }

    }
}

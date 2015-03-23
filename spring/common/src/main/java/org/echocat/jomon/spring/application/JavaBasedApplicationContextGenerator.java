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

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;

import javax.annotation.Nonnull;
import java.util.Collection;

public class JavaBasedApplicationContextGenerator implements ApplicationContextGenerator {

    @Override
    public boolean supports(@Nonnull ApplicationContextRequirement configuration) {
        return configuration instanceof JavaBasedApplicationContextRequirement;
    }

    @Nonnull
    @Override
    public ConfigurableApplicationContext generate(@Nonnull ApplicationContextRequirement configuration) {
        if (configuration instanceof JavaBasedApplicationContextRequirement) {
            return generate((JavaBasedApplicationContextRequirement) configuration);
        } else {
            throw new IllegalArgumentException("Could not handle " + configuration + ".");
        }
    }

    @Nonnull
    protected ConfigurableApplicationContext generate(@Nonnull JavaBasedApplicationContextRequirement configuration) {
        final Collection<Class<?>> classes = configuration.getClasses();
        final Impl result = new Impl();
        result.register(classes.toArray(new Class[classes.size()]));
        result.setParent(configuration.getParentApplicationContext());
        result.setClassLoader(configuration.getClassLoader());
        return result;
    }

    protected static class Impl extends GenericApplicationContext {

        @Nonnull
        private final AnnotatedBeanDefinitionReader _reader = new AnnotatedBeanDefinitionReader(this);

        public void register(Class<?>... annotatedClasses) {
            _reader.register(annotatedClasses);
        }

    }

}

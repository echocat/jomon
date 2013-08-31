/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012-2013 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.spring.testing.environments;

import org.echocat.jomon.testing.environments.BaseEnvironment;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.annotation.Nonnull;

public class BeanEnvironment extends BaseEnvironment {

    private final ConfigurableApplicationContext _applicationContext;

    public BeanEnvironment(@Nonnull Object bean) {
        final String configuration = findFileFor(bean.getClass(), getConfigurationFileNameSuffixOfClasses(), getConfigurationFileNameInPackage());
        if (configuration == null) {
            throw new IllegalStateException("Could not find any configuration for " + bean + ".");
        }
        boolean success = false;
        final ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(configuration);
        try {
            autowire(applicationContext, bean);
            success = true;
        } finally {
            if (!success) {
                try {
                    applicationContext.close();
                } catch (Exception ignored) {}
            }
        }
        _applicationContext = applicationContext;
    }

    protected void autowire(@Nonnull ConfigurableApplicationContext applicationContext, @Nonnull Object bean) {
        final AutowireCapableBeanFactory autowireCapableBeanFactory = applicationContext.getAutowireCapableBeanFactory();
        autowireCapableBeanFactory.autowireBean(bean);
    }

    public void autowire(@Nonnull Object bean) {
        autowire(getApplicationContext(), bean);
    }

    @Nonnull
    public ConfigurableApplicationContext getApplicationContext() {
        final ConfigurableApplicationContext applicationContext = _applicationContext;
        if (applicationContext == null) {
            throw new IllegalStateException("The initFor method was not yet called.");
        }
        return applicationContext;
    }

    @Override
    public void close() {
        final ConfigurableApplicationContext applicationContext = _applicationContext;
        if (applicationContext != null) {
            applicationContext.close();
        }
    }

    @Nonnull
    protected String getConfigurationFileNameSuffixOfClasses() {
        return ".beans.testing.xml";
    }

    @Nonnull
    protected String getConfigurationFileNameInPackage() {
        return "beans.testing.xml";
    }

}

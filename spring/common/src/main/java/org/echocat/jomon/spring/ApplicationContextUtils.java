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
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ApplicationContextUtils {

    private ApplicationContextUtils() {}

    @Nullable
    public static Class<?> findTypeOfBeanDefinition(@Nonnull ApplicationContext applicationContext, @Nonnull String beanName) {
        if (!(applicationContext instanceof ConfigurableApplicationContext)) {
            throw new IllegalArgumentException("Currently there is only an applicationContext of type " + ConfigurableApplicationContext.class.getName() + " supported.");
        }
        return findTypeOfBeanDefinition((ConfigurableApplicationContext) applicationContext, beanName);
    }

    @Nullable
    public static Class<?> findTypeOfBeanDefinition(@Nonnull ConfigurableApplicationContext applicationContext, @Nonnull String beanName) {
        return BeanFactoryUtils.findTypeOfBeanDefinition(applicationContext.getBeanFactory(), beanName);
    }

    @Nullable
    public static String findScopeOfBeanDefinition(@Nonnull ApplicationContext applicationContext, @Nonnull String beanName) {
        if (!(applicationContext instanceof ConfigurableApplicationContext)) {
            throw new IllegalArgumentException("Currently there is only an applicationContext of type " + ConfigurableApplicationContext.class.getName() + " supported.");
        }
        return findScopeOfBeanDefinition((ConfigurableApplicationContext) applicationContext, beanName);
    }

    @Nullable
    public static String findScopeOfBeanDefinition(@Nonnull ConfigurableApplicationContext applicationContext, @Nonnull String beanName) {
        return BeanFactoryUtils.findScopeOfBeanDefinition(applicationContext.getBeanFactory(), beanName);
    }

}

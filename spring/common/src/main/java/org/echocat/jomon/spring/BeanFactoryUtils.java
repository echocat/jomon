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

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BeanFactoryUtils {

    private BeanFactoryUtils() {}

    @Nullable
    public static Class<?> findTypeOfBeanDefinition(@Nonnull BeanFactory beanFactory, @Nonnull String beanName) {
        if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
            throw new IllegalArgumentException("Currently there is only a beanFactory of type " + ConfigurableListableBeanFactory.class.getName() + " supported.");
        }
        return findTypeOfBeanDefinition((ConfigurableListableBeanFactory) beanFactory, beanName);
    }

    @Nullable
    public static Class<?> findTypeOfBeanDefinition(@Nonnull ConfigurableListableBeanFactory beanFactory, @Nonnull String beanName) {
        Class<?> type;
        final BeanDefinition definition = beanFactory.getBeanDefinition(beanName);
        if (definition != null && definition.getFactoryMethodName() == null) {
            final String beanClassName = definition.getBeanClassName();
            if (beanClassName != null) {
                try {
                    type = AutomaticServicesDiscovery.class.getClassLoader().loadClass(beanClassName);
                } catch (final ClassNotFoundException ignored) {
                    type = null;
                }
            } else {
                type = null;
            }
        } else {
            type = null;
        }
        return type == null || FactoryBean.class.isAssignableFrom(type) ? null : type;
    }

    @Nullable
    public static String findScopeOfBeanDefinition(@Nonnull BeanFactory beanFactory, @Nonnull String beanName) throws NoSuchBeanDefinitionException  {
        if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
            throw new IllegalArgumentException("Currently there is only a beanFactory of type " + ConfigurableListableBeanFactory.class.getName() + " supported.");
        }
        return findScopeOfBeanDefinition((ConfigurableListableBeanFactory) beanFactory, beanName);
    }

    @Nullable
    public static String findScopeOfBeanDefinition(@Nonnull ConfigurableListableBeanFactory beanFactory, @Nonnull String beanName) throws NoSuchBeanDefinitionException {
        final BeanDefinition definition = beanFactory.getBeanDefinition(beanName);
        if (definition == null) {
            throw new NoSuchBeanDefinitionException("Could not find a bean named '" + beanName + "'.");
        }
        return definition.getScope();
    }

}

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

package org.echocat.jomon.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public abstract class AutomaticServicesDiscovery<V, C extends Collection<V>> implements FactoryBean<C>, ApplicationContextAware {

    private final Class<V> _expectedType;
    private ApplicationContext _applicationContext;

    private Collection<Class<?>> _excludes;

    protected AutomaticServicesDiscovery(@Nonnull Class<V> expectedType) {
        _expectedType = expectedType;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        _applicationContext = applicationContext;
    }

    protected abstract boolean isApplicable(@Nonnull Class<?> type, @Nonnull String withBeanName, @Nonnull ApplicationContext of);
    
    protected abstract C createNewCollection();

    @Override
    public C getObject() throws Exception {
        final C values = createNewCollection();
        final String[] names = _applicationContext.getBeanDefinitionNames();
        for (String name : names) {
            final Class<?> type = getTypeFor(name);
            if (type != null && isApplicable(type, name, _applicationContext) && !isExcluded(type)) {
                values.add(_applicationContext.getBean(name, _expectedType));
            }
        }
        return values;
    }

    protected boolean isExcluded(@Nonnull Class<?> type) {
        boolean excluded = false;
        if (_excludes != null) {
            for (Class<?> exclude : _excludes) {
                if (exclude.isAssignableFrom(type)) {
                    excluded = true;
                    break;
                }
            }
        }
        return excluded;
    }

    @Nullable
    protected Class<?> getTypeFor(@Nonnull String name) {
        Class<?> type;
        if (_applicationContext instanceof ConfigurableApplicationContext) {
            final ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) _applicationContext).getBeanFactory();
            final BeanDefinition definition = beanFactory.getBeanDefinition(name);
            if (definition != null && definition.getFactoryMethodName() == null) {
                final String beanClassName = definition.getBeanClassName();
                if (beanClassName != null) {
                    try {
                        type = AutomaticServicesDiscovery.class.getClassLoader().loadClass(beanClassName);
                    } catch (ClassNotFoundException ignored) {
                        type = null;
                    }
                } else {
                    type = null;
                }
            } else {
                type = null;
            }
        } else {
            throw new IllegalArgumentException("Currently there is only an applicationContext of type " + ConfigurableApplicationContext.class.getName() + " supported.");
        }
        return type == null || FactoryBean.class.isAssignableFrom(type)  ? null : type;
    }

    public Collection<Class<?>> getExcludes() {
        return _excludes;
    }

    public void setExcludes(Collection<Class<?>> excludes) {
        _excludes = excludes;
    }

    @Override public Class<?> getObjectType() { return createNewCollection().getClass(); }
    @Override public boolean isSingleton() { return true; }
}

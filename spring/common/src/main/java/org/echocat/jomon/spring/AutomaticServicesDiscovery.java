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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

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

    @SuppressWarnings("UnusedParameters")
    protected boolean isApplicable(@Nonnull Class<?> type, @Nonnull String withBeanName, @Nonnull ApplicationContext of) {
        return _expectedType.isAssignableFrom(type);
    }
    
    @SuppressWarnings("UnusedParameters")
    protected boolean isApplicable(@Nonnull V bean, @Nonnull String withBeanName, @Nonnull ApplicationContext of) {
        return true;
    }

    protected abstract C createNewCollection();

    @Override
    public C getObject() throws Exception {
        final C values = createNewCollection();
        final String[] names = _applicationContext.getBeanDefinitionNames();
        for (final String name : names) {
            final Class<?> type = findTypeOfBeanDefinition(name);
            if (type != null && isApplicable(type, name, _applicationContext) && !isExcluded(type)) {
                final V bean = _applicationContext.getBean(name, _expectedType);
                if (isApplicable(bean, name, _applicationContext)) {
                    values.add(bean);
                }
            }
        }
        return values;
    }

    protected boolean isExcluded(@Nonnull Class<?> type) {
        boolean excluded = false;
        if (_excludes != null) {
            for (final Class<?> exclude : _excludes) {
                if (exclude.isAssignableFrom(type)) {
                    excluded = true;
                    break;
                }
            }
        }
        return excluded;
    }

    @Nullable
    protected Class<?> findTypeOfBeanDefinition(@Nonnull String beanName) {
        return ApplicationContextUtils.findTypeOfBeanDefinition(_applicationContext, beanName);
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

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

import org.echocat.jomon.runtime.instance.InstanceProvider;
import org.echocat.jomon.runtime.logging.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

import static org.echocat.jomon.runtime.instance.InstanceProvider.Type.*;
import static org.echocat.jomon.runtime.logging.LogLevel.debug;
import static org.echocat.jomon.runtime.logging.Slf4jUtils.isEnabled;
import static org.echocat.jomon.runtime.logging.Slf4jUtils.log;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

public class BeanProvider implements InstanceProvider, ApplicationContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(BeanProvider.class);

    @Nonnull
    private final Map<Class<?>, Object> _localSingletonCache = new WeakHashMap<>();

    @Nullable
    private ApplicationContext _applicationContext;

    private boolean _createLocaleBeansIfNotPresentInApplicationContext = true;
    @Nonnull
    private LogLevel _logLevelForLocalCreationOfBeans = debug;
    @Nullable
    private Pattern _logLocalCreationOfBeansThatNamesMatch;

    public boolean isCreateLocaleBeansIfNotPresentInApplicationContext() {
        return _createLocaleBeansIfNotPresentInApplicationContext;
    }

    public void setCreateLocaleBeansIfNotPresentInApplicationContext(boolean createLocaleBeansIfNotPresentInApplicationContext) {
        _createLocaleBeansIfNotPresentInApplicationContext = createLocaleBeansIfNotPresentInApplicationContext;
    }

    @Nonnull
    public LogLevel getLogLevelForLocalCreationOfBeans() {
        return _logLevelForLocalCreationOfBeans;
    }

    public void setLogLevelForLocalCreationOfBeans(@Nonnull LogLevel logLevelForLocalCreationOfBeans) {
        _logLevelForLocalCreationOfBeans = logLevelForLocalCreationOfBeans;
    }

    @Nullable
    public Pattern getLogLocalCreationOfBeansThatNamesMatch() {
        return _logLocalCreationOfBeansThatNamesMatch;
    }

    public void setLogLocalCreationOfBeansThatNamesMatch(@Nullable Pattern logLocalCreationOfBeansThatNamesMatch) {
        _logLocalCreationOfBeansThatNamesMatch = logLocalCreationOfBeansThatNamesMatch;
    }

    @Nonnull
    @Override
    public <T> T provideFor(@Nonnull String name, @Nonnull Class<T> clazz, @Nonnull Type type) {
        final T result;
        final AutowireCapableBeanFactory beanFactory = getBeanFactory();
        if (beanFactory.containsBean(name)) {
            validateTypeAgainstBeanConfiguration(name, type);
            result = beanFactory.getBean(name, clazz);
        } else if (isCreateLocaleBeansIfNotPresentInApplicationContext()) {
            result = type == singleton ? getLocalCreatedAndConfiguredSingleton(name, clazz) : createInstanceAndConfigure(name, clazz);
        } else {
            throw new IllegalArgumentException("No such bean named '" + name + "' found.");
        }
        return result;
    }

    @Nonnull
    @Override
    public <T> T provideFor(@Nonnull Class<T> clazz, @Nonnull Type type) {
        return provideFor(clazz.getName(), clazz, type);
    }

    @Nonnull
    @Override
    public <T> T provideFor(@Nonnull String name, @Nonnull Class<T> clazz) {
        return provideFor(name, clazz, undefined);
    }

    @Nonnull
    @Override
    public <T> T provideFor(@Nonnull Class<T> clazz) {
        return provideFor(clazz, undefined);
    }

    protected void validateTypeAgainstBeanConfiguration(@Nonnull String name, @Nonnull Type type) {
        if (type != undefined) {
            final BeanDefinition definition = getBeanDefinitionRegistry().getBeanDefinition(name);
            final String scope = definition.getScope();
            if (type == multiton && !SCOPE_PROTOTYPE.equals(scope)) {
                throw new IllegalArgumentException("The bean '" + name + "' was requested as " + type + ". This requires the scope '" + SCOPE_PROTOTYPE + "' but was '" + scope + "'.");
            }
            if (type == singleton && !SCOPE_SINGLETON.equals(scope)) {
                throw new IllegalArgumentException("The bean '" + name + "' was requested as " + type + ". This requires the scope '" + SCOPE_SINGLETON + "' but was '" + scope + "'.");
            }
        }
    }

    @Nonnull
    protected <T> T createInstanceAndConfigure(@Nonnull String name, @Nonnull Class<T> type) {
        final T result = createInstanceOf(type);
        getBeanFactory().autowireBean(result);
        if (isEnabled(LOG, getLogLevelForLocalCreationOfBeans()) && shouldLogLocalCreationOf(name)) {
            log(LOG, getLogLevelForLocalCreationOfBeans(), "Created bean '" + name + "' of type " + type.getName() + " locally because not present in application context.");
        }
        return result;
    }

    protected boolean shouldLogLocalCreationOf(@Nonnull String name) {
        return _logLocalCreationOfBeansThatNamesMatch == null
            || _logLocalCreationOfBeansThatNamesMatch.matcher(name).matches();
    }

    @Nonnull
    protected <T> T getLocalCreatedAndConfiguredSingleton(@Nonnull String name, @Nonnull Class<T> clazz) {
        synchronized (_localSingletonCache) {
            // noinspection unchecked
            T result = (T) _localSingletonCache.get(clazz);
            if (result == null) {
                result = createInstanceAndConfigure(name, clazz);
                _localSingletonCache.put(clazz, result);
            }
            return result;
        }
    }

    @Nonnull
    protected <T> T createInstanceOf(@Nonnull Class<T> type) {
        try {
            return type.newInstance();
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("Could not create new instance of " + type.getName() + ".", e);
        } catch (final InstantiationException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                //noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
                throw (RuntimeException) cause;
            } else if (cause instanceof Error) {
                //noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
                throw (Error) cause;
            } else {
                throw new RuntimeException("Could not create new instance of " + type.getName() + ".", cause != null ? cause : e);
            }
        }
    }

    @Override
    public void setApplicationContext(@Nonnull ApplicationContext context) throws BeansException {
        if (!(context.getAutowireCapableBeanFactory() instanceof BeanDefinitionRegistry)) {
            throw new IllegalArgumentException("The autowireCapableBeanFactory of given context must also implement " + BeanDefinitionRegistry.class.getName() + ".");
        }
        _applicationContext = context;
    }

    @Nonnull
    protected ApplicationContext getApplicationContext() {
        final ApplicationContext context = _applicationContext;
        if (context == null) {
            throw new IllegalStateException("setApplicationContext() was not called yet.");
        }
        context.getAutowireCapableBeanFactory();
        return context;
    }

    @Nonnull
    protected AutowireCapableBeanFactory getBeanFactory() {
        return getApplicationContext().getAutowireCapableBeanFactory();
    }

    @Nonnull
    protected BeanDefinitionRegistry getBeanDefinitionRegistry() {
        return (BeanDefinitionRegistry) getBeanFactory();
    }
}

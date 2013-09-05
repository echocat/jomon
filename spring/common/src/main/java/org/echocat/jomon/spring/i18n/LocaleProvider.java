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

package org.echocat.jomon.spring.i18n;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class LocaleProvider implements BeanPostProcessor, InitializingBean, FactoryBean<Set<Locale>> {

    private final Set<Locale> _locales = new HashSet<>();
    
    @Override public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException { return bean; }

    @Override public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof Locale) {
            _locales.add((Locale) bean);
        }
        return bean; 
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        _locales.clear();
        _locales.add(new Locale(""));
    }

    @Nonnull
    public Set<Locale> getLocales() {
        return _locales;
    }

    @Override public Set<Locale> getObject() throws Exception { return getLocales(); }
    @Override public Class<?> getObjectType() { return Set.class; }
    @Override public boolean isSingleton() { return true; }
}

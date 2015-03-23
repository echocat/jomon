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

package org.echocat.jomon.spring;

import org.echocat.jomon.runtime.RuntimeInformation;
import org.echocat.jomon.spring.types.BasePropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.factory.config.CustomEditorConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.io.File;

import static java.lang.System.getProperty;

@Configuration
public class BaseConfiguration {

    @Bean(name = "org.echocat.jomon.spring.BeanPostConfigurer")
    public static BeanPostConfigurer beanPostConfigurer() throws Exception {
        final BeanPostConfigurer result = new BeanPostConfigurer();
        result.setPropertiesFromFileIfExists(new File(getProperty("applicationContext.properties", "conf/applicationContext.properties")));
        return result;
    }

    @Bean(name = "org.echocat.jomon.spring.ContextLoadThreadGroup")
    @Lazy
    public ContextLoadThreadGroup contextLoadThreadGroup() throws Exception {
        return new ContextLoadThreadGroup();
    }

    @Bean(name = "org.echocat.jomon.runtime.RuntimeInformation")
    @Lazy
    public RuntimeInformation runtimeInformation() throws Exception {
        return new RuntimeInformation();
    }

    @Bean(name = {"org.echocat.jomon.spring.BeanProvider", "org.echocat.jomon.runtime.instance.InstanceProvider"})
    public BeanProvider beanProvider() throws Exception {
        return new BeanProvider();
    }

    @Bean(name = "customEditorConfigurer")
    public static CustomEditorConfigurer customEditorConfigurer() throws Exception {
        final CustomEditorConfigurer result = new CustomEditorConfigurer();
        result.setPropertyEditorRegistrars(new PropertyEditorRegistrar[]{ new BasePropertyEditorRegistrar() });
        return result;
    }

}

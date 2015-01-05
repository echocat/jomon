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

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.annotation.Nonnull;

public class DefaultApplicationContextGenerator implements ApplicationContextGenerator {

    @Nonnull
    private static final DefaultApplicationContextGenerator INSTANCE = new DefaultApplicationContextGenerator();

    @Nonnull
    public static DefaultApplicationContextGenerator getInstance() {
        return INSTANCE;
    }

    @Nonnull
    public static DefaultApplicationContextGenerator defaultApplicationContextGenerator() {
        return getInstance();
    }

    @Nonnull
    @Override
    public ConfigurableApplicationContext generate(@Nonnull ApplicationRequirement requirement) {
        final AbstractApplicationContext result = newInstance(requirement);
        result.setClassLoader(requirement.getClassLoader());
        return result;
    }

    @Nonnull
    protected AbstractApplicationContext newInstance(@Nonnull ApplicationRequirement requirement) {
        return new ClassPathXmlApplicationContext(new String[]{requirement.getBeanXmlInClassPath()}, false, requirement.getParentApplicationContext());
    }

}

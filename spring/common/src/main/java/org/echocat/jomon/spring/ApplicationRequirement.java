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

import org.echocat.jomon.runtime.generation.Requirement;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ApplicationRequirement extends Requirement {

    @Nonnull
    public String getBeanXmlInClassPath();

    @Nullable
    public String getLog4jConfigurationInClassPath();

    @Nonnull
    public ClassLoader getClassLoader();

    @Nonnull
    public String getDefaultTitle();

    @Nonnull
    public ApplicationContext getParentApplicationContext();

    @Nonnull
    public ApplicationContextGenerator getApplicationContextGenerator();

}

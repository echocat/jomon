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

package org.echocat.jomon.spring.application;

import org.echocat.jomon.runtime.generation.Requirement;
import org.echocat.jomon.runtime.logging.LoggingEnvironmentConfiguration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ApplicationRequirement extends Requirement {

    @Nullable
    public String getTitle();

    @Nonnull
    public ApplicationContextRequirement getApplicationContextRequirement();

    @Nonnull
    public LoggingEnvironmentConfiguration getLoggingEnvironmentConfiguration();

}

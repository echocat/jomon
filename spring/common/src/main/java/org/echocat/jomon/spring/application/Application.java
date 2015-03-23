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

package org.echocat.jomon.spring.application;

import org.echocat.jomon.runtime.logging.LoggingEnvironment;
import org.echocat.jomon.runtime.util.Duration;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Application extends AutoCloseable {

    public void init();

    @Nonnull
    public ApplicationContext getApplicationContext();

    @Nonnull
    public ApplicationInformation getInformation();

    @Nonnull
    public LoggingEnvironment getLoggingEnvironment();

    @Nullable
    public Duration getUptime();

}

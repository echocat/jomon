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

package org.echocat.jomon.testing.environments;

import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.echocat.jomon.testing.environments.LoggingEnvironment.Type.log4j;

/**
 * @deprecated Use {@link LoggingEnvironment} instead.
 */
@Deprecated
public class LogEnvironment extends LoggingEnvironment {

    public LogEnvironment() { super(log4j); }

    public LogEnvironment(@Nonnull Object object) {
        super(object, log4j);
    }

    public LogEnvironment(@Nullable Class<?> clazz) {
        super(clazz, log4j);
    }

    @Nonnull
    @Deprecated
    public Logger getLogger() {
        // noinspection deprecation
        return getLogger(getReference());
    }

}

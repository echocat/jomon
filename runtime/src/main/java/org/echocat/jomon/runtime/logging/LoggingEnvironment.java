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

package org.echocat.jomon.runtime.logging;

import org.slf4j.Logger;

import javax.annotation.Nonnull;

public interface LoggingEnvironment extends AutoCloseable {

    @Nonnull
    public Logger getLogger(@Nonnull String name);

    @Nonnull
    public Logger getLogger(@Nonnull Class<?> type);

}

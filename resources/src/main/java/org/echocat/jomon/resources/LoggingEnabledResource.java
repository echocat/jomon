/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.resources;

import org.echocat.jomon.runtime.iterators.CloseableIterator;

import javax.annotation.Nonnull;
import java.io.IOException;

public interface LoggingEnabledResource extends Resource {

    public void logMessage(@Nonnull String message) throws IOException;

    @Nonnull
    public CloseableIterator<String> logMessageIterator() throws IOException;

}

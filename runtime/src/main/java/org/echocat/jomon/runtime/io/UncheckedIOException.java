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

package org.echocat.jomon.runtime.io;

import java.io.IOException;

public class UncheckedIOException extends RuntimeException {

    public UncheckedIOException() {}

    public UncheckedIOException(String message) {
        super(message);
    }

    public UncheckedIOException(String message, IOException cause) {
        super(message != null ? message : "", cause);
    }

    public UncheckedIOException(IOException cause) {
        this(cause != null ? cause.getMessage() : null, cause);
    }

    @Override
    public synchronized IOException getCause() {
        return (IOException) super.getCause();
    }
}

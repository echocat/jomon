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

package org.echocat.jomon.process;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.io.InputStream;
import java.io.OutputStream;

@ThreadSafe
public interface GeneratedProcess extends Process {

    @Nonnull
    public OutputStream getOutputStream();

    @Nonnull
    public InputStream getInputStream();

    @Nonnull
    public InputStream getErrorStream();

    @Nonnegative
    public int waitFor() throws InterruptedException;

    @Nonnegative
    public int exitValue() throws IllegalStateException;

    public boolean isAlive();

    public void shutdown();

    public boolean isDaemon();
}

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

package org.echocat.jomon.process;

import javax.annotation.Nonnegative;
import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;

@ThreadSafe
public interface GeneratedProcess<E, ID> extends Process<E, ID>, Streams {

    @Nonnegative
    public int waitFor() throws InterruptedException;

    @Nonnegative
    public int exitValue() throws IllegalStateException;

    public boolean isAlive();

    public boolean isDaemon();

    public void kill() throws IOException;

    /**
     * This method will try to shutdown the process regularly. If this will not work implicit {@link #kill()} is called.
     */
    @Override
    public void close() throws IOException;

}

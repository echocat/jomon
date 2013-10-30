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

package org.echocat.jomon.process.listeners.stream;

import org.echocat.jomon.process.GeneratedProcess;
import org.echocat.jomon.runtime.io.StreamType;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * @param <P> reference that this listener belongs to.
 */
public interface StreamListener<P extends GeneratedProcess<?, ?>> {

    public boolean canHandleReferenceType(@Nonnull Class<?> type);

    public void notifyProcessStarted(@Nonnull P process);

    public void notifyProcessStartupDone(@Nonnull P process);

    public void notifyOutput(@Nonnull P process, @Nonnull byte[] data, @Nonnegative int offset, @Nonnegative int length, @Nonnull StreamType streamType);

    public void flushOutput(@Nonnull P process, @Nonnull StreamType streamType);

    public void notifyProcessTerminated(@Nonnull P process, boolean success);

}

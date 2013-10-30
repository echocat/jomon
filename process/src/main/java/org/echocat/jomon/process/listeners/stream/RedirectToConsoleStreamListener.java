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

import javax.annotation.Nonnull;
import java.util.Map;

import static org.echocat.jomon.runtime.io.StreamType.*;

public class RedirectToConsoleStreamListener<P extends GeneratedProcess<?, ?>> extends LineBasedAndStateEnabledStreamListenerSupport<P, RedirectToConsoleStreamListener<P>> {

    @Override
    protected void write(@Nonnull P process, @Nonnull String content, @Nonnull StreamType streamType) {
        if (streamType == stdout) {
            // noinspection UseOfSystemOutOrSystemErr
            System.out.print(content);
        } else if (streamType == stderr) {
            // noinspection UseOfSystemOutOrSystemErr
            System.err.print(content);
        } else if (streamType == system) {
            // noinspection UseOfSystemOutOrSystemErr
            System.out.print(content);
        } else {
            throw new IllegalArgumentException("Could not handle streamType: " + streamType);
        }
    }

    public static class Provider extends StreamListenerProviderSupport {

        public Provider() {
            super(RedirectToConsoleStreamListener.class, "redirectToConsole");
        }

        @Nonnull
        @Override
        protected LineBasedStreamListenerSupport<?, ?> createInstanceBy(@Nonnull Class<? extends LineBasedStreamListenerSupport<?, ?>> listenerType, @Nonnull Map<String, String> parameters) {
            return new RedirectToConsoleStreamListener();
        }

    }

}

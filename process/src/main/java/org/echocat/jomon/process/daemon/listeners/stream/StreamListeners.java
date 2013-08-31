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

package org.echocat.jomon.process.daemon.listeners.stream;

import org.echocat.jomon.process.GeneratedProcess;
import org.echocat.jomon.process.daemon.StreamType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings("ConstantNamingConvention")
public class StreamListeners {

    private static final StreamListenerProvider PROVIDER = new CompoundStreamListenerProvider(true);

    public static final StreamListener redirectToConsole = new ReadOnlyStreamListener(new RedirectToConsoleStreamListener());
    public static final StreamListener redirectToLogger = new ReadOnlyStreamListener(new RedirectToLoggerStreamListener());

    @Nullable
    public static StreamListener streamListenerFor(@Nullable String configuration) {
        return streamListenerFor(configuration, null);
    }

    @Nullable
    public static StreamListener streamListenerFor(@Nullable String configuration, @Nullable StreamListener fallback) {
        final StreamListener result = configuration != null ? PROVIDER.provideFor(configuration) : null;
        return result != null ? result : fallback;
    }

    public static class ReadOnlyStreamListener implements StreamListener {

        private final StreamListener _delegate;

        public ReadOnlyStreamListener(@Nonnull StreamListener delegate) {
            _delegate = delegate;
        }

        @Override
        public void notifyProcessStarted(@Nonnull GeneratedProcess process) {
            _delegate.notifyProcessStarted(process);
        }

        @Override
        public void notifyLineOutput(@Nonnull GeneratedProcess process, @Nonnull String line, @Nonnull StreamType streamType) {
            _delegate.notifyLineOutput(process, line, streamType);
        }

        @Override
        public void notifyProcessTerminated(@Nonnull GeneratedProcess process) {
            _delegate.notifyProcessTerminated(process);
        }

    }

    private StreamListeners() {}

}

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
import org.slf4j.Logger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings("ConstantNamingConvention")
public class StreamListeners {

    private static final StreamListenerProvider PROVIDER = new CompoundStreamListenerProvider(true);

    public static final StreamListener<?> redirectToConsole = new ReadOnlyStreamListener<>(new RedirectToConsoleStreamListener<>());
    public static final StreamListener<?> redirectToLogger = new ReadOnlyStreamListener<>(new RedirectToLoggerStreamListener<>());

    @Nonnull
    public static <P extends GeneratedProcess<?, ?>> StreamListener<P> redirectToConsole() {
        // noinspection unchecked
        return (StreamListener<P>) redirectToConsole;
    }

    @Nonnull
    public static <P extends GeneratedProcess<?, ?>> StreamListener<P> redirectToLogger() {
        // noinspection unchecked
        return (StreamListener<P>) redirectToLogger;
    }

    @Nonnull
    public static <P extends GeneratedProcess<?, ?>> StreamListener<P> redirectToLogger(@Nullable Class<?> forClass) {
        return new RedirectToLoggerStreamListener<>(forClass);
    }

    @Nonnull
    public static <P extends GeneratedProcess<?, ?>> StreamListener<P> redirectToLogger(@Nullable String loggerName) {
        return new RedirectToLoggerStreamListener<>(loggerName);
    }

    @Nonnull
    public static <P extends GeneratedProcess<?, ?>> StreamListener<P> redirectToLogger(@Nullable Logger logger) {
        return new RedirectToLoggerStreamListener<>(logger);
    }

    @Nullable
    public static <P extends GeneratedProcess<?, ?>> StreamListener<P> streamListenerFor(@Nonnull Class<P> referenceType, @Nullable String configuration) {
        return streamListenerFor(referenceType, configuration, null);
    }

    @Nullable
    public static <P extends GeneratedProcess<?, ?>> StreamListener<P> streamListenerFor(@Nonnull Class<P> referenceType, @Nullable String configuration, @Nullable StreamListener<P> fallback) {
        final StreamListener<P> result = configuration != null ? PROVIDER.provideFor(referenceType, configuration) : null;
        return result != null ? result : fallback;
    }

    public static class ReadOnlyStreamListener<P extends GeneratedProcess<?, ?>> implements StreamListener<P> {

        private final StreamListener<P> _delegate;

        public ReadOnlyStreamListener(@Nonnull StreamListener<P> delegate) {
            _delegate = delegate;
        }

        @Override
        public boolean canHandleReferenceType(@Nonnull Class<?> type) {
            return _delegate.canHandleReferenceType(type);
        }

        @Override
        public void notifyProcessStarted(@Nonnull P process) {
            _delegate.notifyProcessStarted(process);
        }

        @Override
        public void notifyProcessStartupDone(@Nonnull P process) {
            _delegate.notifyProcessStartupDone(process);
        }

        @Override
        public void notifyOutput(@Nonnull P process, @Nonnull byte[] data, @Nonnegative int offset, @Nonnegative int length, @Nonnull StreamType streamType) {
            _delegate.notifyOutput(process, data, offset, length, streamType);
        }

        @Override
        public void flushOutput(@Nonnull P process, @Nonnull StreamType streamType) {
            _delegate.flushOutput(process, streamType);
        }

        @Override
        public void notifyProcessTerminated(@Nonnull P reference, boolean regular) {
            _delegate.notifyProcessTerminated(reference, regular);
        }

    }

    private StreamListeners() {}

}

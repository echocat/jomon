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

package org.echocat.jomon.process.listeners.startup;

import org.echocat.jomon.process.GeneratedProcess;
import org.echocat.jomon.process.listeners.stream.StreamListener;
import org.echocat.jomon.runtime.io.StreamType;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.ByteBuffer;

/**
 * @param <P> reference that this listener belongs to.
 */
@SuppressWarnings("ConstantNamingConvention")
public interface StartupListener<P extends GeneratedProcess<?, ?>> extends StreamListener<P> {

    public static final StartupListener<?> noop = new NoopStartupListener();

    public boolean waitForSuccessfulStart() throws InterruptedException;

    /**
     * @return <code>true</code> if the process was successful started. If the process is currently in startup process <code>null</code> is returned.
     */
    @Nullable
    public Boolean isSuccessfulStarted();

    /**
     * @return an instance of {@link Throwable} if there was any problem while starting the process. If <code>null</code> is returned there was no problem while starting.
     * @throws IllegalStateException if this method is called before the startup is done.
     */
    @Nullable
    public Throwable getStartupProblem() throws IllegalStateException;

    /**
     * @return the recorded content while waiting if supported by this listener. Otherwise <code>null</code> will be returned.
     * @throws IllegalStateException if this method is called before the startup is done.
     */
    @Nullable
    public ByteBuffer getRecordedContentWhileWaiting() throws IllegalStateException;

    /**
     * @return the recorded content while waiting if supported by this listener. Otherwise <code>null</code> will be returned.
     * @throws IllegalStateException if this method is called before the startup is done.
     */
    @Nullable
    public String getRecordedContentWhileWaitingAsString() throws IllegalStateException;

    public static class NoopStartupListener<P extends GeneratedProcess<?, ?>> implements StartupListener<P> {

        @Nonnull
        public static <P extends GeneratedProcess<?, ?>> StartupListener<P> noop() {
            // noinspection unchecked
            return (StartupListener<P>) noop;
        }

        @Override public boolean canHandleReferenceType(@Nonnull Class<?> type) { return true; }
        @Override public boolean waitForSuccessfulStart() throws InterruptedException { return true; }
        @Nullable @Override public Boolean isSuccessfulStarted() { return true; }
        @Nullable @Override public Throwable getStartupProblem() throws IllegalStateException { return null; }
        @Nullable @Override public ByteBuffer getRecordedContentWhileWaiting() throws IllegalStateException { return null; }
        @Nullable @Override public String getRecordedContentWhileWaitingAsString() throws IllegalStateException { return null; }
        @Override public void notifyOutput(@Nonnull P process, @Nonnull byte[] data, @Nonnegative int offset, @Nonnegative int length, @Nonnull StreamType streamType) { }
        @Override public void flushOutput(@Nonnull P process, @Nonnull StreamType streamType) {}
        @Override public void notifyProcessStarted(@Nonnull P process) {}
        @Override public void notifyProcessStartupDone(@Nonnull P process) {}
        @Override public void notifyProcessTerminated(@Nonnull P process, boolean success) {}

    }

}

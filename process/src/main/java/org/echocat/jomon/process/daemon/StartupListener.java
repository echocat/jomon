package org.echocat.jomon.process.daemon;

import org.echocat.jomon.process.GeneratedProcess;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings("ConstantNamingConvention")
public interface StartupListener extends StreamListener {

    public static final StartupListener noop = new NoopStartupListener();

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
    public String getRecordedContentWhileWaiting() throws IllegalStateException;

    public static class NoopStartupListener implements StartupListener {

        @Override public boolean waitForSuccessfulStart() throws InterruptedException { return true; }
        @Nullable @Override public Boolean isSuccessfulStarted() { return true; }
        @Nullable @Override public Throwable getStartupProblem() throws IllegalStateException { return null; }
        @Nullable @Override public String getRecordedContentWhileWaiting() throws IllegalStateException { return null; }
        @Override public void notifyLineOutput(@Nonnull GeneratedProcess process, @Nonnull String line, @Nonnull StreamType streamType) {}
        @Override public void notifyProcessStarted(@Nonnull GeneratedProcess process) {}
        @Override public void notifyProcessTerminated(@Nonnull GeneratedProcess process) {}

    }

}

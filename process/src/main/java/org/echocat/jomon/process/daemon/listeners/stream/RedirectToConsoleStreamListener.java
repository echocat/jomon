package org.echocat.jomon.process.daemon.listeners.stream;

import org.echocat.jomon.process.GeneratedProcess;
import org.echocat.jomon.process.daemon.StreamType;

import javax.annotation.Nonnull;

import java.util.Map;

import static org.echocat.jomon.process.daemon.StreamType.*;

public class RedirectToConsoleStreamListener extends StreamListenerSupport<RedirectToConsoleStreamListener> {

    @Override
    protected void write(@Nonnull GeneratedProcess process, @Nonnull String content, @Nonnull StreamType streamType) {
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

    public static class Provider extends StreamListenerProviderSupport<RedirectToConsoleStreamListener> {

        public Provider() {
            super(RedirectToConsoleStreamListener.class, "redirectToConsole");
        }

        @Nonnull
        @Override
        protected RedirectToConsoleStreamListener createInstanceBy(@Nonnull Class<RedirectToConsoleStreamListener> type, @Nonnull Map<String, String> parameters) {
            return new RedirectToConsoleStreamListener();
        }

    }

}
package org.echocat.jomon.process.daemon.listeners.stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface StreamListenerProvider {

    @Nullable
    public StreamListener provideFor(@Nonnull String configuration);

}

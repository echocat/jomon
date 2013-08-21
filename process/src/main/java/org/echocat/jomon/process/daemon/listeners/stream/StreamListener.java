package org.echocat.jomon.process.daemon.listeners.stream;

import org.echocat.jomon.process.GeneratedProcess;
import org.echocat.jomon.process.daemon.StreamType;

import javax.annotation.Nonnull;

public interface StreamListener {

    public void notifyProcessStarted(@Nonnull GeneratedProcess process);

    public void notifyLineOutput(@Nonnull GeneratedProcess process, @Nonnull String line, @Nonnull StreamType streamType);

    public void notifyProcessTerminated(@Nonnull GeneratedProcess process);

}

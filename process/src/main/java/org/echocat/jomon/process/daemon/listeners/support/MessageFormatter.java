package org.echocat.jomon.process.daemon.listeners.support;

import javax.annotation.Nonnull;
import java.util.Map;

public interface MessageFormatter {

    @Nonnull
    public String format(@Nonnull Map<String, Object> parameters);
}

package org.echocat.jomon.process.daemon.listeners.stream;

import org.echocat.jomon.process.GeneratedProcess;
import org.echocat.jomon.process.daemon.StreamType;
import org.echocat.jomon.runtime.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Map;

import static org.echocat.jomon.process.daemon.StreamType.*;

public class RedirectToLoggerStreamListener extends StreamListenerSupport<RedirectToLoggerStreamListener> {

    private static final Logger LOG = LoggerFactory.getLogger(RedirectToLoggerStreamListener.class);

    public RedirectToLoggerStreamListener() {
        whichFormatsMessagesWith("{message}");
    }

    @Override
    protected void write(@Nonnull GeneratedProcess process, @Nonnull String content, @Nonnull StreamType streamType) {
        for (String partOfLine : StringUtils.split(removeLastLineBreakFrom(content), '\n')) {
            final String formattedLine = "[" + process.getId() + "] " + partOfLine;
            if (streamType == stdout) {
                LOG.info(formattedLine);
            } else if (streamType == stderr) {
                LOG.error(formattedLine);
            } else if (streamType == system) {
                LOG.info(formattedLine);
            } else {
                throw new IllegalArgumentException("Could not handle streamType: " + streamType);
            }
        }
    }

    public static class Provider extends StreamListenerProviderSupport<RedirectToLoggerStreamListener> {

        public Provider() {
            super(RedirectToLoggerStreamListener.class, "redirectToLogger");
        }

        @Nonnull
        @Override
        protected RedirectToLoggerStreamListener createInstanceBy(@Nonnull Class<RedirectToLoggerStreamListener> type, @Nonnull Map<String, String> parameters) {
            return new RedirectToLoggerStreamListener();
        }

    }

}

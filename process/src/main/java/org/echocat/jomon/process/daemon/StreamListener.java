package org.echocat.jomon.process.daemon;

import org.echocat.jomon.process.GeneratedProcess;
import org.echocat.jomon.runtime.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

import static org.echocat.jomon.process.daemon.StreamType.stderr;
import static org.echocat.jomon.process.daemon.StreamType.stdout;

@SuppressWarnings("ConstantNamingConvention")
public interface StreamListener {

    public static final StreamListener redirectToConsole = new RedirectToConsoleStreamListener();
    public static final StreamListener redirectToLogger = new RedirectToLoggerStreamListener();

    public void notifyProcessStarted(@Nonnull GeneratedProcess process);

    public void notifyLineOutput(@Nonnull GeneratedProcess process, @Nonnull String line, @Nonnull StreamType streamType);

    public void notifyProcessTerminated(@Nonnull GeneratedProcess process);

    public static class RedirectToConsoleStreamListener implements StreamListener {

        @Override
        public void notifyLineOutput(@Nonnull GeneratedProcess process, @Nonnull String line, @Nonnull StreamType streamType) {
            if (streamType == stdout) {
                // noinspection UseOfSystemOutOrSystemErr
                System.out.print(line);
            } else if (streamType == stderr) {
                // noinspection UseOfSystemOutOrSystemErr
                System.err.print(line);
            } else {
                throw new IllegalArgumentException("Could not handle streamType: " + streamType);
            }
        }

        @Override public void notifyProcessStarted(@Nonnull GeneratedProcess process) {}
        @Override public void notifyProcessTerminated(@Nonnull GeneratedProcess process) {}
    }

    public static class RedirectToLoggerStreamListener implements StreamListener {

        private static final Logger LOG = LoggerFactory.getLogger(RedirectToLoggerStreamListener.class);

        @Override
        public void notifyLineOutput(@Nonnull GeneratedProcess process, @Nonnull String line, @Nonnull StreamType streamType) {
            for (String partOfLine : StringUtils.split(removeLastLineBreakFrom(line), '\n')) {
                final String formattedLine = "[" + process.getId() + "] " + partOfLine;
                if (streamType == stdout) {
                    LOG.info(formattedLine);
                } else if (streamType == stderr) {
                    LOG.error(formattedLine);
                } else {
                    throw new IllegalArgumentException("Could not handle streamType: " + streamType);
                }
            }
        }

        @Nonnull
        protected String removeLastLineBreakFrom(@Nonnull String step0) {
            final String step1 = step0.endsWith("\n") ? step0.substring(0, step0.length() - 1) : step0;
            final String step2 = step1.endsWith("\r") ? step1.substring(0, step1.length() - 1) : step1;
            final String step3 = step2.endsWith("\n") ? step2.substring(0, step1.length() - 2) : step2;
            return step3;
        }

        @Override public void notifyProcessStarted(@Nonnull GeneratedProcess process) {}
        @Override public void notifyProcessTerminated(@Nonnull GeneratedProcess process) {}

    }

}

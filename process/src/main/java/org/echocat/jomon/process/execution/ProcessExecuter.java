package org.echocat.jomon.process.execution;

import org.echocat.jomon.process.GeneratedProcess;
import org.echocat.jomon.process.GeneratedProcessRequirement;
import org.echocat.jomon.process.ProcessRepository;
import org.echocat.jomon.process.daemon.StreamType;
import org.echocat.jomon.runtime.concurrent.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.io.*;
import java.util.regex.Pattern;

import static java.lang.Boolean.TRUE;
import static java.util.regex.Pattern.compile;
import static org.echocat.jomon.process.ProcessRepository.processRepository;
import static org.echocat.jomon.process.daemon.StreamType.stderr;
import static org.echocat.jomon.process.daemon.StreamType.stdout;

public class ProcessExecuter {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessExecuter.class);

    @Nonnull
    private final ProcessRepository _processRepository;

    public ProcessExecuter() {
        this(processRepository());
    }

    public ProcessExecuter(@Nonnull ProcessRepository processRepository) {
        _processRepository = processRepository;
    }

    @Nonnull
    public Response execute(@Nonnull GeneratedProcessRequirement requirement) throws InterruptedException {
        requirement.whichIsDaemon();
        final GeneratedProcess process = _processRepository.generate(requirement);
        try {
            try (final OutputMonitor stdoutMonitor = new OutputMonitor(process, stdout)) {
                try (final OutputMonitor stderrMonitor = new OutputMonitor(process, stderr)) {
                    final int exitCode = process.waitFor();
                    return new Response(stdoutMonitor.getRecordedContent(), stderrMonitor.getRecordedContent(), exitCode);
                }
            }
        } finally {
            process.shutdown();
        }
    }

    public static class Response {

        @Nonnull
        private final String _stdout;
        @Nonnull
        private final String _stderr;
        @Nonnegative
        private final int _exitCode;

        public Response(@Nonnull String stdout, @Nonnull String stderr, @Nonnegative int exitCode) {
            _stdout = stdout;
            _stderr = stderr;
            _exitCode = exitCode;
        }

        @Nonnull
        public String getStdout() {
            return _stdout;
        }

        @Nonnull
        public String getStderr() {
            return _stderr;
        }

        @Nonnegative
        public int getExitCode() {
            return _exitCode;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "{exitCode:" + _exitCode + "}";
        }

        public boolean wasSuccessful() {
            return getExitCode() == 0;
        }

        public boolean hasErrorOutput() {
            return !getStderr().trim().isEmpty();
        }

        public boolean isStdoutMatching(@Nonnull Pattern pattern) {
            return pattern.matcher(getStdout()).matches();
        }

        public boolean isStdoutMatching(@Nonnull String pattern) {
            return isStdoutMatching(compile(pattern));
        }

        public boolean isStderrMatching(@Nonnull Pattern pattern) {
            return pattern.matcher(getStderr()).matches();
        }

        public boolean isStderrMatching(@Nonnull String pattern) {
            return isStderrMatching(compile(pattern));
        }

    }

    protected static class OutputMonitor extends Thread implements AutoCloseable {

        private final StringBuilder _buffer = new StringBuilder();
        private final ThreadLocal<Boolean> _alreadyInClosing = new ThreadLocal<>();
        private final InputStream _stream;


        public OutputMonitor(@Nonnull GeneratedProcess process, @Nonnull StreamType streamType) {
            super("OutputMonitor:" + process.getId() + ":" + streamType);
            setDaemon(true);
            _stream = streamType == stdout ? process.getInputStream() : process.getErrorStream();
            start();
        }

        @Override
        public void run() {
            final InputStreamReader reader = new InputStreamReader(_stream);
            final BufferedReader bufferedReader = new BufferedReader(reader);
            try {
                String line = bufferedReader.readLine();
                while (!Thread.currentThread().isInterrupted() && line != null) {
                    synchronized (_buffer) {
                        _buffer.append(line).append('\n');
                    }
                    line = bufferedReader.readLine();
                }
            } catch (InterruptedIOException ignored) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                // noinspection InstanceofCatchParameter
                if (!(e instanceof IOException) || !"Stream closed".equals(e.getMessage())) {
                    LOG.warn("Could not read from " + _stream + ".", e);
                }
            }
        }

        @Nonnull
        public String getRecordedContent() {
            synchronized (_buffer) {
                return _buffer.toString();
            }
        }

        @Override
        public void close() {
            if (!TRUE.equals(_alreadyInClosing.get())) {
                _alreadyInClosing.set(TRUE);
                try {
                    ThreadUtils.stop(this);
                } finally {
                    _alreadyInClosing.remove();
                }
            }
        }
    }

}

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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import static java.lang.Boolean.TRUE;
import static java.util.regex.Pattern.compile;
import static org.echocat.jomon.process.ProcessRepository.processRepository;
import static org.echocat.jomon.process.daemon.StreamType.stderr;
import static org.echocat.jomon.process.daemon.StreamType.stdout;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

public class ProcessExecuter {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessExecuter.class);

    @Nonnull
    private final ProcessRepository _processRepository;

    @Nonnegative
    private int _readSize = 4096;

    public ProcessExecuter() {
        this(processRepository());
    }

    public ProcessExecuter(@Nonnull ProcessRepository processRepository) {
        _processRepository = processRepository;
    }

    @Nonnull
    public Response execute(@Nonnull GeneratedProcessRequirement requirement) throws InterruptedException {
        final GeneratedProcess process = _processRepository.generate(requirement);
        try {
            try (final OutputMonitor stdoutMonitor = new OutputMonitor(process, stdout)) {
                try (final OutputMonitor stderrMonitor = new OutputMonitor(process, stderr)) {
                    final int exitCode = process.waitFor();
                    stdoutMonitor.waitFor();
                    stderrMonitor.waitFor();
                    return new Response(stdoutMonitor.getRecordedContent(), stderrMonitor.getRecordedContent(), exitCode);
                }
            }
        } finally {
            process.shutdown();
        }
    }

    @Nonnull
    public ProcessExecuter withReadSize(@Nonnegative int readSize) {
        setReadSize(readSize);
        return this;
    }

    public void setReadSize(@Nonnegative int readSize) {
        _readSize = readSize;
    }

    @Nonnegative
    public int getReadSize() {
        return _readSize;
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

    protected class OutputMonitor extends Thread implements AutoCloseable {

        private final StringBuilder _buffer = new StringBuilder();
        private final ThreadLocal<Boolean> _alreadyInClosing = new ThreadLocal<>();
        private final InputStream _stream;

        private final Lock _lock = new ReentrantLock();
        private final Condition _condition = _lock.newCondition();


        public OutputMonitor(@Nonnull GeneratedProcess process, @Nonnull StreamType streamType) {
            super("OutputMonitor:" + process.getId() + ":" + streamType);
            setDaemon(true);
            _stream = streamType == stdout ? process.getInputStream() : process.getErrorStream();
            start();
        }

        @Override
        public void run() {
            final InputStreamReader reader = new InputStreamReader(_stream);
            try {
                final char[] buf = new char[_readSize];
                int read = reader.read(buf);
                while (!currentThread().isInterrupted() && read >= 0) {
                    _buffer.append(buf, 0, read);
                    read = reader.read(buf);
                }
            } catch (InterruptedIOException ignored) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                // noinspection InstanceofCatchParameter
                if (!(e instanceof IOException) || !"Stream closed".equals(e.getMessage())) {
                    LOG.warn("Could not read from " + _stream + ".", e);
                }
            } finally {
                _lock.lock();
                try {
                    _condition.signalAll();
                } finally {
                    _lock.unlock();
                }
            }
        }

        @Nonnull
        public String getRecordedContent() {
            synchronized (_buffer) {
                return _buffer.toString();
            }
        }

        public void waitFor() throws InterruptedException {
            _lock.lockInterruptibly();
            try {
                if (isAlive()) {
                    _condition.await();
                }
            } finally {
                _lock.unlock();
            }
        }

        @Override
        public void close() {
            if (!TRUE.equals(_alreadyInClosing.get())) {
                _alreadyInClosing.set(TRUE);
                try {
                    try {
                        closeQuietly(_stream);
                    } finally {
                        ThreadUtils.stop(this);
                    }
                } finally {
                    _alreadyInClosing.remove();
                }
            }
        }
    }

}

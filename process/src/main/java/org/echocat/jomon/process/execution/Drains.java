package org.echocat.jomon.process.execution;

import org.echocat.jomon.runtime.logging.LogLevel;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.echocat.jomon.process.execution.Drain.ForLogger.drainForLogger;
import static org.echocat.jomon.runtime.logging.LogLevel.error;
import static org.echocat.jomon.runtime.logging.LogLevel.info;
import static org.slf4j.LoggerFactory.getLogger;

public class Drains {

    @Nonnull
    public static Drains drains() {
        return new Drains();
    }

    @Nonnull
    private Drain _stdout = Drain.stdout;
    @Nonnull
    private Drain _stderr = Drain.stderr;

    @Nonnull
    public Drains stdout(@Nonnull Drain drain) {
        if (drain == null) {
            throw new NullPointerException();
        }
        _stdout = drain;
        return this;
    }

    @Nonnull
    public Drains stderr(@Nonnull Drain drain) {
        if (drain == null) {
            throw new NullPointerException();
        }
        _stderr = drain;
        return this;
    }

    public void setStdout(@Nonnull Drain drain) {
        stdout(drain);
    }

    public void setStderr(@Nonnull Drain drain) {
        stderr(drain);
    }

    @Nonnull
    public Drain getStdout() {
        return stdout();
    }

    @Nonnull
    public Drain getStderr() {
        return stderr();
    }

    @Nonnull
    public Drain stdout() {
        return _stdout;
    }

    @Nonnull
    public Drain stderr() {
        return _stderr;
    }

    @Nonnull
    public Drains forLogger(@Nullable Class<?> forClass) {
        return forLogger(getLogger(forClass));
    }

    @Nonnull
    public Drains forLogger(@Nullable String loggerName) {
        return forLogger(getLogger(loggerName));
    }

    @Nonnull
    public Drains forLogger(@Nullable Logger logger) {
        return forLogger(logger, null, null);
    }

    @Nonnull
    public Drains forLogger(@Nullable Logger logger, @Nullable LogLevel stdoutLogLevel, @Nullable LogLevel stderrLogLevel) {
        final Logger targetLogger = logger != null ? logger : getLogger(getClass());
        stdout(drainForLogger(targetLogger).loggingOn(stdoutLogLevel != null ? stdoutLogLevel : info));
        stderr(drainForLogger(targetLogger).loggingOn(stderrLogLevel != null ? stderrLogLevel : error));
        return this;
    }

}

/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012-2013 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.process.daemon;

import org.echocat.jomon.process.CouldNotStartException;
import org.echocat.jomon.process.GeneratedProcess;
import org.echocat.jomon.process.listeners.startup.StartupListener;
import org.echocat.jomon.runtime.io.StreamType;
import org.echocat.jomon.runtime.util.ByteCount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static java.lang.Thread.currentThread;
import static org.echocat.jomon.runtime.CollectionUtils.asImmutableList;
import static org.echocat.jomon.runtime.concurrent.ThreadUtils.stop;
import static org.echocat.jomon.runtime.util.ByteCount.byteCountOf;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietlyIfAutoCloseable;

public abstract class ProcessDaemon<
    E,
    ID,
    P extends GeneratedProcess<E, ID>,
    R extends ProcessDaemonRequirement<E, ID, P, ?>,
    DEB
> implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessDaemon.class);

    @Nonnull
    public static final ByteCount DEFAULT_SIZE_OF_READ_BUFFER = byteCountOf("1k");

    @Nonnull
    private final R _requirement;
    @Nonnull
    private final P _process;
    @Nonnull
    private final List<Thread> _monitors;

    protected ProcessDaemon(@Nonnull DEB dependencies, @Nonnull R requirement) throws CouldNotStartException {
        _requirement = requirement;
        try {
            _process = generateProcess(dependencies, requirement);
        } catch (final Exception e) {
            throw new RuntimeException("Could not create process.", e);
        }
        boolean success = false;
        try {
            requirement.getStartupListener().notifyProcessStarted(_process);
            requirement.getStreamListener().notifyProcessStarted(_process);
            _monitors = asImmutableList(
                createOutputMonitorThreadFor(requirement, _process, _process.getStderr(), StreamType.stderr),
                createOutputMonitorThreadFor(requirement, _process, _process.getStdout(), StreamType.stdout),
                createProcessMonitorThreadFor(requirement, _process)
            );
            for (final Thread monitor : _monitors) {
                monitor.start();
            }
            waitForStart(requirement);
            success = true;
            requirement.getStartupListener().notifyProcessStartupDone(_process);
            requirement.getStreamListener().notifyProcessStartupDone(_process);
        } catch (final IOException e) {
            throw new CouldNotStartException("Could not control the process " + _process + ".", e);
        } finally {
            if (!success) {
                shutdownImmediatelyAfterStart(requirement);
            }
        }
    }

    @Nonnull
    protected Thread createOutputMonitorThreadFor(@Nonnull R requirement, @Nonnull P process, @Nonnull InputStream is, @Nonnull StreamType streamType) {
        final OutputMonitor<E, ID, P, R>  monitor = createOutputMonitorFor(requirement, process, is, streamType);
        return new Thread(monitor, streamType + ":" + process.getId());
    }

    @Nonnull
    protected OutputMonitor<E, ID, P, R> createOutputMonitorFor(@Nonnull R requirement, @Nonnull P process, @Nonnull InputStream is, @Nonnull StreamType streamType) {
        return new OutputMonitor<>(requirement, process, is, streamType, getSizeOfReadBuffer());
    }

    @Nonnull
    protected Thread createProcessMonitorThreadFor(@Nonnull R requirement, @Nonnull P process) {
        final ProcessMonitor<E, ID, P, R> monitor = new ProcessMonitor<>(requirement, process);
        final ID id = process.getId();
        return new Thread(monitor, "Process:" + (id != null ? id.toString() : "<unknown>"));
    }

    protected void waitForStart(@Nonnull R requirement) {
        try {
            final StartupListener<P> startupListener = requirement.getStartupListener();
            if (!startupListener.waitForSuccessfulStart()) {
                Throwable e = startupListener.getStartupProblem();
                if (e == null) {
                    e = new CouldNotStartException("Could not successful start process. Output while waiting:\n" + startupListener.getRecordedContentWhileWaitingAsString());
                }
                if (e instanceof CouldNotStartException) {
                    throw (CouldNotStartException) e;
                } else if (e instanceof Error) {
                    throw (Error) e;
                } else {
                    throw new CouldNotStartException("Could not successful start process. Output while waiting:\n" + startupListener.getRecordedContentWhileWaitingAsString(), e);
                }
            }
        } catch (final InterruptedException ignored) {
            currentThread().interrupt();
        }
    }

    protected void shutdownImmediatelyAfterStart(@Nonnull R requirement) {
        try {
            try {
                requirement.getStreamListener().notifyProcessTerminated(_process, false);
            } finally {
                requirement.getStartupListener().notifyProcessTerminated(_process, false);
            }
        } finally {
            closeQuietly(this);
        }
    }

    @Nonnull
    protected abstract P generateProcess(@Nonnull DEB dependencies, @Nonnull R requirement) throws Exception;

    @Override
    public void close() {
        try {
            try {
                _process.close();
                _process.waitFor();
            } catch (final Exception e) {
                // noinspection InstanceofCatchParameter
                if (e instanceof InterruptedException) {
                    currentThread().interrupt();
                }
                LOG.warn("Could not stop the process. The process will still be on this computer. You have to stop it manually.", e);
            } finally {
                stop(_monitors);
            }
        } finally {
            try {
                closeQuietlyIfAutoCloseable(_requirement.getStartupListener());
            } finally {
                closeQuietlyIfAutoCloseable(_requirement.getStreamListener());
            }
        }
    }

    @Nonnull
    public ByteCount getSizeOfReadBuffer() {
        return DEFAULT_SIZE_OF_READ_BUFFER;
    }

    @Nonnull
    public R getRequirement() {
        return _requirement;
    }

    @Nonnull
    public P getProcess() {
        return _process;
    }

    @Override
    public String toString() {
        final P process = getProcess();
        final ID pid = process.getId();
        return getClass().getSimpleName() + "{" + (pid != null ? "pid=" + pid + ", " : "") + "alive=" + process.isAlive() + "}";
    }

}

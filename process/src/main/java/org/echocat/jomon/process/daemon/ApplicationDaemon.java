/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.process.daemon;

import org.apache.commons.io.FileUtils;
import org.echocat.jomon.process.GeneratedProcess;
import org.echocat.jomon.process.ProcessRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.List;

import static java.lang.Thread.currentThread;
import static org.echocat.jomon.process.daemon.ApplicationDaemon.SubPath.inBaseDirectory;
import static org.echocat.jomon.process.daemon.StreamType.stderr;
import static org.echocat.jomon.process.daemon.StreamType.stdout;
import static org.echocat.jomon.runtime.CollectionUtils.asImmutableList;
import static org.echocat.jomon.runtime.concurrent.ThreadUtils.stop;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

public abstract class ApplicationDaemon<R extends ApplicationDaemonRequirement<?>> implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationDaemon.class);

    public enum SubPath {
        inBaseDirectory(null),
        inBinDirectory("bin");

        private final String _value;

        private SubPath(@Nullable String value) {
            _value = value;
        }

        @Nullable
        public String getValue() {
            return _value;
        }
    }

    @Nonnull
    private final R _requirement;
    @Nonnull
    private final GeneratedProcess _process;
    @Nonnull
    private final List<OutputMonitor> _monitors;

    private File _temporaryDirectory;

    protected ApplicationDaemon(@Nonnull R requirement) throws CouldNotStartProcessException {
        this(ProcessRepository.getInstance(), requirement);
    }

    protected ApplicationDaemon(@Nonnull ProcessRepository processRepository, @Nonnull R requirement) throws CouldNotStartProcessException {
        _requirement = requirement;
        try {
            _process = generateProcess(processRepository, requirement);
        } catch (Exception e) {
            throw new RuntimeException("Could not create process.", e);
        }
        requirement.getStartupListener().notifyProcessStarted(_process);
        requirement.getStreamListener().notifyProcessStarted(_process);
        _monitors = asImmutableList(
            new OutputMonitor(_process.getErrorStream(), stderr),
            new OutputMonitor(_process.getInputStream(), stdout)
        );
        for (OutputMonitor monitor : _monitors) {
            monitor.start();
        }
        waitForSuccessfulStart(requirement);
    }

    protected void waitForSuccessfulStart(@Nonnull R requirement) {
        try {
            final StartupListener startupListener = requirement.getStartupListener();
            if (!startupListener.waitForSuccessfulStart()) {
                shutdownImmediatelyAfterStart(requirement);
                Throwable e = startupListener.getStartupProblem();
                if (e == null) {
                    e = new CouldNotStartProcessException("Could not successful start process. Output while waiting:\n" + startupListener.getRecordedContentWhileWaiting());
                }
                if (e instanceof CouldNotStartProcessException) {
                    throw (CouldNotStartProcessException) e;
                } else if (e instanceof Error) {
                    throw (Error) e;
                } else {
                    throw new CouldNotStartProcessException("Could not successful start process. Output while waiting:\n" + startupListener.getRecordedContentWhileWaiting(), e);
                }
            }
        } catch (InterruptedException ignored) {
            currentThread().interrupt();
            shutdownImmediatelyAfterStart(requirement);
        }
    }

    protected void shutdownImmediatelyAfterStart(@Nonnull R requirement) {
        try {
            try {
                requirement.getStreamListener().notifyProcessTerminated(_process);
            } finally {
                requirement.getStartupListener().notifyProcessTerminated(_process);
            }
        } finally {
            closeQuietly(this);
        }
    }

    @Nonnull
    protected abstract GeneratedProcess generateProcess(@Nonnull ProcessRepository repository, @Nonnull R requirement) throws Exception;

    @Override
    public void close() {
        try {
            try {
                _process.shutdown();
                _process.waitFor();
            } catch (Exception e) {
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
                if (_temporaryDirectory != null) {
                    FileUtils.deleteDirectory(_temporaryDirectory);
                }
            } catch (Exception e) {
                LOG.warn("Could not delete the temporary directory '" + _temporaryDirectory + "' with the data values of the process. The files will remain on the disk and still will use disk space. You have to delete it manually.", e);
            } finally {
                _temporaryDirectory = null;
            }
        }
    }

    @Nonnull
    public File getExecutable() {
        return _process.getExecutable();
    }

    public long getPid() {
        return _process.getId();
    }

    @Nonnull
    public String[] getCommandLine() {
        return _process.getCommandLine();
    }

    public boolean isAlive() {
        return getProcess().isAlive();
    }

    @Nonnull
    protected GeneratedProcess getProcess() {
        return _process;
    }

    @Nonnull
    protected R getRequirement() {
        return _requirement;
    }

    @Nonnull
    protected File getBaseDirectoryOfEnvironmentVariable(@Nonnull String name) throws IOException {
        final String directoryPath = System.getenv(name);
        if (directoryPath == null) {
            throw new IllegalStateException("In the current environment is no " + name + " environment variable set.");
        }
        final File directory = new File(directoryPath);
        if (!directory.exists()) {
            throw new IllegalStateException("In the current environment points the " + name + " environment variable to " + directory + " but it does not exists.");
        }
        if (!directory.isDirectory()) {
            throw new IllegalStateException("In the current environment points the " + name + " environment variable to " + directory + " but it is not a directory.");
        }
        return directory.getCanonicalFile();
    }

    @Nonnull
    protected File getDirectoryOfEnvironmentVariable(@Nonnull String name, @Nonnull SubPath subPath) throws IOException {
        final File baseDirectory = getBaseDirectoryOfEnvironmentVariable(name);
        final File directory;
        if (subPath == inBaseDirectory) {
            directory = baseDirectory;
        } else {
            directory = new File(baseDirectory, subPath.getValue());
            if (!directory.exists()) {
                throw new IllegalStateException("In the current environment points the " + name + " environment variable to " + directory + " but it does not exists.");
            }
            if (!directory.isDirectory()) {
                throw new IllegalStateException("In the current environment points the " + name + " environment variable to " + directory + " but it is not a directory.");
            }
        }
        return directory.getCanonicalFile();
    }

    @Nonnull
    protected File getBinaryOfEnvironmentVariable(@Nonnull String environmentVariableName, @Nonnull SubPath subPath, @Nonnull String binaryFileName) throws IOException {
        final File directory = getDirectoryOfEnvironmentVariable(environmentVariableName, subPath);
        File binary = new File(directory, binaryFileName);
        if (!binary.canExecute()) {
            binary = new File(directory, binaryFileName + ".exe");
            if (!binary.canExecute()) {
                throw new IllegalStateException("In the current environment points the " + environmentVariableName + " environment variable to " + directory + " with no " + binaryFileName + " executable in it.");
            }
        }
        return binary;
    }

    @Nonnull
    protected File createTemporaryDirectory(@Nonnull String prefix, @Nonnull String suffix) throws IOException {
        if (_temporaryDirectory != null) {
            throw new IllegalStateException("There was already a temporary directory created for this process");
        }
        final File result = File.createTempFile(prefix, suffix);
        result.delete();
        if (!result.mkdir()) {
            throw new IOException("Could not create temporary directory: " + result);
        }
        result.deleteOnExit();
        _temporaryDirectory = result;
        return result;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{pid=" + getPid() + ", alive=" + isAlive() + "}";
    }

    private class OutputMonitor extends Thread {

        private final InputStream _stream;
        private final StreamType _streamType;

        private OutputMonitor(@Nonnull InputStream stream, @Nonnull StreamType streamType) {
            super("OutputMonitor:" + streamType);
            setDaemon(true);
            _stream = stream;
            _streamType = streamType;
        }

        @Override
        public void run() {
            final StreamListener streamListener = _requirement.getStreamListener();
            final StartupListener startupListener = _requirement.getStartupListener();
            final InputStreamReader reader = new InputStreamReader(_stream);
            final BufferedReader bufferedReader = new BufferedReader(reader);
            try {
                String line = bufferedReader.readLine();
                while (!Thread.currentThread().isInterrupted() && line != null) {
                    startupListener.notifyLineOutput(_process, line, _streamType);
                    streamListener.notifyLineOutput(_process, line, _streamType);
                    line = bufferedReader.readLine();
                }
            } catch (InterruptedIOException ignored) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                // noinspection InstanceofCatchParameter
                if (!(e instanceof IOException) || !"Stream closed".equals(e.getMessage())) {
                    LOG.warn("Could not read from " + _stream + ".", e);
                }
            } finally {
                if (!_process.isAlive()) {
                    streamListener.notifyProcessTerminated(_process);
                    startupListener.notifyProcessTerminated(_process);
                }
            }
        }

    }

}

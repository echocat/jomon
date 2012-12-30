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

package org.echocat.jomon.net.service;

import org.apache.commons.io.FileUtils;
import org.echocat.jomon.net.FreeTcpPortDetector;
import org.echocat.jomon.process.GeneratedProcess;
import org.echocat.jomon.process.ProcessRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.echocat.jomon.net.service.ApplicationDaemon.SubPath.inBaseDirectory;

public abstract class ApplicationDaemon implements Closeable {

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

    private final GeneratedProcess _process;
    private final ProcessMonitor _errorMonitor;
    private final ProcessMonitor _monitor;

    private File _temporaryDirectory;

    protected ApplicationDaemon() throws CouldNotStartProcessException {
        this(ProcessRepository.getInstance());
    }

    protected ApplicationDaemon(@Nonnull ProcessRepository processRepository) throws CouldNotStartProcessException {
        try {
            _process = createProcess(processRepository);
        } catch (Exception e) {
            throw new RuntimeException("Could not create process.", e);
        }
        _errorMonitor = new ProcessMonitor(_process.getErrorStream(), true);
        _errorMonitor.start();
        _monitor = new ProcessMonitor(_process.getInputStream(), false);
        _monitor.start();
        if (!_monitor.waitForSuccessfulStart()) {
            throw new CouldNotStartProcessException("Could not successful start process.\nOutput while waiting:\n" + _monitor.getContentWhileWaiting());
        }
    }

    @Nonnull
    protected abstract GeneratedProcess createProcess(@Nonnull ProcessRepository repository) throws Exception;
    @Nonnull
    protected abstract String getReadyMessage() throws Exception;

    @Override
    public void close() {
        try {
            try {
                _process.shutdown();
                _process.waitFor();
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                LOG.warn("Could not stop the mongod process. The process will still be on this computer. You have to stop it manually.", e);
            } finally {
                _errorMonitor.interrupt();
                _monitor.interrupt();
            }
        } finally {
            try {
                if (_temporaryDirectory != null) {
                    FileUtils.deleteDirectory(_temporaryDirectory);
                }
            } catch (Exception e) {
                LOG.warn("Could not delete the temporary directory '" + _temporaryDirectory + "' with the data values of the mongod process. The files will remain on the disk and still will use disk space. You have to delete it manually.", e);
            } finally {
                _temporaryDirectory = null;
            }
        }
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
        File mongodBinary = new File(directory, binaryFileName);
        if (!mongodBinary.canExecute()) {
            mongodBinary = new File(directory, binaryFileName + ".exe");
            if (!mongodBinary.canExecute()) {
                throw new IllegalStateException("In the current environment points the " + environmentVariableName + " environment variable to " + directory + " with no " + binaryFileName + " executable in it.");
            }
        }
        return mongodBinary;
    }

    @Nonnegative
    protected int findFreePort() throws UnknownHostException {
        final InetAddress localhost = InetAddress.getByName("localhost");
        final FreeTcpPortDetector freeTcpPortDetector = new FreeTcpPortDetector(localhost, 10000, 45000);
        return freeTcpPortDetector.detect();
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

    private class ProcessMonitor extends Thread {

        private final InputStream _stream;
        private final boolean _errorStream;
        private final StringBuilder _contentWhileWaiting = new StringBuilder();

        private final Lock _lock = new ReentrantLock();
        private final Condition _condition = _lock.newCondition();

        private volatile boolean _waitingForConnectionsSeen;
        private volatile Boolean _alive;

        private ProcessMonitor(@Nonnull InputStream stream, boolean errorStream) {
            super("ProcessMonitor");
            setDaemon(true);
            _stream = stream;
            _errorStream = errorStream;
        }

        @Override
        public void run() {
            final InputStreamReader reader = new InputStreamReader(_stream);
            final BufferedReader bufferedReader = new BufferedReader(reader);
            try {
                _alive = true;
                String line = bufferedReader.readLine();
                while (!Thread.currentThread().isInterrupted() && line != null) {
                    if (line.contains(getReadyMessage())) {
                        _lock.lock();
                        try {
                            _waitingForConnectionsSeen = true;
                            _condition.signalAll();
                        } finally {
                            _lock.unlock();
                        }
                    }
                    if (!_waitingForConnectionsSeen) {
                        if (_contentWhileWaiting.length() > 0) {
                            _contentWhileWaiting.append('\n');
                        }
                        _contentWhileWaiting.append(line);
                    }
                    if (_errorStream) {
                        LOG.warn(line);
                    } else {
                        LOG.info(line);
                    }
                    line = bufferedReader.readLine();
                }
            } catch (InterruptedIOException ignored) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                if (!(e instanceof IOException) || !"Stream closed".equals(e.getMessage())) {
                    LOG.warn("Could not read from " + _stream + ".", e);
                }
            } finally {
                _lock.lock();
                try {
                    _alive = false;
                    _condition.signalAll();
                } finally {
                    _lock.unlock();
                }
            }
        }

        private boolean waitForSuccessfulStart() {
            while ((_alive == null || _alive) && !_waitingForConnectionsSeen) {
                _lock.lock();
                try {
                    _condition.await(1, MINUTES);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                } finally {
                    _lock.unlock();
                }
            }
            return _waitingForConnectionsSeen;
        }

        @Nonnull
        public String getContentWhileWaiting() {
            return _contentWhileWaiting.toString();
        }
    }

    public static class CouldNotStartProcessException extends RuntimeException {

        public CouldNotStartProcessException() {}

        public CouldNotStartProcessException(String message) {
            super(message);
        }

        public CouldNotStartProcessException(String message, Throwable cause) {
            super(message, cause);
        }

        public CouldNotStartProcessException(Throwable cause) {
            super(cause);
        }
    }

}

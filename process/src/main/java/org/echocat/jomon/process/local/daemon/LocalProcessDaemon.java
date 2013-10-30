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

package org.echocat.jomon.process.local.daemon;

import org.apache.commons.io.FileUtils;
import org.echocat.jomon.process.CouldNotStartException;
import org.echocat.jomon.process.daemon.ProcessDaemon;
import org.echocat.jomon.process.local.LocalGeneratedProcess;
import org.echocat.jomon.process.local.LocalGeneratedProcessRequirement;
import org.echocat.jomon.process.local.LocalProcessRepository;
import org.echocat.jomon.runtime.generation.Generator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;

import static org.echocat.jomon.process.local.daemon.LocalProcessDaemon.SubPath.inBaseDirectory;

public abstract class LocalProcessDaemon<R extends LocalProcessDaemonRequirement<?>> extends ProcessDaemon<File, Long, LocalGeneratedProcess, R, Generator<LocalGeneratedProcess, LocalGeneratedProcessRequirement>> {

    private static final Logger LOG = LoggerFactory.getLogger(LocalProcessDaemon.class);

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

    @Nullable
    private File _temporaryDirectory;

    protected LocalProcessDaemon(@Nonnull R requirement) throws CouldNotStartException {
        this(LocalProcessRepository.getInstance(), requirement);
    }

    protected LocalProcessDaemon(@Nonnull LocalProcessRepository processRepository, @Nonnull R requirement) throws CouldNotStartException {
        super(processRepository, requirement);
    }

    @Override
    public void close() {
        try {
            super.close();
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

}

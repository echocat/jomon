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

package org.echocat.jomon.process.sigar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.*;
import java.lang.reflect.Method;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.lang.System.setProperty;
import static org.apache.commons.io.IOUtils.copy;
import static org.echocat.jomon.runtime.exceptions.ExceptionUtils.containsClassNotFoundException;

public class SigarFacadeFactory {

    private static final Logger LOG = LoggerFactory.getLogger(SigarFacadeFactory.class);
    private static final String SIGAR_VERSION = "1.6.4";
    private static final String BINARIES_PACKAGE_DIRECTORY_NAME = "sigarBinaries" + File.separator + SIGAR_VERSION;
    private static final String BINARIES_PACKAGE_FILE_NAME = "sigarBinaries-" + SIGAR_VERSION + ".zip";

    private static final boolean AVIALABLE;

    static {
        AVIALABLE = isAvialableInitialCheck();
    }

    /**
     * @throws UnsupportedOperationException if sigar is not available. Call {@link #isAvialable()} before.
     */
    @Nonnull
    public static SigarFacade create() throws UnsupportedOperationException {
        if (!isAvialable()) {
            throw new UnsupportedOperationException("Sigar is not available for this JVM.");
        }
        return new SigarFacade();
    }

    public static boolean isAvialable() {
        return AVIALABLE;
    }

    private static boolean isAvialableInitialCheck() {
        boolean result;
        try {
            initializeSigar();
            result = true;
        } catch (SigarBinariesException e) {
            result = false;
            LOG.info("The sigar process repository implementation is not available. Could not find the sigar binaries in the classpath or the provided sigar binaries are not compatible with your current JVM and/or operation system.", e);
        } catch (Exception e) {
            if (containsClassNotFoundException(e, "org.hyperic.sigar.")) {
                result = false;
                LOG.info("The sigar process repository implementation is not available. Could not find the sigar implementation in the classpath present. If you use Maven add the org.fusesource:sigar:1.6.4+ dependency. You can ignore this message if you do not want to use the sigar process implementation.");
            } else if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            } else {
                throw new SigarLoadException("Could not load sigar: " + e.getMessage(), e);
            }
        }
        return result;
    }

    private static void initializeSigar() throws Exception {
        final File directory = getDirectory();
        try {
            copyFilesTo(directory);
        } catch (IOException e) {
            throw new SigarBinariesException("Could not copy " + BINARIES_PACKAGE_FILE_NAME + " to " + directory + ".", e);
        }
        setLibraryPath(directory);
        final Class<?> clazz = Class.forName("org.hyperic.sigar.Sigar");
        final Method method = clazz.getMethod("load");
        method.invoke(null);
    }

    @Nonnull
    private static File getDirectory() {
        final String temporaryDirectoryPath = System.getProperty("java.io.tmpdir", "tmp");
        final File temporaryDirectory = new File(temporaryDirectoryPath);
        final File sigarDirectory = new File(temporaryDirectory, BINARIES_PACKAGE_DIRECTORY_NAME);
        sigarDirectory.mkdirs();
        return sigarDirectory;
    }

    private static void copyFilesTo(@Nonnull File directory) throws IOException {
        try (final InputStream is = SigarFacadeFactory.class.getResourceAsStream(BINARIES_PACKAGE_FILE_NAME)) {
            if (is == null) {
                throw new SigarBinariesException("Could not find file: " + BINARIES_PACKAGE_FILE_NAME);
            }
            try (final ZipInputStream zip = new ZipInputStream(is)) {
                ZipEntry entry = zip.getNextEntry();
                while (entry != null) {
                    if (!entry.isDirectory()) {
                        final File destinationFile = new File(directory, entry.getName());
                        if (!destinationFile.exists()) {
                            destinationFile.getParentFile().mkdirs();
                            try (final OutputStream os = new FileOutputStream(destinationFile)) {
                                copy(zip, os);
                            }
                        }
                    }
                    entry = zip.getNextEntry();
                }
            }
        }
    }

    private static void setLibraryPath(@Nonnull File directory) {
        setProperty("org.hyperic.sigar.path", directory.getPath());
    }

    private SigarFacadeFactory() {}

}

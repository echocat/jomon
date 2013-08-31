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

import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.lang.System.setProperty;
import static org.apache.commons.io.IOUtils.copy;

public class SigarInitializer {

    private static final String SIGAR_VERSION = "1.6.4";
    private static final String BINARIES_PACKAGE_DIRECTORY_NAME = "sigarBinaries" + File.separator + SIGAR_VERSION;
    private static final String BINARIES_PACKAGE_FILE_NAME = "sigarBinaries-" + SIGAR_VERSION + ".zip";

    private static boolean c_initialized;

    public static synchronized void initializeSigar() {
        if (!c_initialized) {
            final File directory = getDirectory();
            try {
                copyFilesTo(directory);
            } catch (IOException e) {
                throw new RuntimeException("Could not copy " + BINARIES_PACKAGE_FILE_NAME + " to " + directory + ".", e);
            }
            setLibraryPath(directory);
            try {
                Sigar.load();
            } catch (SigarException e) {
                throw new RuntimeException("Could not load sigar.", e);
            }
            c_initialized = true;
        }
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
        try (final InputStream is = SigarInitializer.class.getResourceAsStream(BINARIES_PACKAGE_FILE_NAME)) {
            if (is == null) {
                throw new IllegalArgumentException("Could not find file: " + BINARIES_PACKAGE_FILE_NAME);
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

    private SigarInitializer() {}

}

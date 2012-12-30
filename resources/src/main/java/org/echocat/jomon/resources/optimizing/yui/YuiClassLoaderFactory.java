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

package org.echocat.jomon.resources.optimizing.yui;

import javax.annotation.Nonnull;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;

import static java.lang.System.getProperty;
import static org.apache.commons.io.IOUtils.copy;

public class YuiClassLoaderFactory {

    private static final ClassLoader CLASS_LOADER = createClassLoader();

    @Nonnull
    public static ClassLoader getClassLoader() {
        return CLASS_LOADER;
    }

    @Nonnull
    private static ClassLoader createClassLoader() {
        return new URLClassLoader(new URL[]{
            createUrlCopyOf("js-1.7R2.jar"),
            createUrlCopyOf("slf4j-api-1.5.6.jar"),
            createUrlCopyOf("slf4j-jdk14-1.5.6.jar"),
            createUrlCopyOf("servlet-api-2.5.jar"),
            createUrlCopyOf("wro4j-core-1.4.5.jar"),
            createUrlCopyOf("wro4j-extensions-1.4.5.jar"),
        }, null);
    }

    @Nonnull
    private static URL createUrlCopyOf(@Nonnull String fileName) {
        try {
            final File file = getFileFor(fileName);
            try (final InputStream is = YuiClassLoaderFactory.class.getResourceAsStream(fileName)) {
                if (is == null) {
                    throw new IllegalArgumentException("Could not find resource named: " + fileName);
                }
                try (final OutputStream os = new FileOutputStream(file)) {
                    copy(is, os);
                }
            }
            return file.toURI().toURL();
        } catch (IOException e) {
            throw new RuntimeException("Could not create url copy of " + fileName + ".", e);
        }
    }

    @Nonnull
    private static File getFileFor(@Nonnull String fileName) {
        final File directory = getDirectory();
        directory.mkdirs();
        return new File(directory, fileName);
    }

    @Nonnull
    private static File getDirectory() {
        return new File(getProperty("java.io.tmdir", "tmp"), "yuicache");
    }

    private YuiClassLoaderFactory() {}

}

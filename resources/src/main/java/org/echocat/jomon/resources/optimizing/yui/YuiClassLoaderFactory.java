/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012-2014 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.resources.optimizing.yui;

import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.getProperty;
import static org.apache.commons.io.IOUtils.copy;

public class YuiClassLoaderFactory {

    private static final ClassLoader CLASS_LOADER = createClassLoader();

    @Nonnull
    public static ClassLoader yuiClassLoader() {
        return CLASS_LOADER;
    }

    @Nonnull
    private static ClassLoader createClassLoader() {
        return new OverwritingClassLoader(YuiClassLoaderFactory.class.getClassLoader(),
            createUrlCopyOf("yuicompressor-2.4.7.jar"),
            createUrlCopyOf("rhino-1.7R3.jar")
        ).including(ErrorReporterImpl.class);
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
        } catch (final IOException e) {
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

    protected static class OverwritingClassLoader extends URLClassLoader {

        @Nonnull
        private final Map<String, Class<?>> _nameToClass = new HashMap<>();
        @Nonnull
        private final ClassLoader _parent;

        public OverwritingClassLoader(@Nonnull ClassLoader parent, @Nonnull URL... urls) {
            super(urls, null);
            _parent = parent;
        }

        @Nonnull
        public OverwritingClassLoader including(@Nonnull Class<?> clazz) {
            _nameToClass.put(clazz.getName(), clazz);
            return this;
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            synchronized (getClassLoadingLock(name)) {
                Class<?> result = findLoadedClass(name);
                if (result == null) {
                    try {
                        result = findClass(name);
                    } catch (final ClassNotFoundException ignored) {
                        result = _parent.loadClass(name);
                    }
                }
                if (resolve) {
                    resolveClass(result);
                }
                return result;
            }
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            Class<?> result;
            try {
                result = super.findClass(name);
            } catch (final ClassNotFoundException ignored) {
                result = findClassInternal(name);
            }
            return result;
        }

        protected Class<?> findClassInternal(String name) throws ClassNotFoundException {
            final Class<?> clazz = _nameToClass.get(name);
            if (clazz == null) {
                throw new ClassNotFoundException();
            }
            try (final InputStream is = clazz.getClassLoader().getResourceAsStream(clazz.getName().replace('.', '/') + ".class")) {
                if (is == null) {
                    throw new ClassNotFoundException("Could not find original bytecode of " + clazz.getName() + " in classpath.");
                }
                final byte[] classAsBytes = IOUtils.toByteArray(is);
                return defineClass(name, classAsBytes, 0, classAsBytes.length);
            } catch (final IOException e) {
                throw new RuntimeException("Could not load content of class " + clazz.getName() + ".", e);
            }
        }

    }

    private YuiClassLoaderFactory() {}

}

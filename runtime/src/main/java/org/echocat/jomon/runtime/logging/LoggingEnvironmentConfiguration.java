/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012-2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.runtime.logging;

import org.echocat.jomon.runtime.generation.Requirement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.Charset;

import static java.lang.System.getProperty;
import static java.nio.charset.Charset.forName;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

public interface LoggingEnvironmentConfiguration extends Requirement {

    public static final Charset CHARSET = forName(getProperty(LoggingEnvironmentConfiguration.class.getName() + ".charset", "UTF-8"));

    @Nonnull
    public String getSourceName();

    public boolean isInstallSl4jRequired();

    public abstract static class Support<T extends Support<T>> implements LoggingEnvironmentConfiguration {

        private boolean _installSl4jRequired = true;

        @Nullable
        public Reader openAsReader() throws IOException, IllegalArgumentException {
            boolean success = false;
            final InputStream is = openAsInputStream();
            try {
                final Reader reader = new InputStreamReader(is, CHARSET);
                success = true;
                return reader;
            } finally {
                if (!success) {
                    closeQuietly(is);
                }
            }
        }

        @Nullable
        public abstract InputStream openAsInputStream() throws IOException, IllegalArgumentException;

        @Override
        public boolean isInstallSl4jRequired() {
            return _installSl4jRequired;
        }

        public void setInstallSl4jRequired(boolean installSl4jRequired) {
            _installSl4jRequired = installSl4jRequired;
        }

        @Nonnull
        public T whichInstallSl4j(boolean installSl4j) {
            setInstallSl4jRequired(installSl4j);
            return thisObject();
        }

        @Nonnull
        public T whichInstallSl4j() {
            return whichInstallSl4j(true);
        }

        @Nonnull
        public T whichNotInstallSl4j() {
            return whichInstallSl4j(false);
        }

        @Nonnull
        protected T thisObject() {
            // noinspection unchecked
            return (T) this;
        }
    }

    public abstract static class ForClassSupport<T extends ForClassSupport<T>> extends Support<T> {

        @Nonnull
        private final Class<?> _forClass;
        @Nonnull
        private final String _configurationFileName;

        public ForClassSupport(@Nonnull Class<?> forClass, @Nonnull String configurationFileName) {
            _forClass = forClass;
            _configurationFileName = configurationFileName;
        }

        @Nonnull
        @Override
        public InputStream openAsInputStream() throws IOException, IllegalArgumentException {
            final InputStream result = _forClass.getResourceAsStream(_configurationFileName);
            if (result == null) {
                throw new IllegalArgumentException("File '" + _configurationFileName + "' does not exist in relation to class '" + _forClass.getName() + "'.");
            }
            return result;
        }

        @Nonnull
        @Override
        public String getSourceName() {
            return _configurationFileName;
        }

        @Override
        public String toString() {
            return "Logging configuration '" + _configurationFileName + "' for class '" + _forClass.getName() + "'";
        }

    }

    public abstract static class ForClassLoaderSupport<T extends ForClassLoaderSupport<T>> extends Support<T> {

        @Nonnull
        private final ClassLoader _forLoader;
        @Nonnull
        private final String _configurationFileName;

        public ForClassLoaderSupport(@Nonnull ClassLoader forLoader, @Nonnull String configurationFileName) {
            _forLoader = forLoader;
            _configurationFileName = configurationFileName;
        }

        @Nonnull
        @Override
        public InputStream openAsInputStream() throws IOException, IllegalArgumentException {
            final InputStream result = _forLoader.getResourceAsStream(_configurationFileName);
            if (result == null) {
                throw new IllegalArgumentException("File '" + _configurationFileName + "' does not exist in classLoader '" + _forLoader + "'.");
            }
            return result;
        }

        @Nonnull
        @Override
        public String getSourceName() {
            return _configurationFileName;
        }

        @Override
        public String toString() {
            return "Logging configuration '" + _configurationFileName + "' for classLoader '" + _forLoader + "'";
        }

    }

    public abstract static class ForFileSupport<T extends ForFileSupport<T>> extends Support<T> {

        @Nonnull
        private final File _file;

        public ForFileSupport(@Nonnull File file) {
            _file = file;
        }

        @Nonnull
        @Override
        public InputStream openAsInputStream() throws IOException, IllegalArgumentException {
            try {
                return new FileInputStream(_file);
            } catch (final FileNotFoundException e) {
                throw new IllegalStateException("File '" + _file + "' does not exist.", e);
            }
        }

        @Nonnull
        @Override
        public String getSourceName() {
            return _file.getPath();
        }

        @Override
        public String toString() {
            return "Logging configuration '" + _file + "'";
        }

    }

}

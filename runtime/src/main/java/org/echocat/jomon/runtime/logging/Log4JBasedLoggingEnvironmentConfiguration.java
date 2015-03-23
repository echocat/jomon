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

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.Reader;

public interface Log4JBasedLoggingEnvironmentConfiguration extends LoggingEnvironmentConfiguration {

    public static String DEFAULT_CONFIGURATION_FILE = "org/echocat/jomon/runtime/logging/default.log4j.xml";

    @Nonnull
    public Reader openAsReader() throws IOException;

    public static class ForClass extends ForClassSupport<ForClass> implements Log4JBasedLoggingEnvironmentConfiguration {

        @Nonnull
        public static ForClass log4JBasedLoggingFor(@Nonnull Class<?> forClass) {
            return log4JBasedLoggingFor(forClass, "log4j.xml");
        }

        @Nonnull
        public static ForClass log4JBasedLoggingFor(@Nonnull Class<?> forClass, @Nonnull String xmlFileName) {
            return new ForClass(forClass, xmlFileName);
        }

        public ForClass(@Nonnull Class<?> forClass, @Nonnull String xmlFileName) {
            super(forClass, xmlFileName);
        }

    }

    public static class ForClassLoader extends ForClassLoaderSupport<ForClassLoader> implements Log4JBasedLoggingEnvironmentConfiguration {

        @Nonnull
        public static ForClassLoader log4JBasedLoggingFor(@Nonnull ClassLoader forLoader, @Nonnull String xmlFileName) {
            return new ForClassLoader(forLoader, xmlFileName);
        }

        @Nonnull
        public static ForClassLoader defaultLog4JBasedLogging() {
            return log4JBasedLoggingFor(Log4JBasedLoggingEnvironmentConfiguration.class.getClassLoader(), DEFAULT_CONFIGURATION_FILE);
        }

        public ForClassLoader(@Nonnull ClassLoader forLoader, @Nonnull String xmlFileName) {
            super(forLoader, xmlFileName);
        }

    }

    public static class ForFile extends ForFileSupport<ForFile> implements Log4JBasedLoggingEnvironmentConfiguration {

        @Nonnull
        public static ForFile log4JBasedLoggingFor(@Nonnull File file) {
            return new ForFile(file);
        }

        public ForFile(@Nonnull File file) {
            super(file);
        }

    }

}

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

public interface LogbackBasedLoggingEnvironmentConfiguration extends LoggingEnvironmentConfiguration {

    public static String DEFAULT_CONFIGURATION_FILE = "org/echocat/jomon/runtime/logging/default.logback.xml";

    @Nonnull
    public Reader openAsReader() throws IOException;

    public static class ForClass extends ForClassSupport<ForClass> implements LogbackBasedLoggingEnvironmentConfiguration {

        @Nonnull
        public static ForClass logbackBasedLoggingFor(@Nonnull Class<?> forClass) {
            return logbackBasedLoggingFor(forClass, "logback.xml");
        }

        @Nonnull
        public static ForClass logbackBasedLoggingFor(@Nonnull Class<?> forClass, @Nonnull String xmlFileName) {
            return new ForClass(forClass, xmlFileName);
        }

        public ForClass(@Nonnull Class<?> forClass, @Nonnull String xmlFileName) {
            super(forClass, xmlFileName);
        }

    }

    public static class ForClassLoader extends ForClassLoaderSupport<ForClassLoader> implements LogbackBasedLoggingEnvironmentConfiguration {

        @Nonnull
        public static ForClassLoader logbackBasedLoggingFor(@Nonnull ClassLoader forLoader, @Nonnull String xmlFileName) {
            return new ForClassLoader(forLoader, xmlFileName);
        }

        @Nonnull
        public static ForClassLoader defaultLogbackBasedLogging() {
            return logbackBasedLoggingFor(LogbackBasedLoggingEnvironmentConfiguration.class.getClassLoader(), DEFAULT_CONFIGURATION_FILE);
        }

        public ForClassLoader(@Nonnull ClassLoader forLoader, @Nonnull String xmlFileName) {
            super(forLoader, xmlFileName);
        }

    }

    public static class ForFile extends ForFileSupport<ForFile> implements LogbackBasedLoggingEnvironmentConfiguration {

        @Nonnull
        public static ForFile logbackBasedLoggingFor(@Nonnull File file) {
            return new ForFile(file);
        }

        public ForFile(@Nonnull File file) {
            super(file);
        }

    }

}

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

package org.echocat.jomon.runtime.logging;

import javax.annotation.Nonnull;

import static java.lang.Integer.compare;
import static java.lang.System.getProperty;
import static org.echocat.jomon.runtime.logging.LogLevel.Impl.logLevel;

@SuppressWarnings("ConstantNamingConvention")
public interface LogLevel extends Comparable<LogLevel> {

    public static final LogLevel trace = logLevel(LogLevel.class, 1000, "trace");
    public static final LogLevel debug = logLevel(LogLevel.class, 2000, "debug");
    public static final LogLevel info = logLevel(LogLevel.class, 3000, "info");
    public static final LogLevel warning = logLevel(LogLevel.class, 4000, "warning");
    public static final LogLevel error = logLevel(LogLevel.class, 5000, "error");
    public static final LogLevel fatal = logLevel(LogLevel.class, 6000, "fatal");

    public int getPriority();

    @Nonnull
    public String getName();

    public static class Impl implements LogLevel {

        @Nonnull
        protected static LogLevel logLevel(@Nonnull Class<?> clazz, int defaultPriority, @Nonnull String name) {
            return logLevel(clazz.getName() + ".", defaultPriority, name);
        }

        @Nonnull
        protected static LogLevel logLevel(@Nonnull String propertyNamePrefix, int defaultPriority, @Nonnull String name) {
            final String propertyName = propertyNamePrefix + name;
            final String plainPriority = getProperty(propertyName, Integer.toString(defaultPriority));
            final int priority;
            try {
                priority = plainPriority.isEmpty() ? defaultPriority : Integer.parseInt(plainPriority);
            } catch (final NumberFormatException e) {
                throw new IllegalArgumentException("System property '" + propertyName + "' contains in invalid priority value for logLevel '" + name + "'. Got: " + plainPriority, e);
            }
            return new Impl(priority, name);
        }

        @Nonnull
        protected static LogLevel logLevel(int priority, @Nonnull String name) {
            return new Impl(priority, name);
        }

        private final int _priority;
        @Nonnull
        private final String _name;

        public Impl(int priority, @Nonnull String name) {
            _priority = priority;
            _name = name;
        }

        @Override
        public int getPriority() {
            return _priority;
        }

        @Nonnull
        @Override
        public String getName() {
            return _name;
        }

        @Override
        public int compareTo(LogLevel that) {
            return compare(getPriority(), that.getPriority());
        }

        @Override
        public boolean equals(Object o) {
            final boolean result;
            if (this == o) {
                result = true;
            } else if (o == null || getClass() != o.getClass()) {
                result = false;
            } else {
                final LogLevel that = (LogLevel) o;
                result = getPriority() == that.getPriority();
            }
            return result;
        }

        @Override
        public int hashCode() {
            return getPriority();
        }

        @Override
        public String toString() {
            return getName();
        }

    }

}

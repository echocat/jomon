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

package org.echocat.jomon.runtime.logging;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.TreeSet;

import static java.lang.Integer.compare;
import static org.echocat.jomon.runtime.CollectionUtils.addAllAndMakeImmutable;

@SuppressWarnings("ConstantNamingConvention")
public interface LogLevel extends Comparable<LogLevel> {

    public static final LogLevel trace = new Impl(1000, "trace");
    public static final LogLevel debug = new Impl(2000, "debug");
    public static final LogLevel info = new Impl(3000, "info");
    public static final LogLevel warning = new Impl(4000, "warning");
    public static final LogLevel error = new Impl(5000, "error");
    public static final LogLevel fatal = new Impl(6000, "fatal");

    public static final Set<LogLevel> defaults = addAllAndMakeImmutable(new TreeSet<LogLevel>(), trace, debug, info, warning, error, fatal);

    public int getPriority();

    @Nonnull
    public String getName();

    public static class Impl implements LogLevel {

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

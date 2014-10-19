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

import org.echocat.jomon.runtime.CollectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static java.util.ServiceLoader.load;
import static org.echocat.jomon.runtime.CollectionUtils.*;

public class LogLevels {

    @Nonnull
    private static final Set<LogLevel> DEFAULTS = CollectionUtils.addAllAndMakeImmutable(new TreeSet<LogLevel>(), LogLevel.trace, LogLevel.debug, LogLevel.info, LogLevel.warning, LogLevel.error, LogLevel.fatal);
    @Nonnull
    private static final Set<LogLevel> ALL = loadLevels(DEFAULTS);
    @Nonnull
    private static final Map<String, LogLevel> NAME_TO_LEVEL = asNameToLevel(ALL);
    @Nonnull
    private static final Map<Integer, LogLevel> PRIORITY_TO_LEVEL = asPriorityToLevel(ALL);

    @Nonnull
    public static Set<LogLevel> defaults() {
        return DEFAULTS;
    }

    @Nonnull
    public static Set<LogLevel> all() {
        return ALL;
    }

    @Nonnull
    public static Set<LogLevel> defaultLogLevels() {
        return defaults();
    }

    @Nonnull
    public static Set<LogLevel> allLogLevels() {
        return all();
    }

    @Nullable
    public static LogLevel findLogLevelBy(@Nonnull String name) {
        return NAME_TO_LEVEL.get(name);
    }

    @Nonnull
    public static LogLevel getLogLevelBy(@Nonnull String name) throws IllegalArgumentException {
        final LogLevel logLevel = findLogLevelBy(name);
        if (logLevel == null) {
            throw new IllegalArgumentException("There is no logLevel named '" + name + "'.");
        }
        return logLevel;
    }

    @Nullable
    public static LogLevel findLogLevelBy(@Nonnull int priority) {
        return PRIORITY_TO_LEVEL.get(priority);
    }

    @Nonnull
    public static LogLevel getLogLevelBy(@Nonnull int priority) throws IllegalArgumentException {
        final LogLevel logLevel = findLogLevelBy(priority);
        if (logLevel == null) {
            throw new IllegalArgumentException("There is no logLevel with priority " + priority + ".");
        }
        return logLevel;
    }

    @Nonnull
    protected static Set<LogLevel> loadLevels(@Nullable Iterable<LogLevel> defaultLevels) {
        final Set<LogLevel> result = new TreeSet<>();
        addAll(result, defaultLevels);
        addAll(result, load(LogLevel.class));
        return asImmutableSet(result);
    }

    @Nonnull
    protected static Map<String, LogLevel> asNameToLevel(@Nonnull Iterable<LogLevel> logLevels) {
        final Map<String, LogLevel> result = new HashMap<>();
        for (final LogLevel logLevel : logLevels) {
            final String name = logLevel.getName();
            if (result.containsKey(name)) {
                throw new IllegalStateException("The name '" + name + "' was defined for more then one logLevels.");
            }
            result.put(name, logLevel);
        }
        return asImmutableMap(result);
    }

    @Nonnull
    protected static Map<Integer, LogLevel> asPriorityToLevel(@Nonnull Iterable<LogLevel> logLevels) {
        final Map<Integer, LogLevel> result = new HashMap<>();
        for (final LogLevel logLevel : logLevels) {
            final int priority = logLevel.getPriority();
            if (result.containsKey(priority)) {
                throw new IllegalStateException("The priority '" + priority + "' was defined for more then one logLevels.");
            }
            result.put(priority, logLevel);
        }
        return asImmutableMap(result);
    }

    private LogLevels() {}

}

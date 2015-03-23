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
import java.util.ArrayList;
import java.util.List;

import static java.util.ServiceLoader.load;
import static org.echocat.jomon.runtime.CollectionUtils.addAll;
import static org.echocat.jomon.runtime.CollectionUtils.asImmutableList;

public class LoggingEnvironmentGenerators {

    @Nonnull
    private static final Iterable<LoggingEnvironmentGenerator> ALL = loadAll();
    @Nonnull
    private static final LoggingEnvironmentGenerator DEFAULT = new Combined();

    private LoggingEnvironmentGenerators() {}

    @Nonnull
    public static LoggingEnvironmentGenerator getDefault() {
        return DEFAULT;
    }

    @Nonnull
    public static LoggingEnvironmentGenerator loggingEnvironmentGenerator() {
        return getDefault();
    }

    @Nonnull
    public static Iterable<LoggingEnvironmentGenerator> getAll() {
        return ALL;
    }

    @Nonnull
    private static Iterable<LoggingEnvironmentGenerator> loadAll() {
        final List<LoggingEnvironmentGenerator> result = new ArrayList<>();
        addAll(result, load(LoggingEnvironmentGenerator.class));
        return asImmutableList(result);
    }

    private static class Combined implements LoggingEnvironmentGenerator {

        @Nonnull
        @Override
        public LoggingEnvironment generate(@Nonnull LoggingEnvironmentConfiguration requirement) {
            LoggingEnvironment result = null;
            for (final LoggingEnvironmentGenerator generator : ALL) {
                if (generator.supports(requirement)) {
                    result = generator.generate(requirement);
                }
            }
            if (result == null) {
                throw new IllegalArgumentException("Could not generate environment for " + requirement + ".");
            }
            return result;
        }

        @Override
        public boolean supports(@Nonnull LoggingEnvironmentConfiguration configuration) {
            boolean result = false;
            for (final LoggingEnvironmentGenerator generator : ALL) {
                if (generator.supports(configuration)) {
                    result = true;
                    break;
                }
            }
            return result;
        }

    }

}

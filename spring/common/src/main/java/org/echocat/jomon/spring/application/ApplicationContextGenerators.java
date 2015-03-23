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

package org.echocat.jomon.spring.application;

import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static java.util.ServiceLoader.load;
import static org.echocat.jomon.runtime.CollectionUtils.addAll;
import static org.echocat.jomon.runtime.CollectionUtils.asImmutableList;

public class ApplicationContextGenerators {

    @Nonnull
    private static final Iterable<ApplicationContextGenerator> ALL = loadAll();
    @Nonnull
    private static final ApplicationContextGenerator DEFAULT = new Combined();

    private ApplicationContextGenerators() {}

    @Nonnull
    public static ApplicationContextGenerator getDefault() {
        return DEFAULT;
    }

    @Nonnull
    public static ApplicationContextGenerator applicationContextGenerator() {
        return getDefault();
    }

    @Nonnull
    public static Iterable<ApplicationContextGenerator> getAll() {
        return ALL;
    }

    @Nonnull
    private static Iterable<ApplicationContextGenerator> loadAll() {
        final List<ApplicationContextGenerator> result = new ArrayList<>();
        addAll(result, load(ApplicationContextGenerator.class));
        return asImmutableList(result);
    }

    private static class Combined implements ApplicationContextGenerator {

        @Nonnull
        @Override
        public ConfigurableApplicationContext generate(@Nonnull ApplicationContextRequirement requirement) {
            ConfigurableApplicationContext result = null;
            for (final ApplicationContextGenerator generator : ALL) {
                if (generator.supports(requirement)) {
                    result = generator.generate(requirement);
                }
            }
            if (result == null) {
                throw new IllegalArgumentException("Could not generate context for " + requirement + ".");
            }
            return result;
        }

        @Override
        public boolean supports(@Nonnull ApplicationContextRequirement configuration) {
            boolean result = false;
            for (final ApplicationContextGenerator generator : ALL) {
                if (generator.supports(configuration)) {
                    result = true;
                    break;
                }
            }
            return result;
        }

    }

}

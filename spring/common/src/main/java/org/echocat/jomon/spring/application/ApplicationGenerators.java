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

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static java.util.ServiceLoader.load;
import static org.echocat.jomon.runtime.CollectionUtils.addAll;
import static org.echocat.jomon.runtime.CollectionUtils.asImmutableList;

public class ApplicationGenerators {

    @Nonnull
    private static final Iterable<ApplicationGenerator> ALL = loadAll();
    @Nonnull
    private static final ApplicationGenerator DEFAULT = new Combined();

    private ApplicationGenerators() {}

    @Nonnull
    public static ApplicationGenerator getDefault() {
        return DEFAULT;
    }

    @Nonnull
    public static ApplicationGenerator applicationGenerator() {
        return getDefault();
    }

    @Nonnull
    public static Iterable<ApplicationGenerator> getAll() {
        return ALL;
    }

    @Nonnull
    private static Iterable<ApplicationGenerator> loadAll() {
        final List<ApplicationGenerator> result = new ArrayList<>();
        addAll(result, load(ApplicationGenerator.class));
        result.add(new DefaultApplicationGenerator());
        return asImmutableList(result);
    }

    private static class Combined implements ApplicationGenerator {

        @Nonnull
        @Override
        public Application generate(@Nonnull ApplicationRequirement requirement) {
            Application result = null;
            for (final ApplicationGenerator generator : ALL) {
                if (generator.supports(requirement)) {
                    result = generator.generate(requirement);
                }
            }
            if (result == null) {
                throw new IllegalArgumentException("Could not generate application for " + requirement + ".");
            }
            return result;
        }

        @Override
        public boolean supports(@Nonnull ApplicationRequirement requirement) {
            boolean result = false;
            for (final ApplicationGenerator generator : ALL) {
                if (generator.supports(requirement)) {
                    result = true;
                    break;
                }
            }
            return result;
        }

    }

}

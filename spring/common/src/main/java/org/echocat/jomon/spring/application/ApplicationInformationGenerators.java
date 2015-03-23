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

package org.echocat.jomon.spring.application;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static java.util.ServiceLoader.load;
import static org.echocat.jomon.runtime.CollectionUtils.addAll;
import static org.echocat.jomon.runtime.CollectionUtils.asImmutableList;

public class ApplicationInformationGenerators {

    @Nonnull
    private static final Iterable<ApplicationInformationGenerator> ALL = loadAll();
    @Nonnull
    private static final ApplicationInformationGenerator DEFAULT = new Combined();

    private ApplicationInformationGenerators() {}

    @Nonnull
    public static ApplicationInformationGenerator getDefault() {
        return DEFAULT;
    }

    @Nonnull
    public static ApplicationInformationGenerator applicationInformationGenerator() {
        return getDefault();
    }

    @Nonnull
    public static Iterable<ApplicationInformationGenerator> getAll() {
        return ALL;
    }

    @Nonnull
    private static Iterable<ApplicationInformationGenerator> loadAll() {
        final List<ApplicationInformationGenerator> result = new ArrayList<>();
        addAll(result, load(ApplicationInformationGenerator.class));
        return asImmutableList(result);
    }

    private static class Combined implements ApplicationInformationGenerator {

        @Nonnull
        @Override
        public ApplicationInformation generate(@Nonnull ApplicationInformationRequirement requirement) {
            ApplicationInformation result = null;
            for (final ApplicationInformationGenerator generator : ALL) {
                if (generator.supports(requirement)) {
                    result = generator.generate(requirement);
                }
            }
            if (result == null) {
                result = new FallbackInformation();
            }
            return result;
        }

        @Override
        public boolean supports(@Nonnull ApplicationInformationRequirement requirement) {
            return true;
        }

    }

    public static class FallbackInformation implements ApplicationInformation {

        @Nullable
        @Override
        public String getBanner() {
            return null;
        }

        @Nonnull
        @Override
        public String getTitle() {
            return Application.class.getName();
        }

        @Nullable
        @Override
        public String getName() {
            return null;
        }

        @Nullable
        @Override
        public String getVersion() {
            return null;
        }

        @Nullable
        @Override
        public String getBuildRevision() {
            return null;
        }

        @Nullable
        @Override
        public String getBuildDate() {
            return null;
        }
    }



}

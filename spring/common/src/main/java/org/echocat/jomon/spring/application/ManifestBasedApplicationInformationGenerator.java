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

import org.apache.commons.io.IOUtils;
import org.echocat.jomon.runtime.ManifestInformationFactory;
import org.echocat.jomon.spring.application.ApplicationInformationGenerators.FallbackInformation;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class ManifestBasedApplicationInformationGenerator implements ApplicationInformationGenerator {

    @Override
    public boolean supports(@Nonnull ApplicationInformationRequirement requirement) {
        return tryGenerateFor(requirement.getApplicationContextRequirement()) != null;
    }

    @Nonnull
    @Override
    public ApplicationInformation generate(@Nonnull ApplicationInformationRequirement requirement) {
        final ApplicationInformation information = tryGenerateFor(requirement.getApplicationContextRequirement());
        return information != null ? information : new FallbackInformation();
    }

    @Nullable
    protected ApplicationInformation tryGenerateFor(@Nonnull ApplicationContextRequirement applicationContextRequirement) {
        final ApplicationInformation result;
        if (applicationContextRequirement instanceof XmlBasedApplicationContextRequirement) {
            result = tryGenerateFor((XmlBasedApplicationContextRequirement) applicationContextRequirement);
        } else if (applicationContextRequirement instanceof JavaBasedApplicationContextRequirement) {
            result = generateFor((JavaBasedApplicationContextRequirement) applicationContextRequirement);
        } else {
            result = null;
        }
        return result;
    }

    @Nullable
    protected ApplicationInformation tryGenerateFor(@Nonnull XmlBasedApplicationContextRequirement configuration) {
        ApplicationInformation result = null;
        for (final Resource resource : configuration.getConfigurationFiles()) {
            if (resource instanceof ClassPathResource) {
                final ClassLoader classLoader = ((ClassPathResource) resource).getClassLoader();
                final String path = ((ClassPathResource) resource).getPath();
                result = tryGenerateFor(classLoader, path);
                if (result != null) {
                    break;
                }
            }
        }
        return result;
    }

    @Nonnull
    protected ApplicationInformation generateFor(@Nonnull JavaBasedApplicationContextRequirement configuration) {
        ApplicationInformation result = null;
        for (final Class<?> aClass : configuration.getClasses()) {
            result = tryGenerateFor(aClass);
            if (result != null) {
                break;
            }
        }
        return result;
    }

    @Nullable
    protected ApplicationInformation tryGenerateFor(@Nonnull ClassLoader classLoader, @Nonnull String referenceResource) {
        final ManifestInformationFactory factory = new ManifestInformationFactory(referenceResource, classLoader);
        return isEmpty(factory.getImplementationTitle()) ? null : new ManifestInformation(factory);
    }

    @Nullable
    protected ApplicationInformation tryGenerateFor(@Nonnull Class<?> type) {
        final ManifestInformationFactory factory = new ManifestInformationFactory(type);
        return isEmpty(factory.getImplementationTitle()) ? null : new ManifestInformation(factory);
    }

    protected static class ManifestInformation implements ApplicationInformation {

        @Nonnull
        private final ManifestInformationFactory _factory;

        public ManifestInformation(@Nonnull ManifestInformationFactory factory) {
            _factory = factory;
        }

        @Nullable
        @Override
        public String getBanner() {
            final String bannerFile = _factory.getImplementationBannerFile();
            final String result;
            if (!isEmpty(bannerFile)) {
                try (final InputStream is = _factory.getClassLoader().getResourceAsStream(bannerFile)) {
                    if (is != null) {
                        result = IOUtils.toString(is);
                    } else {
                        result = null;
                    }
                } catch (final IOException e) {
                    throw new RuntimeException("Could not read banner file '" + bannerFile + "'.", e);
                }
            } else {
                result = null;
            }
            return result;
        }

        @Nonnull
        @Override
        public String getTitle() {
            return _factory.getApplicationInfoString();
        }

        @Nullable
        @Override
        public String getName() {
            return _factory.getImplementationTitle();
        }

        @Nullable
        @Override
        public String getVersion() {
            return _factory.getImplementationVersion();
        }

        @Nullable
        @Override
        public String getBuildRevision() {
            return _factory.getImplementationBuildRevision();
        }

        @Nullable
        @Override
        public String getBuildDate() {
            return _factory.getImplementationBuildDate();
        }
    }


}

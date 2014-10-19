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

package org.echocat.jomon.spring;

import org.echocat.jomon.runtime.ManifestInformationFactory;
import org.echocat.jomon.runtime.concurrent.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import static java.lang.Thread.currentThread;

@ThreadSafe
public class DefaultApplicationGenerator implements ApplicationGenerator {

    @Nonnull
    private static final Logger LOG = LoggerFactory.getLogger(DefaultApplicationGenerator.class);

    @Nonnull
    private static final DefaultApplicationGenerator INSTANCE = new DefaultApplicationGenerator();

    @Nonnull
    public static DefaultApplicationGenerator getInstance() {
        return INSTANCE;
    }

    @Nonnull
    @Override
    public Application generate(@Nonnull ApplicationRequirement requirement) {
        final String title = getTitleFor(requirement);
        final StopWatch stopWatch = new StopWatch();
        LOG.info("Starting " + title + "...");
        final ConfigurableApplicationContext applicationContext = requirement.getApplicationContextGenerator().generate(requirement);
        boolean success = false;
        boolean applicationContextStarted = false;
        try {
            final Application application = new DefaultApplication(applicationContext, title);
            applicationContext.refresh();
            applicationContextStarted = true;
            waitForContextLoadThreads(applicationContext);
            LOG.info("Starting " + title + "... DONE! (after: " + stopWatch + ")");
            success = true;
            return application;
        } finally {
            if (applicationContextStarted && !success) {
                applicationContext.close();
            }
        }
    }

    protected void waitForContextLoadThreads(@Nonnull ConfigurableApplicationContext applicationContext) {
        try {
            for (final ContextLoadThreadGroup contextLoadThreadGroup : applicationContext.getBeansOfType(ContextLoadThreadGroup.class).values()) {
                contextLoadThreadGroup.join();
            }
        } catch (final InterruptedException ignored) {
            currentThread().interrupt();
        }
    }

    @Nonnull
    protected String getTitleFor(@Nonnull ApplicationRequirement requirement) {
        final ManifestInformationFactory manifestInformationFactory = new ManifestInformationFactory(requirement.getBeanXmlInClassPath(), requirement.getClassLoader());
        return getTitleFor(requirement, manifestInformationFactory);
    }

    @Nonnull
    protected static String getTitleFor(@Nonnull ApplicationRequirement requirement, @Nonnull ManifestInformationFactory manifestInformationFactory) {
        final String title = manifestInformationFactory.getApplicationInfoString();
        return title != null ? title : requirement.getDefaultTitle();
    }

}

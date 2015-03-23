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

import org.echocat.jomon.runtime.concurrent.StopWatch;
import org.echocat.jomon.runtime.logging.LoggingEnvironment;
import org.echocat.jomon.runtime.logging.LoggingEnvironmentGenerator;
import org.echocat.jomon.runtime.logging.LoggingEnvironmentGenerators;
import org.echocat.jomon.spring.ContextLoadThreadGroup;
import org.slf4j.Logger;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.echocat.jomon.spring.application.ApplicationContextGenerators.applicationContextGenerator;
import static org.echocat.jomon.spring.application.ApplicationInformationGenerators.applicationInformationGenerator;
import static org.echocat.jomon.spring.application.ApplicationInformationRequirement.applicationInformationRequirementFor;

@ThreadSafe
public class DefaultApplicationGenerator implements ApplicationGenerator {


    @Nonnull
    private final ApplicationContextGenerator _applicationContextGenerator;
    @Nonnull
    private final ApplicationInformationGenerator _applicationInformationGenerator;
    @Nonnull
    private final LoggingEnvironmentGenerator _loggingEnvironmentGenerator;

    public DefaultApplicationGenerator() {
        this(applicationContextGenerator(), applicationInformationGenerator(), LoggingEnvironmentGenerators.loggingEnvironmentGenerator());
    }

    public DefaultApplicationGenerator(@Nonnull ApplicationContextGenerator applicationContextGenerator, @Nonnull ApplicationInformationGenerator applicationInformationGenerator, @Nonnull LoggingEnvironmentGenerator loggingEnvironmentGenerator) {
        _applicationContextGenerator = applicationContextGenerator;
        _applicationInformationGenerator = applicationInformationGenerator;
        _loggingEnvironmentGenerator = loggingEnvironmentGenerator;
    }

    @Override
    public boolean supports(@Nonnull ApplicationRequirement requirement) {
        return true;
    }

    @Nonnull
    @Override
    public Application generate(@Nonnull ApplicationRequirement requirement) {
        final ApplicationInformation information = generateApplicationInformationFor(requirement);
        final LoggingEnvironment loggingEnvironment = generateLoggingEnvironmentFor(requirement);
        final StopWatch stopWatch = new StopWatch();
        displayBannerIfNeeded(information);
        final Logger logger = loggingEnvironment.getLogger(DefaultApplicationGenerator.class);
        logger.info("Starting " + information.getTitle() + "...");
        final ConfigurableApplicationContext applicationContext = getApplicationContextGenerator().generate(requirement.getApplicationContextRequirement());
        boolean success = false;
        boolean applicationContextStarted = false;
        try {
            final Application application = new DefaultApplication(applicationContext, information, loggingEnvironment);
            application.init();
            applicationContextStarted = true;
            waitForContextLoadThreads(applicationContext);
            logger.info("Starting " + information.getTitle() + "... DONE! (after: " + stopWatch.toCurrentPattern(MILLISECONDS) + ")");
            notifyApplicationSuccessfulInitiated(application);
            success = true;
            return application;
        } finally {
            if (applicationContextStarted && !success) {
                applicationContext.close();
            }
        }
    }

    protected void displayBannerIfNeeded(@Nonnull ApplicationInformation information) {
        final String banner = information.getBanner();
        if (!isEmpty(banner)) {
            // noinspection UseOfSystemOutOrSystemErr
            System.out.println(banner);
        }
    }

    protected void notifyApplicationSuccessfulInitiated(@Nonnull Application application) {
        for (final ApplicationSuccessfulInitiatedReceiver receiver : application.getApplicationContext().getBeansOfType(ApplicationSuccessfulInitiatedReceiver.class).values()) {
            receiver.onApplicationSuccessfulInitiated(application);
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
    protected ApplicationInformation generateApplicationInformationFor(@Nonnull ApplicationRequirement requirement) {
        final ApplicationInformation original = getApplicationInformationGenerator().generate(applicationInformationRequirementFor(requirement.getApplicationContextRequirement()));
        final ApplicationInformation result;
        final String requirementTitle = requirement.getTitle();
        if (!isEmpty(requirementTitle)) {
            result = new ApplicationInformationReImpl(original, requirementTitle);
        } else {
            result = original;
        }
        return result;
    }

    @Nonnull
    protected LoggingEnvironment generateLoggingEnvironmentFor(@Nonnull ApplicationRequirement requirement) {
        return getLoggingEnvironmentGenerator().generate(requirement.getLoggingEnvironmentConfiguration());
    }

    @Nonnull
    public ApplicationContextGenerator getApplicationContextGenerator() {
        return _applicationContextGenerator;
    }

    @Nonnull
    public ApplicationInformationGenerator getApplicationInformationGenerator() {
        return _applicationInformationGenerator;
    }

    @Nonnull
    public LoggingEnvironmentGenerator getLoggingEnvironmentGenerator() {
        return _loggingEnvironmentGenerator;
    }

    protected static class ApplicationInformationReImpl implements ApplicationInformation {

        @Nonnull
        private final ApplicationInformation _delegate;
        @Nonnull
        private final String _title;

        public ApplicationInformationReImpl(@Nonnull ApplicationInformation delegate, @Nonnull String title) {
            _delegate = delegate;
            _title = title;
        }

        @Nullable
        @Override
        public String getBanner() {
            return _delegate.getBanner();
        }

        @Override
        @Nonnull
        public String getTitle() {return _title; }

        @Override
        @Nullable
        public String getName() {return _delegate.getName();}

        @Override
        @Nullable
        public String getVersion() {return _delegate.getVersion();}

        @Override
        @Nullable
        public String getBuildRevision() {return _delegate.getBuildRevision();}

        @Override
        @Nullable
        public String getBuildDate() {return _delegate.getBuildDate();}
    }

}

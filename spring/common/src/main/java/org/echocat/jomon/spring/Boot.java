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

import org.echocat.jomon.spring.application.Application;
import org.echocat.jomon.spring.application.ApplicationRequirement;
import org.echocat.jomon.spring.application.DefaultApplicationRequirement;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.echocat.jomon.runtime.io.UrlUtils.registerUrlStreamHandlerIfNeeded;
import static org.echocat.jomon.spring.application.ApplicationGenerators.applicationGenerator;
import static org.echocat.jomon.spring.application.DefaultApplicationRequirement.xmlConfiguredApplicationFor;

public class Boot {

    static {
        registerUrlStreamHandlerIfNeeded();
    }

    @Nullable
    private static Application c_application;

    public static void main(String[] args) {
        if (args == null || (args.length != 1 && args.length != 2)) {
            //noinspection UseOfSystemOutOrSystemErr
            System.err.println("java -cp <?> " + Boot.class.getName() + " <boot bean.xml or configuration class name in classpath> [log4j configuration in classpath]");
            //noinspection CallToSystemExit
            System.exit(1);
        } else {
            final DefaultApplicationRequirement requirement = xmlConfiguredApplicationFor(Boot.class.getClassLoader(), args[0]);
            if (args.length > 1) {
                requirement.withLog4jFor(Boot.class.getClassLoader(), args[1]);
            }
            start(requirement);
        }
    }

    @Nonnull
    public static Application start(@Nonnull ApplicationRequirement requirement) {
        return startApplication(requirement);
    }

    @Nonnull
    public static Application startApplication(@Nonnull ApplicationRequirement requirement) {
        final Application application = applicationGenerator().generate(requirement);
        c_application = application;
        return application;
    }

    /**
     * ====== DEPRECATED METHODS BELOW =========================================================================================================================
     */

    /**
     * @deprecated Use {@link Application#getInformation()}.{@link org.echocat.jomon.spring.application.ApplicationInformation#getTitle() getTitle()} instead.
     */
    @Deprecated
    @Nonnull
    public static String getApplicationName() {
        final Application application = c_application;
        final String result;
        if (application != null) {
            result = application.getInformation().getTitle();
        } else {
            result = Boot.class.getName();
        }
        return result;
    }

    /**
     * @deprecated Use {@link #start(ApplicationRequirement)} instead.
     */
    @Deprecated
    @Nonnull
    public static ConfigurableApplicationContext start(@Nonnull Class<?> reference, @Nonnull String bootBeanXmlFileName) {
        return (ConfigurableApplicationContext) start(xmlConfiguredApplicationFor(reference, bootBeanXmlFileName)
        ).getApplicationContext();
    }

    /**
     * @deprecated Use {@link #start(ApplicationRequirement)} instead.
     */
    @Deprecated
    @Nonnull
    public static ConfigurableApplicationContext start(@Nonnull Class<?> reference, @Nonnull String bootBeanXmlFileName, @Nullable String log4jConfigurationFileName) {
        return (ConfigurableApplicationContext) start(xmlConfiguredApplicationFor(reference, bootBeanXmlFileName)
            .withLog4jFor(reference, log4jConfigurationFileName)
        ).getApplicationContext();
    }

    /**
     * @deprecated Use {@link #start(ApplicationRequirement)} instead.
     */
    @Deprecated
    @Nonnull
    public static ConfigurableApplicationContext start(@Nonnull String bootBeanXmlInClassPath) {
        return (ConfigurableApplicationContext) start(xmlConfiguredApplicationFor(Boot.class.getClassLoader(), bootBeanXmlInClassPath)
        ).getApplicationContext();
    }

    /**
     * @deprecated Use {@link #start(ApplicationRequirement)} instead.
     */
    @Deprecated
    @Nonnull
    public static ConfigurableApplicationContext start(@Nonnull String bootBeanXmlInClassPath, @Nullable String log4jConfigurationInClassPath) {
        return (ConfigurableApplicationContext) start(xmlConfiguredApplicationFor(Boot.class.getClassLoader(), bootBeanXmlInClassPath)
            .withLog4jFor(Boot.class.getClassLoader(), log4jConfigurationInClassPath)
        ).getApplicationContext();
    }

    /**
     * @deprecated Use {@link #startApplication(ApplicationRequirement)} instead.
     */
    @Deprecated
    @Nonnull
    public static ConfigurableApplicationContext startComponent(@Nonnull String bootBeanXmlInClassPath, @Nonnull String applicationName) {
        return (ConfigurableApplicationContext) start(xmlConfiguredApplicationFor(Boot.class.getClassLoader(), bootBeanXmlInClassPath)
            .withApplicationTitle(applicationName)
        ).getApplicationContext();
    }

    private Boot() {}
}

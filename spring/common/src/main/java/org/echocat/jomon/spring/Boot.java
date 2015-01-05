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
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.echocat.jomon.runtime.io.UrlUtils.registerUrlStreamHandlerIfNeeded;
import static org.echocat.jomon.runtime.logging.Log4JUtils.configureRuntime;
import static org.echocat.jomon.spring.ApplicationGenerators.applicationGenerator;
import static org.echocat.jomon.spring.ClassBasedApplicationRequirement.applicationBasedOn;
import static org.echocat.jomon.spring.DefaultApplicationGenerator.getTitleFor;
import static org.echocat.jomon.spring.PathBasedApplicationRequirement.applicationFor;

public class Boot {

    static {
        registerUrlStreamHandlerIfNeeded();
    }

    @Nonnull
    private static ManifestInformationFactory c_informationFactory = new ManifestInformationFactory(Boot.class);
    @Nonnull
    private static String c_applicationName = Boot.class.getName();

    public static void main(String[] args) {
        if (args == null || (args.length != 1 && args.length != 2)) {
            //noinspection UseOfSystemOutOrSystemErr
            System.err.println("java -cp <?> " + Boot.class.getName() + " <boot bean.xml in classpath> [log4j configuration in classpath]");
            //noinspection CallToSystemExit
            System.exit(1);
        } else {
            final PathBasedApplicationRequirement requirement = applicationFor(Boot.class.getClassLoader(), args[0]);
            if (args.length > 1) {
                requirement.setLog4jConfigurationInClassPath(args[1]);
            }
            start(requirement);
        }
    }

    @Nonnull
    public static Application start(@Nonnull ApplicationRequirement requirement) {
        configureInformationFactory(requirement);
        configureLog4j(requirement);
        return startApplication(requirement);
    }

    @Nonnull
    public static Application startApplication(@Nonnull ApplicationRequirement requirement) {
        return applicationGenerator().generate(requirement);
    }

    @Nonnull
    public static ManifestInformationFactory getInformationFactory() {
        return c_informationFactory;
    }

    /**
     * @deprecated Use {@link Application#getTitle()} instead.
     */
    @Deprecated
    @Nonnull
    public static String getApplicationName() {
        return c_applicationName;
    }

    protected static void configureInformationFactory(@Nonnull ApplicationRequirement requirement) {
        c_informationFactory = new ManifestInformationFactory(requirement.getBeanXmlInClassPath(), requirement.getClassLoader());
        c_applicationName = getTitleFor(requirement, c_informationFactory);
    }

    protected static void configureLog4j(@Nonnull ApplicationRequirement requirement) {
        final String log4jConfigurationInClassPath = requirement.getLog4jConfigurationInClassPath();
        final ClassLoader classLoader = requirement.getClassLoader();
        configureRuntime(log4jConfigurationInClassPath != null ? classLoader.getResource(log4jConfigurationInClassPath) : Boot.class.getResource("default.log4j.xml"));
    }

    /**
     * @deprecated Use {@link #start(ApplicationRequirement)} instead.
     */
    @Deprecated
    @Nonnull
    public static ConfigurableApplicationContext start(@Nonnull Class<?> reference, @Nonnull String bootBeanXmlFileName) {
        return start(applicationBasedOn(reference)
            .withBeanXmlFileName(bootBeanXmlFileName)
        ).getApplicationContext();
    }

    /**
     * @deprecated Use {@link #start(ApplicationRequirement)} instead.
     */
    @Deprecated
    @Nonnull
    public static ConfigurableApplicationContext start(@Nonnull Class<?> reference, @Nonnull String bootBeanXmlFileName, @Nullable String log4jConfigurationFileName) {
        return start(applicationBasedOn(reference)
            .withBeanXmlFileName(bootBeanXmlFileName)
            .withLog4jConfigurationFileName(log4jConfigurationFileName)
        ).getApplicationContext();
    }

    /**
     * @deprecated Use {@link #start(ApplicationRequirement)} instead.
     */
    @Deprecated
    @Nonnull
    public static ConfigurableApplicationContext start(@Nonnull String bootBeanXmlInClassPath) {
        return start(applicationFor(Boot.class.getClassLoader(), bootBeanXmlInClassPath)).getApplicationContext();
    }

    /**
     * @deprecated Use {@link #start(ApplicationRequirement)} instead.
     */
    @Deprecated
    @Nonnull
    public static ConfigurableApplicationContext start(@Nonnull String bootBeanXmlInClassPath, @Nullable String log4jConfigurationInClassPath) {
        return start(applicationFor(Boot.class.getClassLoader(), bootBeanXmlInClassPath)
            .withLog4jConfigurationInClassPath(log4jConfigurationInClassPath)
        ).getApplicationContext();
    }

    /**
     * @deprecated Use {@link #startApplication(ApplicationRequirement)} instead.
     */
    @Deprecated
    @Nonnull
    public static ConfigurableApplicationContext startComponent(@Nonnull String bootBeanXmlInClassPath, @Nonnull String applicationName) {
        return start(applicationFor(Boot.class.getClassLoader(), bootBeanXmlInClassPath)
            .withDefaultApplicationName(applicationName)
        ).getApplicationContext();
    }

    /**
     * @deprecated Use {@link #startApplication(ApplicationRequirement)} instead.
     */
    @Deprecated
    @Nonnull
    public static ConfigurableApplicationContext startComponent(@Nullable ApplicationContext parent, @Nonnull String bootBeanXmlInClassPath, @Nonnull final String applicationName) {
        return start(applicationFor(Boot.class.getClassLoader(), bootBeanXmlInClassPath)
            .withDefaultApplicationName(applicationName)
            .withParentApplicationContext(parent)
        ).getApplicationContext();
    }

    private Boot() {}
}

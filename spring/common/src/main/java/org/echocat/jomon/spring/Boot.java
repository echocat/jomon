/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012-2013 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.spring;

import org.echocat.jomon.runtime.Log4JUtils;
import org.echocat.jomon.runtime.ManifestInformationFactory;
import org.echocat.jomon.runtime.concurrent.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.lang.reflect.Field;

import static java.lang.Thread.currentThread;
import static org.echocat.jomon.runtime.io.UrlUtils.registerUrlStreamHandlerIfNeeded;

public class Boot {

    static {
        registerUrlStreamHandlerIfNeeded();
    }

    private static final Logger LOG = LoggerFactory.getLogger(Boot.class);
    
    private static ManifestInformationFactory c_informationFactory = new ManifestInformationFactory(Boot.class);

    public static void main(String[] args) {
        if (args == null || (args.length != 1 && args.length != 2)) {
            //noinspection UseOfSystemOutOrSystemErr
            System.err.println("java -cp <?> " + Boot.class.getName() + " <boot bean.xml in classpath> [log4j configuration in classpath]");
            //noinspection CallToSystemExit
            System.exit(1);
        } else {
            start(args[0], args.length == 2 ? args[1] : null);
        }
    }

    @Nonnull
    public static ConfigurableApplicationContext start(@Nonnull Class<?> reference, @Nonnull String bootBeanXmlFileName) {
        return start(reference, bootBeanXmlFileName, null);
    }

    @Nonnull
    public static ConfigurableApplicationContext start(@Nonnull Class<?> reference, @Nonnull String bootBeanXmlFileName, @Nullable String log4jConfigurationFileName) {
        final String bootBeanXmlInClassPath = fullElementPath(reference, bootBeanXmlFileName);
        final String log4jConfigurationInClassPath = log4jConfigurationFileName != null ? fullElementPath(reference, log4jConfigurationFileName) : null;
        return start(bootBeanXmlInClassPath, log4jConfigurationInClassPath);
    }

    @Nonnull
    public static ConfigurableApplicationContext start(@Nonnull String bootBeanXmlInClassPath) {
        return start(bootBeanXmlInClassPath, null);
    }

    @Nonnull
    public static ConfigurableApplicationContext start(@Nonnull String bootBeanXmlInClassPath, @Nullable String log4jConfigurationInClassPath) {
        configureInformationFactory(bootBeanXmlInClassPath);
        configureLog4j(log4jConfigurationInClassPath);
        return startComponent(bootBeanXmlInClassPath, getApplicationName());
    }

    @Nonnull
    public static ConfigurableApplicationContext startComponent(@Nonnull String bootBeanXmlInClassPath, @Nonnull final String applicationName) {
        return startComponent(null, bootBeanXmlInClassPath, applicationName);
    }

    @Nonnull
    public static ConfigurableApplicationContext startComponent(@Nullable ApplicationContext parent, @Nonnull String bootBeanXmlInClassPath, @Nonnull final String applicationName) {
        final StopWatch stopWatch = new StopWatch();
        LOG.info("Starting " + applicationName + "...");
        final ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(new String[] {bootBeanXmlInClassPath}, false, parent);
        final Thread shutdownHook = new Thread("destroyer") { @Override public void run() {
            LOG.info("Stopping " + applicationName + "...");
            applicationContext.close();
            LOG.info("Stopping " + applicationName + "... DONE!");
        }};
        boolean success = false;
        boolean applicationContextStarted = false;
        try {
            applicationContext.refresh();
            applicationContextStarted = true;
            waitForContextLoadThreads(applicationContext);
            setShutdownHook(shutdownHook, applicationContext);
            Runtime.getRuntime().addShutdownHook(shutdownHook);
            LOG.info("Starting " + applicationName + "... DONE! (after: " + stopWatch + ")");
            success = true;
        } finally {
            if (applicationContextStarted && !success) {
                applicationContext.close();
            }
        }
        return applicationContext;
    }

    @Nonnull
    public static String fullElementPath(@Nonnull Class<?> reference, @Nonnull String element) {
        return reference.getPackage().getName().replace('.', '/') + '/' + element;
    }

    @Nonnull
    public static ManifestInformationFactory getInformationFactory() {
        return c_informationFactory;
    }

    @Nonnull
    public static String getApplicationName() {
        final String applicationInfoString = c_informationFactory.getApplicationInfoString();
        return applicationInfoString != null ? applicationInfoString : Boot.class.getName();
    }

    private static void configureInformationFactory(String bootBeanXmlInClassPath) {
        c_informationFactory = new ManifestInformationFactory(bootBeanXmlInClassPath, Boot.class.getClassLoader());
    }

    public static void configureLog4j(@Nullable String log4jConfigurationInClassPath) {
        Log4JUtils.configureRuntime(log4jConfigurationInClassPath != null ? Boot.class.getClassLoader().getResource(log4jConfigurationInClassPath) : Boot.class.getResource("default.log4j.xml"));
    }

    protected static void waitForContextLoadThreads(@Nonnull AbstractApplicationContext applicationContext) {
        try {
            for (ContextLoadThreadGroup contextLoadThreadGroup : applicationContext.getBeansOfType(ContextLoadThreadGroup.class).values()) {
                contextLoadThreadGroup.join();
            }
        } catch (InterruptedException ignored) {
            currentThread().interrupt();
        }
    }

    protected static void setShutdownHook(@Nonnull Thread thread, @Nonnull AbstractApplicationContext to) {
        try {
            final Field field = AbstractApplicationContext.class.getDeclaredField("shutdownHook");
            field.setAccessible(true);
            field.set(to, thread);
        } catch (Exception e) {
            LOG.warn("Could not register shutdownHook at " + to + " this could cause to much memory consume of the JVM.", e);
        }
    }

    private Boot() {}
}

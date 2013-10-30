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

package org.echocat.jomon.testing.environments;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static java.lang.Thread.currentThread;
import static org.echocat.jomon.runtime.Log4JUtils.configureRuntime;
import static org.echocat.jomon.runtime.reflection.ClassUtils.findClass;

public class LogEnvironment extends BaseEnvironment implements TestRule {

    private final Class<?> _reference;

    public LogEnvironment() {
        this(null);
    }

    public LogEnvironment(@Nonnull Object object) {
        this(object instanceof Class ? (Class<?>)object : object.getClass());
    }

    public LogEnvironment(@Nullable Class<?> clazz) {
        final Class<?> targetClass = clazz != null ? clazz : findTopFromCallStack();
        String configuration = findFileFor(targetClass, getLog4JFileNameSuffixOfClasses(), getLog4JConfigurationFileNameInPackage());
        if (configuration == null) {
            configuration = findFileFor(LogEnvironment.class, getLog4JFileNameSuffixOfClasses(), getLog4JConfigurationFileNameInPackage());
        }
        if (configuration != null) {
            configureRuntime(targetClass.getClassLoader().getResource(configuration));
        }
        _reference = targetClass;
    }

    @Override
    public void close() {}

    @Nonnull
    protected String getLog4JFileNameSuffixOfClasses() {
        return ".log4j.xml";
    }

    @Nonnull
    protected String getLog4JConfigurationFileNameInPackage() {
        return "log4j.xml";
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() { @Override public void evaluate() throws Throwable {
            base.evaluate();
        }};
    }

    @Nonnull
    public Logger getLogger() {
        return getLogger(_reference);
    }

    @Nonnull
    public Logger getLogger(@Nonnull String name) {
        return LoggerFactory.getLogger(name);
    }

    @Nonnull
    public Logger getLogger(@Nonnull Class<?> reference) {
        return LoggerFactory.getLogger(reference);
    }

    @Nonnull
    protected Class<?> findTopFromCallStack() {
        final StackTraceElement[] stackTrace = currentThread().getStackTrace();
        Class<?> found = null;
        for (StackTraceElement stackTraceElement : stackTrace) {
            final Class<?> currentClass = findClass(stackTraceElement.getClassName());
            if (currentClass != Thread.class && !currentClass.isAssignableFrom(LogEnvironment.class)) {
                found = currentClass;
                break;
            }
        }
        return found != null ? found : getClass();
    }

}

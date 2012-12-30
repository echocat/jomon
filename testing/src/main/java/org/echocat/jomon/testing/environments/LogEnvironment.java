/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012 echocat
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

import javax.annotation.Nonnull;

import static org.echocat.jomon.runtime.Log4JUtils.configureRuntime;

public class LogEnvironment extends BaseEnvironment implements TestRule {

    public LogEnvironment() {
        this(LogEnvironment.class);
    }

    public LogEnvironment(@Nonnull Object object) {
        this(object instanceof Class ? (Class<?>)object : object.getClass());
    }

    public LogEnvironment(@Nonnull Class<?> clazz) {
        final String configuration = findFileFor(clazz, getLog4JFileNameSuffixOfClasses(), getLog4JConfigurationFileNameInPackage());
        if (configuration != null) {
            configureRuntime(clazz.getClassLoader().getResource(configuration));
        }
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
}

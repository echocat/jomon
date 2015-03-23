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

package org.echocat.jomon.spring.testing.environments;

import org.echocat.jomon.testing.environments.LoggingEnvironment;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

public class TestingEnvironment implements TestRule {

    @Nonnull
    private final Object _baseObject;

    @Nullable
    private BeanEnvironment _beanEnvironment;

    public TestingEnvironment(@Nonnull Object baseObject) {
        _baseObject = baseObject;
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() { @Override public void evaluate() throws Throwable {
            final LoggingEnvironment logEnvironment = new LoggingEnvironment(_baseObject);
            try {
                final BeanEnvironment beanEnvironment = new BeanEnvironment(_baseObject);
                try {
                    _beanEnvironment = beanEnvironment;
                    base.evaluate();
                } finally {
                    try {
                        closeQuietly(beanEnvironment);
                    } finally {
                        _beanEnvironment = null;
                    }
                }
            } finally {
                closeQuietly(logEnvironment);
            }
        }};
    }

    @Nonnull
    public ConfigurableApplicationContext getApplicationContext() {
        return _beanEnvironment.getApplicationContext();
    }

}

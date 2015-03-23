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

package org.echocat.jomon.spring;

import org.echocat.jomon.spring.application.Application;
import org.echocat.jomon.spring.beans.Bean1;
import org.echocat.jomon.spring.beans.Bean2;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import static org.echocat.jomon.spring.Boot.start;
import static org.echocat.jomon.spring.application.DefaultApplicationRequirement.javaConfiguredApplicationFor;
import static org.echocat.jomon.testing.BaseMatchers.is;
import static org.echocat.jomon.testing.BaseMatchers.isNotNull;
import static org.junit.Assert.assertThat;

public class BootUnitTest {

    @Test
    public void bootJavaConfiguration1() throws Exception {
        try (final Application application = start(javaConfiguredApplicationFor(JavaConfiguration1.class))) {
            final ApplicationContext context = application.getApplicationContext();

            final Bean1 bean1 = context.getBean(Bean1.class);
            assertThat(bean1, isNotNull());
            assertThat(bean1.getAString(), is(null));

            final Bean2 bean2 = context.getBean(Bean2.class);
            assertThat(bean2, isNotNull());
            assertThat(bean2.getBean1(), is(bean1));
        }
    }

}
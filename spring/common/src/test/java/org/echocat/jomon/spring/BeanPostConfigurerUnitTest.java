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

import org.echocat.jomon.testing.environments.LoggingEnvironment;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static java.lang.System.*;
import static org.echocat.jomon.testing.BaseMatchers.is;
import static org.junit.Assert.assertThat;

public class BeanPostConfigurerUnitTest {

    protected static final String SYSTEM_PROPERTY_NAME = TestBean.class.getName() + "#other";
    @Rule
    public LoggingEnvironment _logEnvironment = new LoggingEnvironment();

    @Test
    public void testWithOutSystemProperties() throws Exception {
        try (ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("BeanPostConfigurerUnitTest.beans.xml", getClass())) {
            final TestBean testBean = applicationContext.getBean(TestBean.class);
            assertThat(testBean.getFoo(), is("fooValueFromFile"));
            assertThat(testBean.getOther(), is("originalOtherValue"));
            assertThat(testBean.getBar(), is(667));
        }
    }

    @Test
    public void testWithSystemProperties() throws Exception {
        final String oldPropertyValue = getProperty(SYSTEM_PROPERTY_NAME);
        try {
            setProperty(SYSTEM_PROPERTY_NAME, "otherValueFromSystemProperties");
            try (ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("BeanPostConfigurerUnitTest.beans.xml", getClass())) {
                final TestBean testBean = applicationContext.getBean(TestBean.class);
                assertThat(testBean.getFoo(), is("fooValueFromFile"));
                assertThat(testBean.getOther(), is("otherValueFromSystemProperties"));
                assertThat(testBean.getBar(), is(667));
            }
        } finally {
            if (oldPropertyValue != null) {
                setProperty(SYSTEM_PROPERTY_NAME, oldPropertyValue);
            } else {
                clearProperty(SYSTEM_PROPERTY_NAME);
            }
        }
    }
    
    public static class TestBean {

        private String _foo;
        private String _other;
        private int _bar;

        public String getFoo() {
            return _foo;
        }

        public void setFoo(String foo) {
            _foo = foo;
        }

        public String getOther() {
            return _other;
        }

        public void setOther(String other) {
            _other = other;
        }

        public int getBar() {
            return _bar;
        }

        public void setBar(int bar) {
            _bar = bar;
        }
    }
}

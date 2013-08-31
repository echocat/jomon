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

package org.echocat.jomon.runtime.id;

import org.echocat.jomon.runtime.jaxb.XmlId;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class AnnotationBasedIdProviderUnitTest {

    @Test
    public void testProvideIdOf() throws Exception {
        final AnnotationBasedIdProvider<Object, Object> provider = new AnnotationBasedIdProvider<>(TestBeanA.class, TestBeanA1.class);
        final TestBeanA beanA = new TestBeanA();
        assertThat(provider.provideIdOf(beanA), is((Object)null));
        beanA.setId("foo");
        assertThat(provider.provideIdOf(beanA), is((Object)"foo"));

        final TestBeanA1 beanA1 = new TestBeanA1();
        assertThat(provider.provideIdOf(beanA1), is((Object)null));
        beanA1.setId("foo1");
        assertThat(provider.provideIdOf(beanA1), is((Object)"foo1"));

        final TestBeanA2 beanA2 = new TestBeanA2();
        assertThat(provider.provideIdOf(beanA2), is((Object)null));
        beanA2.setId("foo2");
        assertThat(provider.provideIdOf(beanA2), is((Object) "foo2"));
    }

    @Test
    public void testCreationOfBeanWithoutIdAnnotation() throws Exception {
        try {
            new AnnotationBasedIdProvider<>(TestBeanB.class);
            fail("Expected exception missing.");
        } catch (IllegalArgumentException expected) {}
    }

    public static class TestBeanA {

        private String _id;
        private String _value;

        @XmlId
        public String getId() {
            return _id;
        }

        public void setId(String id) {
            _id = id;
        }

        public String getValue() {
            return _value;
        }

        public void setValue(String value) {
            _value = value;
        }
    }

    public static class TestBeanA1 extends TestBeanA {}
    public static class TestBeanA2 extends TestBeanA {}

    public static class TestBeanB {

        private String _id;
        private String _value;

        public String getId() {
            return _id;
        }

        public void setId(String id) {
            _id = id;
        }

        public String getValue() {
            return _value;
        }

        public void setValue(String value) {
            _value = value;
        }
    }
}

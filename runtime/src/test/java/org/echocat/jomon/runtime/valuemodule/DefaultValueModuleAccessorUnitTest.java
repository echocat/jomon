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

package org.echocat.jomon.runtime.valuemodule;

import org.echocat.jomon.runtime.valuemodule.access.DefaultValueModuleAccessor;
import org.echocat.jomon.runtime.valuemodule.access.ValueModuleToAssignmentStack;
import org.echocat.jomon.runtime.valuemodule.annotation.AnnotationBasedValueModuleToAssignmentStackFactory;
import org.echocat.jomon.runtime.valuemodule.testbeans.a.BeanA;
import org.echocat.jomon.runtime.valuemodule.testbeans.a.BeanAModule;
import org.echocat.jomon.runtime.valuemodule.testbeans.a.a.BeanAA;
import org.echocat.jomon.runtime.valuemodule.testbeans.a.a.BeanAAModule;
import org.echocat.jomon.runtime.valuemodule.testbeans.a.a.a.BeanAAA;
import org.echocat.jomon.runtime.valuemodule.testbeans.a.a.a.BeanAAAModule;
import org.echocat.jomon.runtime.valuemodule.testbeans.a.a.a.a.BeanAAAA;
import org.echocat.jomon.runtime.valuemodule.testbeans.a.a.a.b.BeanAAAB;
import org.echocat.jomon.runtime.valuemodule.testbeans.a.a.b.BeanAAB;
import org.echocat.jomon.runtime.valuemodule.testbeans.a.b.BeanAB;
import org.junit.Test;

import javax.annotation.Nonnull;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class DefaultValueModuleAccessorUnitTest {

    protected static final ValueModuleToAssignmentStack<ValueModule, BeanA> STACK = new AnnotationBasedValueModuleToAssignmentStackFactory().getStack(BeanA.class);
    protected static final DefaultValueModuleAccessor<ValueModule, BeanA> ACCESSOR = new DefaultValueModuleAccessor<>(STACK);

    @Test
    public void testGetValueOfLevel1() throws Exception {
        final BeanA bean = new BeanA();
        assertThat(ACCESSOR.getValueOf(bean, BeanAModule.a), is((Object) null));
        assertThat(ACCESSOR.getValueOf(bean, BeanAModule.b), is((Object) null));
        bean.setA(new BeanAA());
        assertThat(ACCESSOR.getValueOf(bean, BeanAModule.a), is((Object) bean.getA()));
        assertThat(ACCESSOR.getValueOf(bean, BeanAModule.b), is((Object) null));
        bean.setB(new BeanAB());
        assertThat(ACCESSOR.getValueOf(bean, BeanAModule.a), is((Object) bean.getA()));
        assertThat(ACCESSOR.getValueOf(bean, BeanAModule.b), is((Object) bean.getB()));
    }

    @Test
    public void testGetValueOfLevel2() throws Exception {
        final BeanA bean = new BeanA();
        assertThat(ACCESSOR.getValueOf(bean, BeanAAModule.a), is((Object) null));
        assertThat(ACCESSOR.getValueOf(bean, BeanAAModule.b), is((Object) null));
        bean.setA(new BeanAA());
        assertThat(ACCESSOR.getValueOf(bean, BeanAAModule.a), is((Object) null));
        assertThat(ACCESSOR.getValueOf(bean, BeanAAModule.b), is((Object) null));

        bean.getA().setA(new BeanAAA());
        assertThat(ACCESSOR.getValueOf(bean, BeanAAModule.a), is((Object) bean.getA().getA()));
        assertThat(ACCESSOR.getValueOf(bean, BeanAAModule.b), is((Object) null));
        bean.getA().setB(new BeanAAB());
        assertThat(ACCESSOR.getValueOf(bean, BeanAAModule.a), is((Object) bean.getA().getA()));
        assertThat(ACCESSOR.getValueOf(bean, BeanAAModule.b), is((Object) bean.getA().getB()));
    }

    @Test
    public void testGetValueOfLevel3() throws Exception {
        final BeanA bean = new BeanA();
        assertThat(ACCESSOR.getValueOf(bean, BeanAAAModule.a), is((Object) null));
        assertThat(ACCESSOR.getValueOf(bean, BeanAAAModule.b), is((Object) null));
        bean.setA(new BeanAA());
        assertThat(ACCESSOR.getValueOf(bean, BeanAAAModule.a), is((Object) null));
        assertThat(ACCESSOR.getValueOf(bean, BeanAAAModule.b), is((Object) null));
        bean.getA().setA(new BeanAAA());
        assertThat(ACCESSOR.getValueOf(bean, BeanAAAModule.a), is((Object) null));
        assertThat(ACCESSOR.getValueOf(bean, BeanAAAModule.b), is((Object) null));

        bean.getA().getA().setA(new BeanAAAA());
        assertThat(ACCESSOR.getValueOf(bean, BeanAAAModule.a), is((Object) bean.getA().getA().getA()));
        assertThat(ACCESSOR.getValueOf(bean, BeanAAAModule.b), is((Object) null));
        bean.getA().getA().setB(new BeanAAAB());
        assertThat(ACCESSOR.getValueOf(bean, BeanAAAModule.a), is((Object) bean.getA().getA().getA()));
        assertThat(ACCESSOR.getValueOf(bean, BeanAAAModule.b), is((Object) bean.getA().getA().getB()));
    }

    @Test
    public void testSetValueOfLevel1() throws Exception {
        final BeanA bean = new BeanA();

        final BeanAA beanAA = new BeanAA();
        assertThat(bean.getA(), is((Object) null));
        ACCESSOR.setValueOf(bean, BeanAModule.a, beanAA);
        assertThat(bean.getA(), is((Object) beanAA));

        final BeanAB beanAB = new BeanAB();
        assertThat(bean.getB(), is((Object) null));
        ACCESSOR.setValueOf(bean, BeanAModule.b, beanAB);
        assertThat(bean.getB(), is((Object) beanAB));
    }

    @Test
    public void testSetValueOfLevel2() throws Exception {
        final BeanA bean = new BeanA();

        assertThatSetThrowsNullPointerException(bean, BeanAAModule.a);
        assertThatSetThrowsNullPointerException(bean, BeanAAModule.b);
        bean.setA(new BeanAA());

        final BeanAAA beanAAA = new BeanAAA();
        assertThat(bean.getA().getA(), is((Object) null));
        ACCESSOR.setValueOf(bean, BeanAAModule.a, beanAAA);
        assertThat(bean.getA().getA(), is((Object) beanAAA));

        final BeanAAB beanAAB = new BeanAAB();
        assertThat(bean.getA().getB(), is((Object) null));
        ACCESSOR.setValueOf(bean, BeanAAModule.b, beanAAB);
        assertThat(bean.getA().getB(), is((Object) beanAAB));
    }

    @Test
    public void testSetValueOfLevel3() throws Exception {
        final BeanA bean = new BeanA();

        assertThatSetThrowsNullPointerException(bean, BeanAAModule.a);
        assertThatSetThrowsNullPointerException(bean, BeanAAAModule.a);
        assertThatSetThrowsNullPointerException(bean, BeanAAAModule.b);
        bean.setA(new BeanAA());
        bean.getA().setA(new BeanAAA());

        final BeanAAAA beanAAAA = new BeanAAAA();
        assertThat(bean.getA().getA().getA(), is((Object) null));
        ACCESSOR.setValueOf(bean, BeanAAAModule.a, beanAAAA);
        assertThat(bean.getA().getA().getA(), is((Object) beanAAAA));

        final BeanAAAB beanAAAB = new BeanAAAB();
        assertThat(bean.getA().getA().getB(), is((Object) null));
        ACCESSOR.setValueOf(bean, BeanAAAModule.b, beanAAAB);
        assertThat(bean.getA().getA().getB(), is((Object) beanAAAB));
    }

    protected static void assertThatSetThrowsNullPointerException(@Nonnull BeanA bean, @Nonnull ValueModule module) {
        try {
            ACCESSOR.setValueOf(bean, module, new Object());
            fail("Exepcted exception missing.");
        } catch (NullPointerException expected) {}
    }

}

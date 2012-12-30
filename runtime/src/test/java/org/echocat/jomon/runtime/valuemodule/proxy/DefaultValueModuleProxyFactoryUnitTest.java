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

package org.echocat.jomon.runtime.valuemodule.proxy;

import org.echocat.jomon.runtime.valuemodule.DefaultValueModuleRegistry;
import org.echocat.jomon.runtime.valuemodule.SourcePath;
import org.echocat.jomon.runtime.valuemodule.ValueModule;
import org.echocat.jomon.runtime.valuemodule.ValueModuleRegistry;
import org.echocat.jomon.runtime.valuemodule.access.DefaultValueModuleAccessor;
import org.echocat.jomon.runtime.valuemodule.access.ValueModuleAccessor;
import org.echocat.jomon.runtime.valuemodule.access.ValueModuleToAssignmentStack;
import org.echocat.jomon.runtime.valuemodule.annotation.AnnotationBasedValueModuleProvider;
import org.echocat.jomon.runtime.valuemodule.annotation.AnnotationBasedValueModuleToAssignmentStackFactory;
import org.echocat.jomon.runtime.valuemodule.fetching.ValueFetcher;
import org.echocat.jomon.runtime.valuemodule.testbeans.a.BeanA;
import org.echocat.jomon.runtime.valuemodule.testbeans.a.BeanAModule;
import org.echocat.jomon.runtime.valuemodule.testbeans.a.a.BeanAA;
import org.echocat.jomon.runtime.valuemodule.testbeans.a.a.BeanAAModule;
import org.echocat.jomon.runtime.valuemodule.testbeans.a.a.a.BeanAAA;
import org.echocat.jomon.runtime.valuemodule.testbeans.a.a.a.a.BeanAAAA;
import org.echocat.jomon.runtime.valuemodule.testbeans.a.b.BeanAB;
import org.echocat.jomon.runtime.valuemodule.testbeans.a.c.BeanAC;
import org.junit.Test;
import org.mockito.Matchers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class DefaultValueModuleProxyFactoryUnitTest {

    protected static final ValueModuleProxyFactory FACTORY = new DefaultValueModuleProxyFactory();

    @Test
    public void testNotModularizedStringProperty() throws Exception {
        final BeanA original = new BeanA();
        final BeanA proxy = createProxyFor(BeanA.class, original);

        assertThat(proxy.getD(), is((Object) null));
        original.setD("foo");
        assertThat(proxy.getD(), is("foo"));
        proxy.setD("bar");
        assertThat(proxy.getD(), is("bar"));
    }

    @Test
    public void testNotModularizedBeanACProperty() throws Exception {
        final BeanA original = new BeanA();
        final BeanA proxy = createProxyFor(BeanA.class, original);

        assertThat(proxy.getC(), is((Object) null));
        final BeanAC beanAC1 = new BeanAC();
        original.setC(beanAC1);
        assertThat(proxy.getC(), is(beanAC1));
        final BeanAC beanAC2 = new BeanAC();
        proxy.setC(beanAC2);
        assertThat(proxy.getC(), is(beanAC2));
        assertThat(proxy.getC(), is(not(beanAC1)));
    }

    @Test
    public void testModularizedBeanAAProperty() throws Exception {
        final ValueFetcher<ValueModule, Object, BeanA> valueFetcher = valueFetcherFor(BeanA.class);
        final BeanA original = new BeanA();
        final BeanA proxy = createProxyFor(BeanA.class, valueFetcher, original);

        final BeanAA beanAA0 = new BeanAA();
        doReturn(beanAA0).when(valueFetcher).fetchValueOf(eq(original), eq(BeanAModule.a), anySourcePath());
        assertThat(proxy.getA(), is(beanAA0));

        final BeanAA beanAA1 = new BeanAA();
        original.setA(beanAA1);
        assertThat(proxy.getA(), is(beanAA1));

        final BeanAA beanAA2 = new BeanAA();
        proxy.setA(beanAA2);
        assertThat(proxy.getA(), is(beanAA2));
        assertThat(proxy.getA(), is(not(beanAA0)));
        assertThat(proxy.getA(), is(not(beanAA1)));
    }

    @Nonnull
    protected static SourcePath anySourcePath() {
        return Matchers.anyObject();
    }

    @Test
    public void testModularizedBeanABProperty() throws Exception {
        final ValueFetcher<ValueModule, Object, BeanA> valueFetcher = valueFetcherFor(BeanA.class);
        final BeanA original = new BeanA();
        final BeanA proxy = createProxyFor(BeanA.class, valueFetcher, original);

        final BeanAB beanAB0 = new BeanAB();
        doReturn(beanAB0).when(valueFetcher).fetchValueOf(eq(original), eq(BeanAModule.b), anySourcePath());
        assertThat(proxy.getB(), is(beanAB0));

        final BeanAB beanAB1 = new BeanAB();
        original.setB(beanAB1);
        assertThat(proxy.getB(), is(beanAB1));

        final BeanAB beanAB2 = new BeanAB();
        proxy.setB(beanAB2);
        assertThat(proxy.getB(), is(beanAB2));
        assertThat(proxy.getB(), is(not(beanAB0)));
        assertThat(proxy.getB(), is(not(beanAB1)));
    }

    @Test
    public void testModularizedBeanAAAProperty() throws Exception {
        final ValueFetcher<ValueModule, Object, BeanA> valueFetcher = valueFetcherFor(BeanA.class, BeanAA.class);
        final BeanA originalA = new BeanA();
        final BeanAA originalAA = new BeanAA();
        final BeanA proxyA = createProxyFor(BeanA.class, valueFetcher, originalA);

        doReturn(originalAA).when(valueFetcher).fetchValueOf(eq(originalA), eq(BeanAModule.a), anySourcePath());

        assertThat(proxyA.getA() instanceof ValueModuleProxy, is(true));
        proxyA.getA().setC("foo");
        assertThat(proxyA.getA().getC(), is("foo"));
        proxyA.setA(new BeanAA());
        assertThat(proxyA.getA() instanceof ValueModuleProxy, is(false));
        assertThat(proxyA.getA().getC(), is((String) null));
    }

    @Test
    public void testModularizedBeanAAAAProperty() throws Exception {
        final ValueFetcher<ValueModule, Object, BeanA> valueFetcher = valueFetcherFor(BeanA.class, BeanAA.class, BeanAAA.class);
        final BeanA originalA = new BeanA();
        final BeanAA originalAA = new BeanAA();
        final BeanAAA originalAAA = new BeanAAA();
        final BeanA proxyA = createProxyFor(BeanA.class, valueFetcher, originalA);

        doReturn(originalAA).when(valueFetcher).fetchValueOf(eq(originalA), eq(BeanAModule.a), anySourcePath());
        doReturn(originalAAA).when(valueFetcher).fetchValueOf(eq(originalA), eq(BeanAAModule.a), anySourcePath());

        final BeanAA proxyAA = proxyA.getA();

        final BeanAAA proxyAAA1 = proxyAA.getA();
        assertThat(proxyAAA1 instanceof ValueModuleProxy, is(true));
        proxyAAA1.setA(new BeanAAAA());
        proxyAAA1.setD("foo");
        assertThat(proxyAAA1.getD(), is("foo"));
        proxyAA.setA(new BeanAAA());
        final BeanAAA proxyAAA2 = proxyAA.getA();
        assertThat(proxyAAA2 instanceof ValueModuleProxy, is(false));
        assertThat(proxyAAA2.getD(), is((String) null));
        assertThat(proxyAAA1, is(not(proxyAAA2)));

        try {
            proxyAAA1.toString();
            fail("Expected exception missing.");
        } catch (BeanDisconnectedException expected) {}
        final BeanAAAA beanAAAA1 = new BeanAAAA();
        proxyAAA2.setA(beanAAAA1);
        assertThat(proxyAAA2.getA(), is(beanAAAA1));
        // noinspection ObjectEquality
        assertThat(proxyAAA2.getA() == beanAAAA1, is(true));
    }

    @Nonnull
    protected static <B> B createProxyFor(@Nonnull Class<? extends B> type, @Nullable B baseBean) {
        return createProxyFor(type, DefaultValueModuleProxyFactoryUnitTest.<B>valueFetcherFor(type), baseBean);
    }

    @Nonnull
    protected static <VM extends ValueModule, ID, B> B createProxyFor(@Nonnull Class<? extends B> type, @Nonnull ValueFetcher<VM, ID, B> fetcher, @Nonnull B baseBean) {
        final ValueModuleRegistry<VM, B> registry = valueModuleRegistryFor(type);
        return createProxyFor(registry, fetcher, baseBean);
    }

    @Nonnull
    protected static <VM extends ValueModule, ID, B> B createProxyFor(@Nonnull ValueModuleRegistry<VM, B> registry, @Nonnull ValueFetcher<VM, ID, B> fetcher, @Nonnull B baseBean) {
        return FACTORY.createFor(registry, fetcher, baseBean, null);
    }

    @Nonnull
    protected static <VM extends ValueModule, B> ValueModuleRegistry<VM, B> valueModuleRegistryFor(@Nonnull Class<? extends B> type) {
        final ValueModuleAccessor<VM, B> accessor = valueModuleAccessorFor(type);
        return new DefaultValueModuleRegistry<>(type, accessor);
    }

    @Nonnull
    protected static <VM extends ValueModule, B> ValueModuleAccessor<VM, B> valueModuleAccessorFor(@Nonnull Class<? extends B> type) {
        final ValueModuleToAssignmentStack<VM, B> stack = stackFor(type);
        return new DefaultValueModuleAccessor<>(stack);
    }

    @Nonnull
    protected static <VM extends ValueModule, B> ValueModuleToAssignmentStack<VM, B> stackFor(@Nonnull Class<? extends B> valueType) {
        final AnnotationBasedValueModuleToAssignmentStackFactory factory = new AnnotationBasedValueModuleToAssignmentStackFactory();
        // noinspection unchecked
        return (ValueModuleToAssignmentStack<VM, B>) factory.getStack(valueType);
    }

    @Nonnull
    protected static <VM extends Enum<VM> & ValueModule, B> AnnotationBasedValueModuleProvider<VM> valueModuleProviderFor(@Nonnull ValueModuleToAssignmentStack<VM, B> stack) {
        // noinspection unchecked
        return new AnnotationBasedValueModuleProvider(stack.getAllSupportedValueModuleTypes());
    }

    @Nonnull
    protected static <T> ValueFetcher<ValueModule, Object, T> valueFetcherFor(@Nonnull Class<?>... types) {
        // noinspection unchecked
        final ValueFetcher<ValueModule, Object, T> fetcher = mock(ValueFetcher.class);
        for (Class<?> type : types) {
            doReturn(true).when(fetcher).isResponsibleFor(type);
        }
        return fetcher;
    }
}

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

package org.echocat.jomon.runtime.reflection;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import static org.echocat.jomon.runtime.CollectionUtils.asImmutableList;
import static org.hamcrest.MatcherAssert.assertThat;

public class ClassIteratorTest {

    @Test
    public void testInterfaceA() throws Exception {
        assertThat(new ClassIterator(InterfaceA.class), returns(
            InterfaceA.class
        ));
    }

    @Test
    public void testInterfaceAC() throws Exception {
        assertThat(new ClassIterator(InterfaceAC.class), returns(
            InterfaceAC.class,
            InterfaceAB.class,
            InterfaceAA.class
        ));
    }

    @Test
    public void testClassA() throws Exception {
        assertThat(new ClassIterator(ClassA.class), returns(
            ClassA.class,
            InterfaceAC.class,
            InterfaceAB.class,
            InterfaceAA.class,
            InterfaceA.class,
            Object.class
        ));
    }

    @Test
    public void testClassB() throws Exception {
        assertThat(new ClassIterator(ClassB.class), returns(
            ClassB.class,
            InterfaceBC.class,
            InterfaceBB.class,
            InterfaceBA.class,
            InterfaceB.class,
            ClassA.class,
            InterfaceAC.class,
            InterfaceAB.class,
            InterfaceAA.class,
            InterfaceA.class,
            Object.class
        ));
    }

    @Test
    public void testClassC() throws Exception {
        assertThat(new ClassIterator(ClassC.class), returns(
            ClassC.class,
            InterfaceCC.class,
            InterfaceCB.class,
            InterfaceCA.class,
            InterfaceC.class,
            ClassB.class,
            InterfaceBC.class,
            InterfaceBB.class,
            InterfaceBA.class,
            InterfaceB.class,
            ClassA.class,
            InterfaceAC.class,
            InterfaceAB.class,
            InterfaceAA.class,
            InterfaceA.class,
            Object.class
        ));
    }

    @Test
    public void testIteratorFor() throws Exception {
        assertThat(iteratorFor(ClassC.class), returns(
            InterfaceCC.class,
            InterfaceC.class,
            ClassB.class
        ));
    }

    public static interface InterfaceAA {}
    public static interface InterfaceAB extends InterfaceAA {}
    public static interface InterfaceAC extends InterfaceAB {}
    public static interface InterfaceA {}
    public static class ClassA implements InterfaceAC, InterfaceA {}

    public static interface InterfaceBA {}
    public static interface InterfaceBB extends InterfaceBA {}
    public static interface InterfaceBC extends InterfaceBB {}
    public static interface InterfaceB {}
    public static class ClassB extends ClassA implements InterfaceBC, InterfaceB {}

    public static interface InterfaceCA {}
    public static interface InterfaceCB extends InterfaceCA {}
    public static interface InterfaceCC extends InterfaceCB {}
    public static interface InterfaceC {}
    public static class ClassC extends ClassB implements InterfaceCC, InterfaceC {}


    @Nonnull
    protected static Iterator<Class<?>> iteratorFor(@Nonnull Class<?> what) {
        return new ClassIterator(Object.class).iteratorFor(what);
    }

    @Nonnull
    protected static Matcher<Iterator<Class<?>>> returns(@Nullable Class<?>... classes) {
        final Map<Iterator<Class<?>>, List<Class<?>>> iteratorToAsList = new WeakHashMap<>();
        final List<Class<?>> expected = asImmutableList(classes);
        return new TypeSafeMatcher<Iterator<Class<?>>>() {

            @Override
            protected boolean matchesSafely(@Nonnull Iterator<Class<?>> item) {
                final List<Class<?>> actual = asImmutableList(item);
                iteratorToAsList.put(item, actual);
                return expected.equals(actual);
            }

            @Override
            public void describeTo(@Nonnull Description description) {
                description.appendText("returns ").appendValue(expected);
            }

            @Override
            protected void describeMismatchSafely(@Nonnull Iterator<Class<?>> item, @Nonnull Description description) {
                final List<Class<?>> actual = iteratorToAsList.get(item);
                description.appendText("was ").appendValue(actual != null ? actual : item);
            }
        };
    }

}

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

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.echocat.jomon.runtime.reflection.ClassUtils.findAnnotationRecursively;
import static org.hamcrest.MatcherAssert.assertThat;

public class ClassUtilsUnitTest {

    @Test
    public void testFindAnnotationRecursively() throws Exception {
        assertThat(findAnnotationRecursively(ClassAnnotation.class, ClassA.class), contains(ClassAnnotation.class, 'A'));
        assertThat(findAnnotationRecursively(ClassAnnotation.class, ClassB.class), contains(ClassAnnotation.class, 'B'));
        assertThat(findAnnotationRecursively(ClassAnnotation.class, ClassC.class), contains(ClassAnnotation.class, 'C'));

        assertThat(findAnnotationRecursively(AnnotationA.class, ClassA.class), contains(AnnotationA.class, 'A'));
        assertThat(findAnnotationRecursively(AnnotationA.class, ClassB.class), contains(AnnotationA.class, 'B'));
        assertThat(findAnnotationRecursively(AnnotationA.class, ClassC.class), contains(AnnotationA.class, 'C'));

        assertThat(findAnnotationRecursively(AnnotationB.class, ClassA.class), contains(AnnotationB.class, 'A'));
        assertThat(findAnnotationRecursively(AnnotationB.class, ClassB.class), contains(AnnotationB.class, 'B'));
        assertThat(findAnnotationRecursively(AnnotationB.class, ClassC.class), contains(AnnotationB.class, 'C'));

        assertThat(findAnnotationRecursively(AnnotationC.class, ClassA.class), contains(AnnotationC.class, 'A'));
        assertThat(findAnnotationRecursively(AnnotationC.class, ClassB.class), contains(AnnotationC.class, 'B'));
        assertThat(findAnnotationRecursively(AnnotationC.class, ClassC.class), contains(AnnotationC.class, 'C'));
    }

    @AnnotationA('A')
    public static interface InterfaceAA {}
    @AnnotationB('A')
    public static interface InterfaceAB extends InterfaceAA {}
    @AnnotationC('A')
    public static interface InterfaceAC extends InterfaceAB {}
    public static interface InterfaceA {}
    @ClassAnnotation('A')
    public static class ClassA implements InterfaceAC, InterfaceA {}

    @AnnotationA('B')
    public static interface InterfaceBA {}
    @AnnotationB('B')
    public static interface InterfaceBB extends InterfaceBA {}
    @AnnotationC('B')
    public static interface InterfaceBC extends InterfaceBB {}
    public static interface InterfaceB {}
    @ClassAnnotation('B')
    public static class ClassB extends ClassA implements InterfaceBC, InterfaceB {}

    @AnnotationA('C')
    public static interface InterfaceCA {}
    @AnnotationB('C')
    public static interface InterfaceCB extends InterfaceCA {}
    @AnnotationC('C')
    public static interface InterfaceCC extends InterfaceCB {}
    public static interface InterfaceC {}
    @ClassAnnotation('C')
    public static class ClassC extends ClassB implements InterfaceCC, InterfaceC {}

    @Retention(RUNTIME)
    @Target(TYPE)
    public @interface ClassAnnotation { public char value(); }
    @Retention(RUNTIME)
    @Target(TYPE)
    public @interface AnnotationA { public char value(); }
    @Retention(RUNTIME)
    @Target(TYPE)
    public @interface AnnotationB { public char value(); }
    @Retention(RUNTIME)
    @Target(TYPE)
    public @interface AnnotationC { public char value(); }

    @Nonnull
    protected static Matcher<Annotation> contains(@Nonnull final Class<? extends Annotation> annotationType, @Nonnull final char expected) {
        return new BaseMatcher<Annotation>() {
            @Override
            public boolean matches(Object item) {
                final boolean result;
                if (annotationType.isInstance(item)) {
                    final char actual;
                    try {
                        actual = (char) annotationType.getMethod("value").invoke(item);
                    } catch (final Exception e) {
                        throw new RuntimeException("Could not invoke value method of " + item + ".", e);
                    }
                    result = expected == actual;
                } else {
                    result = false;
                }
                return result;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("contains ").appendValue("@" + annotationType.getName() + "{value=" + Character.toString(expected) + "}");
            }

        };
    }
}

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

import org.junit.Test;

import static org.echocat.jomon.runtime.reflection.IncludesExcludesClassPredicate.classPredicate;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class IncludesExcludesClassPredicateUnitTest {

    @Test
    public void noLimits() throws Exception {
        assertThat(classPredicate().apply(A.class), is(true));
        assertThat(classPredicate().apply(B.class), is(true));
    }

    @Test
    public void onlyIncludes() throws Exception {
        assertThat(classPredicate().including(A.class).apply(A.class), is(true));
        assertThat(classPredicate().including(A.class).apply(B.class), is(false));
    }

    @Test
    public void onlyIncludesWithInheritance() throws Exception {
        assertThat(classPredicate().including(A.class).apply(AA.class), is(true));
        assertThat(classPredicate().including(A.class).apply(AB.class), is(true));
        assertThat(classPredicate().including(A.class).apply(BA.class), is(false));
        assertThat(classPredicate().including(A.class).apply(BB.class), is(false));
    }

    @Test
    public void onlyExcludes() throws Exception {
        assertThat(classPredicate().excluding(B.class).apply(A.class), is(true));
        assertThat(classPredicate().excluding(B.class).apply(B.class), is(false));
    }

    @Test
    public void onlyExcludesWithInheritance() throws Exception {
        assertThat(classPredicate().excluding(B.class).apply(AA.class), is(true));
        assertThat(classPredicate().excluding(B.class).apply(AB.class), is(true));
        assertThat(classPredicate().excluding(B.class).apply(BA.class), is(false));
        assertThat(classPredicate().excluding(B.class).apply(BB.class), is(false));
    }

    @Test
    public void includesAndExcludes() throws Exception {
        assertThat(classPredicate().including(A.class, B.class).excluding(B.class).apply(A.class), is(true));
        assertThat(classPredicate().including(A.class, B.class).excluding(B.class).apply(B.class), is(false));
        assertThat(classPredicate().including(A.class, B.class).excluding(B.class).apply(C.class), is(false));
    }
    
    @Test
    public void includesAndExcludesWithInheritance() throws Exception {
        assertThat(classPredicate().including(A.class, B.class).excluding(B.class).apply(AA.class), is(true));
        assertThat(classPredicate().including(A.class, B.class).excluding(B.class).apply(AB.class), is(true));
        assertThat(classPredicate().including(A.class, B.class).excluding(B.class).apply(BA.class), is(false));
        assertThat(classPredicate().including(A.class, B.class).excluding(B.class).apply(BB.class), is(false));
        assertThat(classPredicate().including(A.class, B.class).excluding(B.class).apply(CA.class), is(false));
        assertThat(classPredicate().including(A.class, B.class).excluding(B.class).apply(CB.class), is(false));
    }

    public static class A {}
    public static class B {}
    public static class C {}

    public static class AA extends A {}
    public static class AB extends A {}

    public static class BA extends B {}
    public static class BB extends B {}

    public static class CA extends C {}
    public static class CB extends C {}
}

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

package org.echocat.jomon.runtime.exceptions;

import org.junit.Test;

import static org.echocat.jomon.runtime.exceptions.ExceptionUtils.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@SuppressWarnings("ThrowableInstanceNeverThrown")
public class ExceptionUtilsUnitTest {

    @Test
    public void testFindExceptionInCausesOf() throws Exception {
        final Throwable c = new ClassNotFoundException("c");
        final Throwable b = new Error("b", c);
        final Throwable a = new RuntimeException("a", b);
        assertThat(findExceptionInCausesOf(a, Throwable.class), is(a));
        assertThat(findExceptionInCausesOf(a, Exception.class), is(a));
        assertThat(findExceptionInCausesOf(a, Error.class), is(b));
        assertThat(findExceptionInCausesOf(a, ReflectiveOperationException.class), is(c));
        assertThat(findExceptionInCausesOf(a, ClassNotFoundException.class), is(c));
    }

    @Test
    public void testContainsException() throws Exception {
        final Throwable c = new ClassNotFoundException("c");
        final Throwable b = new Error("b", c);
        final Throwable a = new RuntimeException("a", b);
        assertThat(containsException(a, Throwable.class), is(true));
        assertThat(containsException(a, Exception.class), is(true));
        assertThat(containsException(a, Error.class), is(true));
        assertThat(containsException(a, ReflectiveOperationException.class), is(true));
        assertThat(containsException(a, ClassNotFoundException.class), is(true));

        assertThat(containsException(b, Throwable.class), is(true));
        assertThat(containsException(b, Exception.class), is(true));
        assertThat(containsException(b, Error.class), is(true));
        assertThat(containsException(b, ReflectiveOperationException.class), is(true));
        assertThat(containsException(b, ClassNotFoundException.class), is(true));

        assertThat(containsException(c, Throwable.class), is(true));
        assertThat(containsException(c, Exception.class), is(true));
        assertThat(containsException(c, Error.class), is(false));
        assertThat(containsException(c, ReflectiveOperationException.class), is(true));
        assertThat(containsException(c, ClassNotFoundException.class), is(true));
    }

    @Test
    public void testMatchesClassNotFound() throws Exception {
        assertThat(matches(new ClassNotFoundException(null), "foo.bar."), is(false));
        assertThat(matches(new ClassNotFoundException("foo.bar.x"), "foo.bar."), is(true));
        assertThat(matches(new ClassNotFoundException("foo.bars.x"), "foo.bar."), is(false));
    }

    @Test
    public void testContainsClassNotFoundException() throws Exception {
        final Throwable c = new ClassNotFoundException("foo.bar.x");
        final Throwable b = new Error("b", c);
        final Throwable a = new RuntimeException("a", b);
        assertThat(containsClassNotFoundException(a, "foo.bar."), is(true));
        assertThat(containsClassNotFoundException(a, "foo.bars."), is(false));

        assertThat(containsClassNotFoundException(b, "foo.bar."), is(true));
        assertThat(containsClassNotFoundException(b, "foo.bars."), is(false));

        assertThat(containsClassNotFoundException(c, "foo.bar."), is(true));
        assertThat(containsClassNotFoundException(c, "foo.bars."), is(false));
    }
}

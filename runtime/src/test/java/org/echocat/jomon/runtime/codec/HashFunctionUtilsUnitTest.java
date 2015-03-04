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

package org.echocat.jomon.runtime.codec;

import org.junit.Test;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.lang.reflect.InvocationTargetException;

import static java.lang.System.clearProperty;
import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
import static org.echocat.jomon.runtime.codec.HashFunctionUtils.asHexString;
import static org.echocat.jomon.runtime.codec.HashFunctionUtils.newInstanceOf;
import static org.echocat.jomon.runtime.codec.HashFunctionUtils.selectConstructorFor;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class HashFunctionUtilsUnitTest {

    @Test
    public void testSelectConstructorFor() throws Exception {
        final String oldValue = getProperty(FooHashFunction.class.getName() + ".implementation");
        try {
            clearProperty(FooHashFunction.class.getName() + ".implementation");
            assertThat(selectConstructorFor(FooHashFunction.class, FooHashFunction1.class).getDeclaringClass(), is((Object) FooHashFunction1.class));
            setProperty(FooHashFunction.class.getName() + ".implementation", FooHashFunction1.class.getName());
            assertThat(selectConstructorFor(FooHashFunction.class, FooHashFunction1.class).getDeclaringClass(), is((Object) FooHashFunction1.class));
            setProperty(FooHashFunction.class.getName() + ".implementation", FooHashFunction2.class.getName());
            assertThat(selectConstructorFor(FooHashFunction.class, FooHashFunction1.class).getDeclaringClass(), is((Object) FooHashFunction2.class));
        } finally {
            if (oldValue != null) {
                setProperty(FooHashFunction.class.getName() + ".implementation", oldValue);
            } else {
                clearProperty(FooHashFunction.class.getName() + ".implementation");
            }
        }
    }

    @Test
    public void selectConstructorForWithMissingConstructor() throws Exception {
        final String oldValue = getProperty(FooHashFunction.class.getName() + ".implementation");
        try {
            clearProperty(FooHashFunction.class.getName() + ".implementation");
            selectConstructorFor(FooHashFunction.class, FooHashFunction3.class);
            fail("Expected exception missing.");
        } catch (final IllegalArgumentException expected) {
            assertThat(expected.getMessage(), is("Illegal value of '" + FooHashFunction.class.getName() + ".implementation'. Class '" + FooHashFunction3.class.getName() + "' has no default constructor."));
        } finally {
            if (oldValue != null) {
                setProperty(FooHashFunction.class.getName() + ".implementation", oldValue);
            } else {
                clearProperty(FooHashFunction.class.getName() + ".implementation");
            }
        }
    }

    @Test
    public void selectConstructorForWithMissingImplementation() throws Exception {
        final String oldValue = getProperty(FooHashFunction.class.getName() + ".implementation");
        try {
            setProperty(FooHashFunction.class.getName() + ".implementation", "missing!");
            selectConstructorFor(FooHashFunction.class, FooHashFunction3.class);
            fail("Expected exception missing.");
        } catch (final IllegalArgumentException expected) {
            assertThat(expected.getMessage(), is("Illegal value of '" + FooHashFunction.class.getName() + ".implementation'. Could not find class for name: missing!"));
        } finally {
            if (oldValue != null) {
                setProperty(FooHashFunction.class.getName() + ".implementation", oldValue);
            } else {
                clearProperty(FooHashFunction.class.getName() + ".implementation");
            }
        }
    }

    @Test
    public void selectConstructorForWithWrongImplementation() throws Exception {
        final String oldValue = getProperty(FooHashFunction.class.getName() + ".implementation");
        try {
            setProperty(FooHashFunction.class.getName() + ".implementation", Object.class.getName());
            selectConstructorFor(FooHashFunction.class, FooHashFunction3.class);
            fail("Expected exception missing.");
        } catch (final IllegalArgumentException expected) {
            assertThat(expected.getMessage(), is("Illegal value of '" + FooHashFunction.class.getName() + ".implementation'. Class '" + Object.class.getName() + "' is not type of '" + FooHashFunction.class.getName() + "'."));
        } finally {
            if (oldValue != null) {
                setProperty(FooHashFunction.class.getName() + ".implementation", oldValue);
            } else {
                clearProperty(FooHashFunction.class.getName() + ".implementation");
            }
        }
    }

    @Test
    public void testNewInstanceOf() throws Exception {
        assertThat(newInstanceOf(FooHashFunction1.class.getConstructor()), isA(FooHashFunction1.class));
    }

    @Test
    public void testNewInstanceOfWithRuntimeException() throws Exception {
        try {
            newInstanceOf(FooHashFunction4.class.getConstructor());
            fail("Exception missing.");
        } catch (final RuntimeException expected) {
            assertThat(expected.getMessage(), is("expected!"));
        }
    }

    @Test
    public void testNewInstanceOfWithError() throws Exception {
        try {
            newInstanceOf(FooHashFunction5.class.getConstructor());
            fail("Exception missing.");
        } catch (final AssertionError expected) {
            assertThat(expected.getMessage(), is("expected!"));
        }
    }

    @Test
    public void testNewInstanceOfWithException() throws Exception {
        try {
            newInstanceOf(FooHashFunction6.class.getConstructor());
            fail("Exception missing.");
        } catch (final RuntimeException expected) {
            assertThat(expected.getCause(), isA((Class)InvocationTargetException.class));
            assertThat(expected.getCause().getCause(), isA((Class)Exception.class));
            assertThat(expected.getCause().getCause().getMessage(), is("expected!"));
        }
    }

    @Test
    public void testAsHexString() throws Exception {
        final byte[] testBytes = "Test".getBytes("UTF-8");
        assertThat(asHexString(testBytes), is("54657374"));
    }

    public static interface FooHashFunction extends HashFunction {}
    public static class FooHashFunction1 extends TestingHashFunctionSupport<FooHashFunction1> implements FooHashFunction {}
    public static class FooHashFunction2 extends TestingHashFunctionSupport<FooHashFunction2> implements FooHashFunction {}
    public static class FooHashFunction3 extends TestingHashFunctionSupport<FooHashFunction3> implements FooHashFunction {
        @SuppressWarnings({"UnusedDeclaration", "UnusedParameters"})
        private FooHashFunction3(boolean foo) {}
    }
    public static class FooHashFunction4 extends TestingHashFunctionSupport<FooHashFunction3> implements FooHashFunction {
        public FooHashFunction4() {
            throw new RuntimeException("expected!");
        }
    }
    public static class FooHashFunction5 extends TestingHashFunctionSupport<FooHashFunction3> implements FooHashFunction {
        public FooHashFunction5() {
            throw new AssertionError("expected!");
        }
    }
    public static class FooHashFunction6 extends TestingHashFunctionSupport<FooHashFunction3> implements FooHashFunction {
        public FooHashFunction6() throws Exception {
            throw new Exception("expected!");
        }
    }

    protected static class TestingHashFunctionSupport<T extends TestingHashFunctionSupport<T>> extends HashFunctionSupport<T> {

        @Nonnull
        @Override
        public T update(@Nullable byte[] with, @Nonnegative int offset, @Nonnegative int length) {
            throw new UnsupportedOperationException();
        }

        @Nonnull
        @Override
        public byte[] asBytes() {
            throw new UnsupportedOperationException();
        }

    }
}
package org.echocat.jomon.runtime.util;

import org.echocat.jomon.runtime.util.Consumer.Noop;
import org.junit.Test;

import static org.echocat.jomon.runtime.util.Consumer.ExceptionThrowing.consumeAndThrow;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ConsumerUnitTest {

    @Test
    public void testConsumeAndIgnore() throws Exception {
        Noop.<String, RuntimeException>consumeAndIgnore().consume(null);
        Noop.<String, RuntimeException>consumeAndIgnore().consume("abc");
    }

    @Test
    public void testConsumeAndThrowException() throws Exception {
        try {
            consumeAndThrow(new IllegalStateException("foo")).consume(null);
        } catch (final IllegalStateException e) {
            assertThat(e.getMessage(), is("foo"));
        }
        try {
            consumeAndThrow(OnlyWithEmptyConstructorException.class).consume(null);
        } catch (final OnlyWithEmptyConstructorException e) {
            assertThat(e.getMessage(), is((String) null));
        }
        try {
            consumeAndThrow(OnlyWithStringConstructorException.class).consume(null);
        } catch (final OnlyWithStringConstructorException e) {
            assertThat(e.getMessage(), is((String) null));
        }
        try {
            consumeAndThrow(OnlyWithStringConstructorException.class, "foo").consume(null);
        } catch (final OnlyWithStringConstructorException e) {
            assertThat(e.getMessage(), is("foo"));
        }
    }

    @Test
    public void testConsumeAndThrowExceptionWithWrongParameters() throws Exception {
        try {
            consumeAndThrow(OnlyWithEmptyConstructorException.class, "foo").consume(null);
        } catch (final IllegalArgumentException ignored) {}
        try {
            consumeAndThrow(OnlyWithIntegerConstructorException.class).consume(null);
        } catch (final IllegalArgumentException ignored) {}
        try {
            consumeAndThrow(OnlyWithIntegerConstructorException.class, "foo").consume(null);
        } catch (final IllegalArgumentException ignored) {}
    }


    public static class OnlyWithEmptyConstructorException extends Exception {}

    public static class OnlyWithStringConstructorException extends Exception {

        public OnlyWithStringConstructorException(String message) {
            super(message);
        }

    }

    public static class OnlyWithIntegerConstructorException extends Exception {

        public OnlyWithIntegerConstructorException(Integer number) {
            super(number.toString());
        }

    }


}

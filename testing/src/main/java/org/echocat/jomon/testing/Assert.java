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

package org.echocat.jomon.testing;

import org.echocat.jomon.runtime.concurrent.*;
import org.echocat.jomon.runtime.util.Duration;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.matchers.JUnitMatchers;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static org.echocat.jomon.runtime.concurrent.RetryForSpecifiedCountStrategy.retryForSpecifiedCountOf;
import static org.echocat.jomon.runtime.concurrent.RetryForSpecifiedTimeStrategy.retryForSpecifiedTimeOf;
import static org.echocat.jomon.runtime.concurrent.Retryer.executeWithRetry;
import static org.echocat.jomon.testing.BaseMatchers.is;

@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
public class Assert {

    public static final String ASSERT_CLASSNAME = Assert.class.getName();

    protected Assert() {}

    public static void assertThat(boolean actual) {
        assertThat(null, actual);
    }

    public static void assertThat(@Nullable String reason, boolean actual) {
        assertThat(reason, actual, is(true));
    }

    /**
     * Asserts that <code>actual</code> satisfies the condition specified by
     * <code>matcher</code>. If not, an {@link AssertionError} is thrown with
     * information about the matcher and failing value. Example:
     *
     * <pre>
     *   assertThat(0, is(1)); // fails:
     *     // failure message:
     *     // expected: is &lt;1&gt;
     *     // got value: &lt;0&gt;
     *   assertThat(0, is(not(1))) // passes
     * </pre>
     *
     * @param <T> the static type accepted by the matcher (this can flag obvious
     * compile-time problems such as {@code assertThat(1, is("a"))}
     * @param actual the computed value being compared
     * @param matcher an expression, built of {@link Matcher}s, specifying allowed
     * values
     * @see CoreMatchers
     * @see JUnitMatchers
     */
    @SuppressWarnings("JavaDoc")
    public static <T> void assertThat(@Nullable T actual, @Nonnull Matcher<T> matcher) {
        assertThat(null, actual, matcher);
    }

    /**
     * Asserts that <code>actual</code> satisfies the condition specified by
     * <code>matcher</code>. If not, an {@link AssertionError} is thrown with
     * the reason and information about the matcher and failing value. Example:
     *
     * <pre>
     * :
     *   assertThat(&quot;Help! Integers don't work&quot;, 0, is(1)); // fails:
     *     // failure message:
     *     // Help! Integers don't work
     *     // expected: is &lt;1&gt;
     *     // got value: &lt;0&gt;
     *   assertThat(&quot;Zero is one&quot;, 0, is(not(1))) // passes
     * </pre>
     *
     * @param reason additional information about the error
     * @param <T> the static type accepted by the matcher (this can flag obvious
     * compile-time problems such as {@code assertThat(1, is("a"))}
     * @param actual the computed value being compared
     * @param matcher an expression, built of {@link Matcher}s, specifying allowed
     * values
     * @see CoreMatchers
     * @see JUnitMatchers
     */
    @SuppressWarnings("JavaDoc")
    public static <T> void assertThat(@Nullable String reason, @Nullable T actual, @Nonnull Matcher<T> matcher) {
        if (!matcher.matches(actual)) {
            final Description description = new StringDescription();
            if (reason != null) {
                description.appendText(reason);
            }
            description.appendText("\nExpected: ");
            description.appendDescriptionOf(matcher);
            description.appendText("\n     but: ");
            matcher.describeMismatch(actual, description);

            description.appendText("\n");
            fail(description.toString());
        }
    }

    public static void fail() {
        fail(null);
    }

    public static void fail(@Nullable String message) {
        final AssertionError error = message != null ? new AssertionError(message) : new AssertionError();
        error.setStackTrace(getCleanStackTrace());
        throw error;
    }

    @Nonnull
    protected static StackTraceElement[] getCleanStackTrace() {
        final List<StackTraceElement> stackTrace = new ArrayList<>(asList(currentThread().getStackTrace()));
        stackTrace.remove(0);
        final Iterator<StackTraceElement> i = stackTrace.iterator();
        while (i.hasNext()) {
            final StackTraceElement element = i.next();
            if (shouldIgnoreClass(element)) {
                i.remove();
            } else {
                break;
            }
        }

        return stackTrace.toArray(new StackTraceElement[stackTrace.size()]);
    }

    private static boolean shouldIgnoreClass(@Nonnull StackTraceElement element) {
        final String clazz = element.getClassName();
        return clazz.equals(ASSERT_CLASSNAME)
            || clazz.startsWith(ASSERT_CLASSNAME + "$")
            || clazz.equals(Retryer.class.getName());
    }

    /**
     * Creates an {@link AssertWithRetry} that allows to test the assertion at most {@code maxNumbersOfRetries} times until it must succeed.
     * Otherwise an {@link AssertionError} is thrown.
     * This method allows a chained notation, e.g.:
     *
     * <pre>
     *     assertWithRetry(3).that("some reasons", new Callable<Boolean>() {
     *         {@literal @}Override
     *         public Boolean call() throws Exception {
     *             return readValue();
     *         }
     *     }, equalTo(5));
     * </pre>
     *
     * With Java 1.8 or later, we can use the more straightforward lamba notation:
     *
     * <pre>
     *     assertWithRetry(3).that(() -> readValue(), equalTo(5));
     * </pre>
     */
    @Nonnull
    public static AssertWithRetryRespectingException assertWithRetries(@Nonnegative int maxNumbersOfRetries) {
        final RetryForSpecifiedCountStrategy<Void> strategy = retryForSpecifiedCountOf(maxNumbersOfRetries);
        return new AssertWithRetryRespectingException(strategy);
    }

    /**
     * Creates an {@link AssertWithRetry} that allows a timeout in which the assertion must be satisfied. If the assertion still fails after this timeout
     * an {link AssertionError} is thrown.
     * This method allows a chained notation, e.g.:
     *
     * <pre>
     *     assertWithRetry("3s").that("some reasons", new Callable<Boolean>() {
     *         {@literal @}Override
     *         public Boolean call() throws Exception {
     *             return readValue();
     *         }
     *     }, equalTo(5));
     * </pre>
     *
     * With Java 1.8 or later, we can use the more straightforward lamba notation:
     *
     * <pre>
     *     assertWithRetry("3s").that(() -> readValue(), equalTo(5));
     * </pre>
     *
     * @param duration the maximum time in {@link Duration} nation in which an assertion must succeed
     */
    @Nonnull
    public static AssertWithRetryRespectingException assertWithTimeout(@Nonnull String duration) {
        return assertWithTimeout(new Duration(duration));
    }

    /**
     * Creates an {@link AssertWithRetry} that allows a timeout in which the assertion must be satisfied. If the assertion still fails after this timeout
     * an {link AssertionError} is thrown.
     * This method allows a chained notation, e.g.:
     *
     * <pre>
     *     assertWithRetry(new Duration("3s")).that("some reasons", new Callable<Boolean>() {
     *         {@literal @}Override
     *         public Boolean call() throws Exception {
     *             return readValue();
     *         }
     *     }, equalTo(5));
     * </pre>
     *
     * With Java 1.8 or later, we can use the more straightforward lamba notation:
     *
     * <pre>
     *     assertWithRetry(new Duration("3s")).that(() -> readValue(), equalTo(5));
     * </pre>
     *
     * @param duration the maximum time as a {@link Duration} in which an assertion must succeed
     */
    @Nonnull
    public static AssertWithRetryRespectingException assertWithTimeout(@Nonnull Duration duration) {
        final RetryForSpecifiedTimeStrategy<Void> strategy = retryForSpecifiedTimeOf(duration);
        return new AssertWithRetryRespectingException(strategy);
    }

    /**
     * Creates an {@link AssertWithRetry} that uses a custom {@link org.echocat.jomon.runtime.concurrent.RetryingStrategy} to satisfy a given assertion.
     * This method allows a chained notation, e.g.:
     *
     * <pre>
     *     assertWithRetry(new RetryForSpecifiedTimeStrategy("5s")).that("some reasons", new Callable<Boolean>() {
     *         {@literal @}Override
     *         public Boolean call() throws Exception {
     *             return readValue();
     *         }
     *     }, equalTo(5));
     * </pre>
     *
     * With Java 1.8 or later, we can use the more straightforward lamba notation:
     *
     * <pre>
     *     assertWithRetry(new RetryForSpecifiedTimeStrategy("5s")).that(() -> readValue(), equalTo(5));
     * </pre>
     *
     * @param retryingStrategy the strategy to retry the assertion until is either succeeds or the strategy declines a retry
     */
    @Nonnull
    public static AssertWithRetry assertWithRetry(@Nonnull RetryingStrategy<Void> retryingStrategy) {
        return new AssertWithRetry(retryingStrategy);
    }

    public static class AssertWithRetry extends AssertWithRetrySupport<RetryingStrategy<Void>> {

        public AssertWithRetry(@Nonnull RetryingStrategy<Void> retryingStrategy) {
            super(retryingStrategy);
        }

    }

    public static class AssertWithRetryRespectingException extends AssertWithRetrySupport<BaseRetryingStrategy<Void, ?>> {

        public AssertWithRetryRespectingException(@Nonnull BaseRetryingStrategy<Void, ?> retryingStrategy) {
            super(retryingStrategy);
            respecting(AssertionError.class);
        }

        @Nonnull
        public AssertWithRetryRespectingException respecting(@Nonnull Class<? extends Throwable>... exceptionTypes) {
            getRetryingStrategy().withExceptionsThatForceRetry(exceptionTypes);
            return this;
        }

        @Nonnull
        public AssertWithRetryRespectingException respecting(@Nonnull Iterable<Class<? extends Throwable>> exceptionTypes) {
            getRetryingStrategy().withExceptionsThatForceRetry(exceptionTypes);
            return this;
        }

    }

    public static class AssertWithRetrySupport<S extends RetryingStrategy<Void>> {

        @Nonnull
        private final S _retryingStrategy;

        /**
         * Creates an assertion with a {@link org.echocat.jomon.runtime.concurrent.RetryingStrategy} to satisfiy a condition. For better readability
         * use {@link org.echocat.jomon.testing.Assert}'s factory methods {@code assertWithTimeout} or {@code assertWithRetries}.
         *
         * @param retryingStrategy the {@link org.echocat.jomon.runtime.concurrent.RetryingStrategy} that will be used to satisfy a given condition
         */
        public AssertWithRetrySupport(@Nonnull S retryingStrategy) {
            _retryingStrategy = retryingStrategy;
        }

        @Nonnull
        protected S getRetryingStrategy() {
            return _retryingStrategy;
        }

        public <T> void that(@Nonnull final Callable<T> fetchActual, @Nonnull final Matcher<T> matcher) throws Exception {
            that(null, fetchActual, matcher);
        }

        /**
         * Asserts that the return value of a {@link Callable} {@code fetchActual} satisfies the condition specified by the {@code matcher}. If it doesn't, the
         * given {@link org.echocat.jomon.runtime.concurrent.RetryingStrategy} is used to try to satisfy the condition. If it cannot succeed, an
         * {@link AssertionError} is thrown.
         *
         * For instance, if we await an integer {@code x} to become {@code 5} within five seconds, we could write:
         *
         * <pre>
         *     assertWithTimeout("5s").that("some reasons", new Callable<Boolean>() {
         *         {@literal @}Override
         *         public Boolean call() throws Exception {
         *             return null;
         *         }
         *     }, equalTo(5));
         * </pre>
         *
         * With Java 1.8 or later, we can use the more straightforward lamba notation:
         *
         * <pre>
         *     assertWithTimeout("5s").that("some reasons", () -> x, equalTo(5));
         * </pre>
         *
         * @param reason additional information about the error
         * @param fetchActual a callable that return the value to be compared
         * compile-time problems such as {@code assertThat(1, is("a"))}
         * @param matcher an expression, built of {@link Matcher}s, specifying allowed
         */
        @SuppressWarnings("JavaDoc")
        public <T> void that(@Nullable final String reason, @Nonnull final Callable<T> fetchActual, @Nonnull final Matcher<T> matcher) throws Exception {
            final Callable<Void> runnable = new Callable<Void>() { @Override public Void call() throws Exception {
                assertThat(reason, fetchActual.call(), matcher);
                return null;
            }};
            executeWithRetry(runnable, _retryingStrategy, Exception.class);
        }
    }

}

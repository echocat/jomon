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

package org.echocat.jomon.testing;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.matchers.JUnitMatchers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static org.echocat.jomon.testing.BaseMatchers.is;

@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
public class Assert {

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
            if (element.getClassName().equals(Assert.class.getName())) {
                i.remove();
            } else {
                break;
            }
        }

        return stackTrace.toArray(new StackTraceElement[stackTrace.size()]);
    }

}

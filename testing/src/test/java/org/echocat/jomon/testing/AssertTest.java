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

package org.echocat.jomon.testing;

import org.echocat.jomon.runtime.util.Duration;
import org.junit.Test;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.concurrent.Callable;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.echocat.jomon.testing.Assert.assertWithRetries;
import static org.echocat.jomon.testing.Assert.assertWithTimeout;
import static org.echocat.jomon.testing.BaseMatchers.is;

@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
public class AssertTest {

    @Test
    public void canAssertWithRetries() throws Exception  {
        assertWithRetries(3).that(incrementingCallable(), is(2));
    }

    @Test(expected = AssertionError.class)
    public void failsWithRetriesIfCannotSucceed() throws Exception  {
        assertWithRetries(3).that(incrementingCallable(), is(100));
    }

    @Test
    public void canAssertWithTimeout() throws Exception {
        assertWithTimeout("4s").that(delayedValueDeliver(5, 0, "2s"), is(5));
    }

    @Test(expected = AssertionError.class)
    public void failsWithTimeoutIfCannotSucceed() throws Exception  {
        assertWithTimeout("2s").that(delayedValueDeliver(5, 0, "4s"), is(5));
    }

    @Test
    public void ignoresSpecifiedExceptions() throws Exception {
        assertWithRetries(4).respecting(MySpecialException.class).that(exceptionThrowingCallable(5, 2), is(5));
    }

    @Test(expected = MySpecialException.class)
    public void passedThroughUnspecifiedExceptions() throws Exception {
        assertWithRetries(4).that(exceptionThrowingCallable(5, 2), is(5));
    }

    private Callable<Integer> exceptionThrowingCallable(int successValue, int retriesUntilSucceeding) {
        return new MySpecialExceptionThrowingCallable(successValue, retriesUntilSucceeding);
    }

    @Nonnull
    private DelayedValueDeliver delayedValueDeliver(int successValue, int failValue, @Nonnull String duration) {
        return new DelayedValueDeliver(successValue, failValue, duration);
    }

    private IncrementingCallable incrementingCallable() {return new IncrementingCallable();}

    private static class IncrementingCallable implements Callable<Integer> {

        private int _value;

        private IncrementingCallable() { this(0); }

        private IncrementingCallable(int startValue) { _value = startValue; }

        @Override
        @Nonnull
        public Integer call() throws Exception {
            return _value++;
        }
    }

    private static class DelayedValueDeliver implements Callable<Integer> {
        private final int _successValue;
        private final int _failValue;
        @Nonnegative
        private final long _delayInMs;
        @Nonnegative
        private final long _startMillies;

        private DelayedValueDeliver(int successValue, int failValue, @Nonnull String duration) {
            _successValue = successValue;
            _failValue = failValue;
            _delayInMs = new Duration(duration).in(MILLISECONDS);
            _startMillies = currentTimeMillis();
        }

        @Override
        @Nonnull
        public Integer call() throws Exception {
            return (currentTimeMillis() - _startMillies >= _delayInMs ? _successValue : _failValue);
        }
    }

    private class MySpecialExceptionThrowingCallable implements Callable<Integer> {
        private final int _successValue;
        private int _retriesUntilSucceeding;

        private MySpecialExceptionThrowingCallable(int successValue, @Nonnegative int retriesUntilSucceeding) {
            _successValue = successValue;
            _retriesUntilSucceeding = retriesUntilSucceeding;
        }

        @Override
        public Integer call() throws Exception {
            if (_retriesUntilSucceeding > 0) {
                --_retriesUntilSucceeding;
                throw new MySpecialException();
            }

            return _successValue;
        }
    }

    private static class MySpecialException extends Exception {
    }

}
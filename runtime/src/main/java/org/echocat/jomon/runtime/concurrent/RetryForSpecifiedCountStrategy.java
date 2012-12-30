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

package org.echocat.jomon.runtime.concurrent;

import org.echocat.jomon.runtime.annotations.Excluding;
import org.echocat.jomon.runtime.annotations.Including;
import org.echocat.jomon.runtime.numbers.IntegerRangeRequirement;
import org.echocat.jomon.runtime.numbers.NumberRequirement;
import org.echocat.jomon.runtime.util.DurationRangeRequirement;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import static java.lang.Thread.currentThread;
import static org.echocat.jomon.runtime.numbers.IntegerGenerator.generateInteger;
import static org.echocat.jomon.runtime.util.DurationGenerator.generateDuration;

@ThreadSafe
public class RetryForSpecifiedCountStrategy<T> extends BaseRetryingStrategy<T, RetryForSpecifiedCountStrategy<T>> {

    @Nonnull
    public static <T> RetryForSpecifiedCountStrategy<T> retryForSpecifiedCountOf(@Nonnegative int maxNumberOfRetries) {
        return new RetryForSpecifiedCountStrategy<>(maxNumberOfRetries);
    }

    public RetryForSpecifiedCountStrategy(@Nonnegative int maxNumberOfRetries) {
        super(new DurationRangeRequirement("10ms", "5s"));
        _maxNumberOfRetries = maxNumberOfRetries;
    }

    public RetryForSpecifiedCountStrategy() {
        this(5);
    }

    private int _maxNumberOfRetries;

    @Nonnull
    public RetryForSpecifiedCountStrategy<T> withMaxNumberOfRetries(@Nonnull NumberRequirement<Integer> requirement) {
        _maxNumberOfRetries = generateInteger(requirement);
        return this;
    }

    @Nonnull
    public RetryForSpecifiedCountStrategy<T> withMaxNumberOfRetries(@Nonnull @Including Integer from, @Nonnull @Excluding Integer to) {
        _maxNumberOfRetries = generateInteger(new IntegerRangeRequirement(from, to));
        return this;
    }

    @Nonnull
    public RetryForSpecifiedCountStrategy<T> withMaxNumberOfRetries(@Nonnegative int maxNumberOfRetries) {
        _maxNumberOfRetries = maxNumberOfRetries;
        return this;
    }

    @Override
    public boolean isRetryRequiredForException(@Nonnull Throwable e, @Nonnull RetryingStatus status) {
        return isExceptionThatForceRetry(e) && status.getCurrentTry() < _maxNumberOfRetries;
    }

    @Override
    public void beforeTry(@Nonnull RetryingStatus status) {
        if (status.getCurrentTry() > 1) {
            try {
                generateDuration(getWaitBetweenEachTry()).sleep();
            } catch (InterruptedException ignored) {
                currentThread().interrupt();
            }
        }
    }

    @Nonnegative
    public int getMaxNumberOfRetries() {
        return _maxNumberOfRetries;
    }
}

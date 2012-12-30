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

import com.google.common.collect.Sets;
import org.echocat.jomon.runtime.annotations.Excluding;
import org.echocat.jomon.runtime.annotations.Including;
import org.echocat.jomon.runtime.util.Duration;
import org.echocat.jomon.runtime.util.DurationRangeRequirement;
import org.echocat.jomon.runtime.util.DurationRequirement;
import org.echocat.jomon.runtime.util.ExactDurationRequirement;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

public abstract class BaseRetryingStrategy<T, S extends BaseRetryingStrategy<T, S>> implements RetryingStrategy<T> {

    private Set<Class<? extends Throwable>> _exceptionsThatForceRetry = Sets.<Class<? extends Throwable>>newHashSet(Exception.class);
    private DurationRequirement _waitBetweenEachTry;

    protected BaseRetryingStrategy(@Nonnull DurationRequirement defaultWaitBetweenEachTry) {
        _waitBetweenEachTry = defaultWaitBetweenEachTry;
    }

    @Nonnull
    public S withExceptionsThatForceRetry(@Nonnull Class<? extends Throwable>... exceptionTypes) {
        return withExceptionsThatForceRetry(asList(exceptionTypes));
    }

    @Nonnull
    public S withExceptionsThatForceRetry(@Nonnull Iterable<Class<? extends Throwable>> exceptionTypes) {
        final Set<Class<? extends Throwable>> exceptionsThatForceRetry = new HashSet<>();
        for (Class<? extends Throwable> exceptionType : exceptionTypes) {
            exceptionsThatForceRetry.add(exceptionType);
        }
        _exceptionsThatForceRetry = unmodifiableSet(exceptionsThatForceRetry);
        return thisInstance();
    }

    @Nonnull
    public S withWaitBetweenEachTry(@Nonnull DurationRequirement requirement) {
        _waitBetweenEachTry = requirement;
        return thisInstance();
    }

    @Nonnull
    public S withWaitBetweenEachTry(@Nonnegative long duration) {
        _waitBetweenEachTry = new ExactDurationRequirement(duration);
        return thisInstance();
    }

    @Nonnull
    public S withWaitBetweenEachTry(@Nonnull String duration) {
        _waitBetweenEachTry = new ExactDurationRequirement(duration);
        return thisInstance();
    }

    @Nonnull
    public S withWaitBetweenEachTry(@Nonnull Duration duration) {
        _waitBetweenEachTry = new ExactDurationRequirement(duration);
        return thisInstance();
    }

    @Nonnull
    public S withWaitBetweenEachTry(@Nonnull @Including String from, @Nonnull @Excluding String to) {
        _waitBetweenEachTry = new DurationRangeRequirement(from, to);
        return thisInstance();
    }

    @Nonnull
    public S withWaitBetweenEachTry(@Nonnull @Including Long from, @Nonnull @Excluding Long to) {
        _waitBetweenEachTry = new DurationRangeRequirement(from, to);
        return thisInstance();
    }

    @Nonnull
    public S withWaitBetweenEachTry(@Nonnull @Including Duration from, @Nonnull @Excluding Duration to) {
        _waitBetweenEachTry = new DurationRangeRequirement(from, to);
        return thisInstance();
    }

    @Override
    public boolean isRetryRequiredForResult(@Nullable T result, @Nonnull RetryingStatus status) {
        return false;
    }

    protected boolean isExceptionThatForceRetry(@Nonnull Throwable e) {
        boolean result = false;
        for (Class<? extends Throwable> exceptionType : _exceptionsThatForceRetry) {
            if (exceptionType.isInstance(e)) {
                result = true;
                break;
            }
        }
        return result;
    }

    @Nonnull
    public Set<Class<? extends Throwable>> getExceptionsThatForceRetry() {
        return _exceptionsThatForceRetry;
    }

    @Nonnull
    public DurationRequirement getWaitBetweenEachTry() {
        return _waitBetweenEachTry;
    }

    public RetryingStrategy<T> asUnmodifiable() {
        return new Unmodifiable();
    }

    @Nonnull
    protected S thisInstance() {
        // noinspection unchecked
        return (S) this;
    }


    protected class Unmodifiable implements RetryingStrategy<T> {

        @Override
        public void beforeTry(@Nonnull RetryingStatus status) {
            BaseRetryingStrategy.this.beforeTry(status);
        }

        @Override
        public boolean isRetryRequiredForResult(@Nonnull T result, @Nonnull RetryingStatus status) {
            return BaseRetryingStrategy.this.isRetryRequiredForResult(result, status);
        }

        @Override
        public boolean isRetryRequiredForException(@Nonnull Throwable e, @Nonnull RetryingStatus status) {
            return BaseRetryingStrategy.this.isRetryRequiredForException(e, status);
        }

    }
}

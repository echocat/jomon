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

package org.echocat.jomon.runtime.concurrent;

import org.echocat.jomon.runtime.util.Duration;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class LockFactoryAdapter implements LockFactory {

    @Nonnull
    private final java.util.concurrent.locks.Lock _delegate;
    @Nonnull
    private final Lock _lock;

    public LockFactoryAdapter(@Nonnull java.util.concurrent.locks.Lock delegate) {
        _delegate = delegate;
        _lock = createLock();
    }

    @Nonnull
    protected Lock createLock() {
        return new LockAdapter();
    }

    @Nonnull
    @Override
    public Lock lock() {
        _delegate.lock();
        return _lock;
    }

    @Nonnull
    @Override
    public Lock lockInterruptibly() throws InterruptedException {
        _delegate.lockInterruptibly();
        return _lock;
    }

    @Nullable
    @Override
    public Lock tryLock() {
        return _delegate.tryLock() ? _lock : null;
    }

    @Nullable
    @Override
    public Lock tryLock(@Nonnegative long time, @Nonnull TimeUnit unit) throws InterruptedException {
        return _delegate.tryLock(time, unit) ? _lock : null;
    }

    @Nullable
    @Override
    public Lock tryLock(@Nonnull Duration timeout) throws InterruptedException {
        return _delegate.tryLock(timeout.toMilliSeconds(), MILLISECONDS) ? _lock : null;
    }

    @Nonnull
    @Override
    public Condition newCondition() {
        return _delegate.newCondition();
    }

    public class LockAdapter implements Lock {

        @Override
        public void close() {
            _delegate.unlock();
        }

    }
}

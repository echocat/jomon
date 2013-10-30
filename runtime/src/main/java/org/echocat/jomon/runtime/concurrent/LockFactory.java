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

public interface LockFactory {

    @Nonnull
    public Lock lock();

    @Nonnull
    public Lock lockInterruptibly() throws InterruptedException;

    /**
     * @return <code>null</code> if it is not possible to acquire the lock. Causes could be that another thread currently already holds the lock.
     */
    @Nullable
    public Lock tryLock();

    /**
     * @return <code>null</code> if it is not possible to acquire the lock within the given <code>timeout</code>. Causes could be that another thread currently already holds the lock.
     */
    @Nullable
    public Lock tryLock(@Nonnegative long time, @Nonnull TimeUnit unit) throws InterruptedException;

    /**
     * @return <code>null</code> if it is not possible to acquire the lock within the given <code>timeout</code>. Causes could be that another thread currently already holds the lock.
     */
    @Nullable
    public Lock tryLock(@Nonnull Duration timeout) throws InterruptedException;

    @Nonnull
    public Condition newCondition();

    public static interface Lock extends AutoCloseable {

        @Override
        public void close();
    }

}

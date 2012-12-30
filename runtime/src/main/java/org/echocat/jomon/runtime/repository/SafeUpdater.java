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

package org.echocat.jomon.runtime.repository;

import org.echocat.jomon.runtime.concurrent.RetryForSpecifiedCountStrategy;
import org.echocat.jomon.runtime.concurrent.RetryingStrategy;
import org.echocat.jomon.runtime.valuemodule.ValueModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;

import static org.echocat.jomon.runtime.concurrent.Retryer.executeWithRetry;

public class SafeUpdater {

    private static final SafeUpdater DEFAULT = safeUpdater();

    protected static final RetryingStrategy<Void> DEFAULT_STRATEGY = RetryForSpecifiedCountStrategy.<Void>retryForSpecifiedCountOf(10).withExceptionsThatForceRetry(ConcurrentModificationException.class).asUnmodifiable();

    @Nonnull
    public static SafeUpdater safe() {
        return getInstance();
    }

    @Nonnull
    public static SafeUpdater getInstance() {
        return DEFAULT;
    }

    @Nonnull
    public static SafeUpdater safeUpdater() {
        return new SafeUpdater();
    }

    private RetryingStrategy<Void> _strategy = DEFAULT_STRATEGY;

    public <V, ID, E extends ConcurrentModificationException, R extends SafeUpdatingRepository<V, E> & QueryableRepository<?, ID, V>>
    void update(@Nonnull final ID id, @Nonnull final R on, @Nonnull final Modifier<V> using) throws E, NoSuchElementException {
        executeWithRetry(new Runnable() { @Override public void run() {
            final V value = on.findOneBy(id);
            if (value == null) {
                throw new NoSuchElementException("Could not find object #" + id);
            }
            using.modifyBeforeUpdate(value);
            on.updateSafe(value);
        }}, _strategy != null ? _strategy : DEFAULT_STRATEGY);
    }

    public <V, ID, VM extends ValueModule, E extends ConcurrentModificationException, R extends ModularizedSafeUpdatingRepository<V, VM, E> & QueryableRepository<?, ID, V>>
    void update(@Nonnull final ID id, @Nonnull final R on, @Nonnull final Modifier<V> using, @Nullable final VM... valueModules) throws E, NoSuchElementException {
        executeWithRetry(new Runnable() { @Override public void run() {
            final V value = on.findOneBy(id);
            if (value == null) {
                throw new NoSuchElementException("Could not find object #" + id);
            }
            using.modifyBeforeUpdate(value);
            on.updateSafe(value, valueModules);
        }}, _strategy != null ? _strategy : DEFAULT_STRATEGY);
    }

    public SafeUpdater withRetryingStrategy(@Nonnull RetryingStrategy<Void> strategy) {
        setStrategy(strategy);
        return this;
    }

    @Nonnull
    public RetryingStrategy<Void> getStrategy() {
        return _strategy;
    }

    public void setStrategy(@Nonnull RetryingStrategy<Void> strategy) {
        defaultCheck();
        if (strategy == null) {
            throw new NullPointerException();
        }
        _strategy = strategy;
    }

    protected void defaultCheck() {
        //noinspection ObjectEquality
        if (DEFAULT == this) {
            throw new IllegalStateException("You are not allowed to modify the default instance of " + getClass().getName() + ".");
        }
    }

    public interface Modifier<V> {
        public void modifyBeforeUpdate(@Nonnull V value);
    }

}

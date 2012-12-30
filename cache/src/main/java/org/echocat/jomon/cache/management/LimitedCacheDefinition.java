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

package org.echocat.jomon.cache.management;

import org.echocat.jomon.cache.Cache;
import org.echocat.jomon.runtime.util.Duration;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class LimitedCacheDefinition<K, V, T extends LimitedCacheDefinition<K, V, T>> extends CacheDefinition<K, V, T> {

    private Long _capacity;
    private Duration _maximumLifetime;

    public LimitedCacheDefinition(@Nonnull Class<? extends Cache<?, ?>> requiredType, @Nonnull Class<K> keyType, @Nonnull Class<V> valueType) {
        super(requiredType, keyType, valueType);
    }

    @Nonnull
    public T withCapacity(@Nonnegative long capacity) {
        setCapacity(capacity);
        return thisInstance();
    }

    @Nonnull
    public T withMaximumLifetime(@Nonnull Duration maximumLifetime) {
        setMaximumLifetime(maximumLifetime);
        return thisInstance();
    }

    @Nonnull
    public T withMaximumLifetime(@Nonnull String maximumLifetime) {
        return withMaximumLifetime(new Duration(maximumLifetime));
    }

    @Nonnull
    public T withMaximumLifetime(@Nonnegative long maximumLifetime) {
        return withMaximumLifetime(new Duration(maximumLifetime));
    }

    public void setCapacity(@Nullable Long capacity) {
        _capacity = capacity;
    }

    public void setMaximumLifetime(@Nullable Duration maximumLifetime) {
        _maximumLifetime = maximumLifetime;
    }

    @Nullable
    public Long getCapacity() {
        return _capacity;
    }

    @Nullable
    public Duration getMaximumLifetime() {
        return _maximumLifetime;
    }

}

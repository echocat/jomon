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

package org.echocat.jomon.cache;

import org.echocat.jomon.runtime.util.Duration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.lang.reflect.Array.newInstance;

public class CacheListenerInvoker implements PutCacheListener, GetCacheListener, RemoveCacheListener, ClearableCacheListener, StatisticsEnabledCacheListener, LimitedCacheListener {

    public static final PutCacheListener[] EMPTY_PUT = new PutCacheListener[0];
    public static final GetCacheListener[] EMPTY_GET = new GetCacheListener[0];
    public static final RemoveCacheListener[] EMPTY_REMOVE = new RemoveCacheListener[0];
    public static final ClearableCacheListener[] EMPTY_CLEARABLE = new ClearableCacheListener[0];
    public static final StatisticsEnabledCacheListener[] EMPTY_STATISTICS = new StatisticsEnabledCacheListener[0];
    public static final LimitedCacheListener[] EMPTY_LIMITED = new LimitedCacheListener[0];

    private Collection<CacheListener> _all;

    private PutCacheListener[] _put = EMPTY_PUT;
    private GetCacheListener[] _get = EMPTY_GET;
    private RemoveCacheListener[] _remove = EMPTY_REMOVE;
    private ClearableCacheListener[] _clearable = EMPTY_CLEARABLE;
    private StatisticsEnabledCacheListener[] _statistics = EMPTY_STATISTICS;
    private LimitedCacheListener[] _limited = EMPTY_LIMITED;

    public void setListeners(@Nullable Collection<CacheListener> listeners) {
        _all = listeners;
        _put = filter(PutCacheListener.class, listeners);
        _get = filter(GetCacheListener.class, listeners);
        _remove = filter(RemoveCacheListener.class, listeners);
        _clearable = filter(ClearableCacheListener.class, listeners);
        _statistics = filter(StatisticsEnabledCacheListener.class, listeners);
        _limited = filter(LimitedCacheListener.class, listeners);
    }

    public Collection<CacheListener> getListeners() {
        return _all;
    }

    @Override
    public boolean beforePut(@Nonnull Cache<?, ?> cache, @Nullable Object key, @Nullable Value<?> value, @Nullable Duration expireAfter) {
        boolean result = true;
        final PutCacheListener[] listeners = _put;
        for (int i = 0; result && i < listeners.length; i++) {
            result = listeners[i].beforePut(cache, key, value, expireAfter);
        }
        return result;
    }

    @Override
    public void afterPut(@Nonnull Cache<?, ?> cache, @Nullable Object key, @Nullable Value<?> value, @Nullable Duration expireAfter) {
        final PutCacheListener[] listeners = _put;
        for (final PutCacheListener listener : listeners) {
            listener.afterPut(cache, key, value, expireAfter);
        }
    }

    @Override
    public boolean beforeGet(@Nonnull Cache<?, ?> cache, @Nullable Object key) {
        boolean result = true;
        final GetCacheListener[] listeners = _get;
        for (int i = 0; result && i < listeners.length; i++) {
            result = listeners[i].beforeGet(cache, key);
        }
        return result;
    }

    @Override
    public void afterGet(@Nonnull Cache<?, ?> cache, @Nullable Object key, @Nullable Value<?> value) {
        final GetCacheListener[] listeners = _get;
        for (final GetCacheListener listener : listeners) {
            listener.afterGet(cache, key, value);
        }
    }

    @Override
    public boolean beforeRemove(@Nonnull Cache<?, ?> cache, @Nullable Object key) {
        boolean result = true;
        final RemoveCacheListener[] listeners = _remove;
        for (int i = 0; result && i < listeners.length; i++) {
            result = listeners[i].beforeRemove(cache, key);
        }
        return result;
    }

    @Override
    public void afterRemove(@Nonnull Cache<?, ?> cache, @Nullable Object key, @Nullable Value<?> oldValue) {
        final RemoveCacheListener[] listeners = _remove;
        for (final RemoveCacheListener listener : listeners) {
            listener.afterRemove(cache, key, oldValue);
        }
    }

    @Override
    public boolean beforeClear(@Nonnull Cache<?, ?> cache) {
        boolean result = true;
        final ClearableCacheListener[] listeners = _clearable;
        for (int i = 0; result && i < listeners.length; i++) {
            result = listeners[i].beforeClear(cache);
        }
        return result;
    }

    @Override
    public void afterClear(@Nonnull Cache<?, ?> cache) {
        final ClearableCacheListener[] listeners = _clearable;
        for (final ClearableCacheListener listener : listeners) {
            listener.afterClear(cache);
        }
    }

    @Override
    public boolean beforeResetStatistics(@Nonnull Cache<?, ?> cache) {
        boolean result = true;
        final StatisticsEnabledCacheListener[] listeners = _statistics;
        for (int i = 0; result && i < listeners.length; i++) {
            result = listeners[i].beforeResetStatistics(cache);
        }
        return result;
    }

    @Override
    public void afterResetStatistics(@Nonnull Cache<?, ?> cache) {
        final StatisticsEnabledCacheListener[] listeners = _statistics;
        for (final StatisticsEnabledCacheListener listener : listeners) {
            listener.afterResetStatistics(cache);
        }
    }

    @Override
    public boolean beforeSetMaximumLifetime(@Nonnull Cache<?, ?> cache, @Nullable Duration millis) {
        boolean result = true;
        final LimitedCacheListener[] listeners = _limited;
        for (int i = 0; result && i < listeners.length; i++) {
            result = listeners[i].beforeSetMaximumLifetime(cache, millis);
        }
        return result;
    }

    @Override
    public void afterSetMaximumLifetime(@Nonnull Cache<?, ?> cache, @Nullable Duration millis) {
        final LimitedCacheListener[] listeners = _limited;
        for (final LimitedCacheListener listener : listeners) {
            listener.afterSetMaximumLifetime(cache, millis);
        }
    }

    @Nonnull
    protected <T extends CacheListener> T[] filter(@Nonnull Class<T> requiredType, @Nullable Collection<CacheListener> all) {
        final List<T> result = new ArrayList<>();
        if (all != null) {
            for (CacheListener listener : all) {
                if (requiredType.isInstance(listener)) {
                    result.add(requiredType.cast(listener));
                }
            }
        }
        //noinspection unchecked
        return result.toArray((T[]) newInstance(requiredType, result.size()));
    }
}

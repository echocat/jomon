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

import org.echocat.jomon.cache.Value.Fixed;
import org.echocat.jomon.runtime.iterators.CloseableIterator;
import org.echocat.jomon.runtime.util.Duration;
import org.echocat.jomon.runtime.util.ProducingType;
import org.echocat.jomon.runtime.util.ValueProducer;
import org.echocat.jomon.runtime.util.ValueProducingFailedException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.util.Map.Entry;

import static java.lang.System.currentTimeMillis;
import static org.echocat.jomon.runtime.CollectionUtils.asCloseableIterator;

/**
 * <h1>Synopsis</h1>
 * <p>Base implementation using a map and CacheEntry instances. Note that due to the ongoing reordering of elements the implementation uses a lot of
 * synchronization. The order of the elements is maintained by a linked list ({@link CacheEntry})</p>
 *
 * <p>The first and the last element is stored internally in the members {@link #_first} and {@link #_last} (surprisingly naming! ;).</p>
 */
@SuppressWarnings("ProtectedField")
@ThreadSafe
public abstract class InMemoryBasedCacheSupport<K, V> extends CacheSupport<K, V> implements StatisticsEnabledCache<K, V>, LimitedCache<K, V>, ClearableCache<K, V>, ListenerEnabledCache<K, V>, IdentifiedCache<K, V>, ProducingTypeEnabledCache<K, V>, KeysEnabledCache<K, V>, AutoCloseable {

    protected final Object _lock = new Object();
    protected final CacheListenerInvoker _listenerInvoker = new CacheListenerInvoker();
    protected final long _createdTimestamp;

    protected String _id;
    protected Map<K, CacheEntry<K, V>> _entries;
    protected Integer _capacity;
    protected Duration _defaultExpireAfter;
    private ProducingType _producingType = ProducingType.DEFAULT;

    protected long _numberOfRequests;
    protected long _numberOfHits;
    protected long _numberOfDrops;
    protected CacheEntry<K, V> _first;
    protected CacheEntry<K, V> _last;
    protected long _nearstExpiringTime;

    protected InMemoryBasedCacheSupport(@Nonnull Class<? extends K> keyType, @Nonnull Class<? extends V> valueType) {
        super(keyType, valueType);
        _createdTimestamp = currentTimeMillis();
        _entries = new HashMap<>();
        _first = null;
        _last = null;
        _numberOfHits = 0;
        _numberOfRequests = 0;
        _nearstExpiringTime = currentTimeMillis();
    }

    @Override
    public String getId() {
        return _id;
    }

    @Override
    public void setId(String id) {
        _id = id;
    }

    @Override
    public Duration getMaximumLifetime() {
        return _defaultExpireAfter;
    }

    @Override
    public Long getCapacity() {
        return _capacity != null ? _capacity.longValue() : null;
    }

    @Override
    public void setListeners(@Nullable Collection<CacheListener> listeners) {
        _listenerInvoker.setListeners(listeners);
    }

    @Override
    public Collection<CacheListener> getListeners() {
        return _listenerInvoker.getListeners();
    }

    @Override
    @Nonnull
    public ProducingType getProducingType() {
        return _producingType;
    }

    @Override
    public void setProducingType(@Nonnull ProducingType producingType) {
        if (producingType == null) {
            throw new NullPointerException();
        }
        _producingType = producingType;
    }

    /**
     * Remove the last element from the cache.
     */
    protected void removeLast() {
        final CacheEntry<K, V> entry;
        synchronized (_lock) {
            if (_last != null) {
                entry = _entries.remove(_last.getKey());
                setLast(_last.getPrevious());
            } else {
                entry = null;
            }
            if (size() == 0) {
                _first = null;
                _last = null;
            }
        }
        if (entry != null) {
            handleRemove(entry);
        }
    }

    /**
     * Remove entries that are out of their maxLifetime.
     */
    @SuppressWarnings("UnnecessaryUnboxing")
    public void cleanUpLifetimeExpired() {
        final long currentTime = currentTimeMillis();
        // Test whether there could be an element that has to be removed
        if (_nearstExpiringTime > 0 && _nearstExpiringTime <= currentTime) {
            // Go through all elements and remove elements that are outdated
            final List<K> keysToRemove = new ArrayList<>();
            synchronized (_lock) {
                _nearstExpiringTime = 0;
                for (final CacheEntry<K, V> cacheEntry : _entries.values()) {
                    final Long expire = cacheEntry.getExpire();
                    if (expire != null) {
                        final long unboxedExpire = expire.longValue();
                        if (unboxedExpire <= currentTime) {
                            keysToRemove.add(cacheEntry.getKey());
                        } else if (_nearstExpiringTime == 0 || unboxedExpire < _nearstExpiringTime) {
                            _nearstExpiringTime = unboxedExpire;
                        }
                    }
                }
                for (K key : keysToRemove) {
                    removeInternal(key);
                }
            }
        }
    }

    @Override
    public void clear() {
        if (_listenerInvoker.beforeClear(this)) {
            final Map<K, CacheEntry<K, V>> oldEntries;
            synchronized (_lock) {
                oldEntries = _entries;
                _entries = new HashMap<>();
                _first = null;
                _last = null;
                resetStatistics();
            }
            for (CacheEntry<K, V> entry : oldEntries.values()) {
                try {
                    handleRemove(entry);
                } catch (ValueProducingFailedException ignored) {}
            }
            _listenerInvoker.afterClear(this);
        }
    }

    /**
     * Is called by the {@link #get} method, must be implemented according to caching strategy.
     */
    protected abstract void updateListAfterHit(CacheEntry<K, V> entry);

    @Override
    public V get(K key) {
        checkKey(key);
        final V value;
        if (_listenerInvoker.beforeGet(this, key)) {
            CacheEntry<K, V> cacheEntry;
            CacheEntry<K, V> outdatedCacheEntry = null;
            synchronized (_lock) {
                _numberOfRequests++;
                cacheEntry = _entries.get(key);
                if (cacheEntry != null) {
                    if (isOutDated(cacheEntry)) {
                        // An outdated entry, remove it ...
                        outdatedCacheEntry = internalRemove(key);
                        cacheEntry = null;
                    } else {
                        _numberOfHits++;
                        cacheEntry.hit();
                        updateListAfterHit(cacheEntry);
                    }
                }
            }
            if (outdatedCacheEntry != null) {
                handleRemove(outdatedCacheEntry);
            }
            final Value<V> valueHolder = cacheEntry == null ? null : cacheEntry.getValue();
            value = valueHolder != null ? valueHolder.get() : null;
            _listenerInvoker.afterGet(this, key, valueHolder);
        } else {
            value = null;
        }
        return value;
    }

    @Override
    public V get(@Nullable K key, @Nullable ValueProducer<K, V> cacheValueProducer) {
        return get(key, cacheValueProducer, null);
    }

    @Override
    public V get(@Nullable K key, @Nullable ValueProducer<K, V> cacheValueProducer, @Nullable Duration expireAfter) {
        checkKey(key);
        final V value;
        if (_listenerInvoker.beforeGet(this, key)) {
            CacheEntry<K, V> cacheEntry;
            CacheEntry<K, V> outdatedCacheEntry = null;
            synchronized (_lock) {
                _numberOfRequests++;
                cacheEntry = _entries.get(key);
                if (cacheEntry != null) {
                    if (isOutDated(cacheEntry)) {
                        // An outdated entry, remove it ...
                        outdatedCacheEntry = internalRemove(key);
                        cacheEntry = null;
                    } else {
                        _numberOfHits++;
                        cacheEntry.hit();
                        updateListAfterHit(cacheEntry);
                    }
                }
                if (cacheEntry == null && cacheValueProducer != null) {
                    cacheEntry = new CacheEntry<>(key, getTargetExpireAfterBasedOn(expireAfter), cacheValueProducer, _producingType);
                    internalPut(cacheEntry);
                }
            }
            if (outdatedCacheEntry != null) {
                handleRemove(outdatedCacheEntry);
            }
            final Value<V> valueHolder = cacheEntry != null ? cacheEntry.getValue() : null;
            value = valueHolder != null ? valueHolder.get() : null;
            checkValueAfterProducing(value);
            _listenerInvoker.afterGet(this, key, valueHolder);
        } else {
            value = null;
        }
        return value;
    }

    @Override
    public boolean contains(K key) {
        checkKey(key);
        synchronized (_lock) {
            get(key);
            return _entries.containsKey(key);
        }
    }

    /**
     * This is called by the {@link #put} method, must be implemented according to caching strategy.
     */
    protected abstract void updateListAfterPut(CacheEntry<K, V> newEntry);

    @Override
    public void put(@Nullable K key, @Nullable V value) {
        put(key, value, null);
    }

    @Override
    public void put(@Nullable K key, @Nullable V value, @Nullable Duration expireAfter) {
        checkKey(key);
        checkValue(value);
        final Fixed<V> fixed = new Fixed<>(value);
        if (_listenerInvoker.beforePut(this, key, fixed, expireAfter)) {
            final Long targetExpireAfter = getTargetExpireAfterBasedOn(expireAfter);
            final CacheEntry<K, V> newEntry = new CacheEntry<>(key, targetExpireAfter, value);
            internalPut(newEntry);
            _listenerInvoker.afterPut(this, key, fixed, expireAfter);
        }
    }

    @SuppressWarnings("UnnecessaryBoxing")
    @Nullable
    protected Long getTargetExpireAfterBasedOn(@Nullable Duration expireAfter) {
        final Long targetExpireAfter;
        if (expireAfter != null) {
            targetExpireAfter = new Long(expireAfter.toMilliSeconds());
        } else {
            final Duration defaultExpireAfter = _defaultExpireAfter;
            targetExpireAfter = defaultExpireAfter != null ? new Long(defaultExpireAfter.toMilliSeconds()) : null;
        }
        return targetExpireAfter;
    }

    @Override
    @Nullable
    public Value<V> remove(@Nullable K key) {
        checkKey(key);
        final Value<V> result;
        if (_listenerInvoker.beforeRemove(this, key)) {
            result = removeInternal(key);
            _listenerInvoker.afterRemove(this, key, result);
        } else {
            result = null;
        }
        return result;
    }

    @Nullable
    protected Value<V> removeInternal(@Nullable K key) {
        final Value<V> result;
        final CacheEntry<K, V> removedCacheEntry = internalRemove(key);
        if (removedCacheEntry == null) {
            result = null;
        } else {
            result = removedCacheEntry.getValue();
            handleRemove(removedCacheEntry);
        }
        return result;
    }

    protected void handleRemove(@Nullable CacheEntry<K, V> cacheEntry) {
        _numberOfDrops++;
    }

    protected void setFirst(@Nullable CacheEntry<K, V> cacheEntry) {
        synchronized (_lock) {
            if (cacheEntry == null) {
                _first = null;
                _last = null;
            } else {
                if (_first == null) {
                    _last = cacheEntry;
                    _last.setNext(null);
                }
                _first = cacheEntry;
                _first.setPrevious(null);
            }
        }
    }

    protected void setLast(@Nullable CacheEntry<K, V> cacheEntry) {
        synchronized (_lock) {
            if (_last == null) {
                _first = cacheEntry;
                if (_first != null) {
                    _first.setPrevious(null);
                }
            }
            _last = cacheEntry;
            if (_last != null) {
                _last.setNext(null);
            }
        }
    }

    @Override
    public void setMaximumLifetime(@Nullable Duration maxLifetime) {
        if (_listenerInvoker.beforeSetMaximumLifetime(this, maxLifetime)) {
            _defaultExpireAfter = maxLifetime;
            _listenerInvoker.afterSetMaximumLifetime(this, maxLifetime);
        }
    }

    @Override
    public void setCapacity(@Nullable Long capacity) {
        if (capacity != null && capacity > Integer.valueOf(Integer.MAX_VALUE).longValue()) {
            throw new IllegalArgumentException("The capacity does not reach " + Integer.MAX_VALUE + ".");
        }
        final List<CacheEntry<K, V>> toCleanUp;
        synchronized (_lock) {
            _capacity = capacity != null ? capacity.intValue() : null;
            final Map<K, CacheEntry<K, V>> old = _entries;
            toCleanUp = new ArrayList<>();
            _entries = _capacity != null ? new HashMap<K, CacheEntry<K, V>>(_capacity, 1f) : new HashMap<K, CacheEntry<K, V>>();
            int i = 0;
            for (Entry<K, CacheEntry<K, V>> entry : old.entrySet()) {
                if (i < capacity) {
                    _entries.put(entry.getKey(), entry.getValue());
                } else {
                    toCleanUp.add(entry.getValue());
                }
                i++;
            }
        }
        for (CacheEntry<K, V> entry : toCleanUp) {
            try {
                handleRemove(entry);
            } catch (ValueProducingFailedException ignored) {
            }
        }
    }

    @Override
    public Long size() {
        synchronized (_lock) {
            return (long) _entries.size();
        }
    }

    @Override
    public Long getNumberOfHits() {
        return _numberOfHits;
    }

    @Override
    public Long getNumberOfRequests() {
        return _numberOfRequests;
    }

    @Override
    public Long getNumberOfDrops() {
        return _numberOfDrops;
    }

    @Override
    public Date getCreated() {
        return new Date(_createdTimestamp);
    }

    protected CacheEntry<K, V> getFirst() {
        synchronized (_lock) {
            return _first;
        }
    }

    protected CacheEntry<K, V> getLast() {
        synchronized (_lock) {
            return _last;
        }
    }

    protected Object getLock() {
        return _lock;
    }

    /**
     * You <b>must</b> synchronize on the {@link #getLock() semaphore}
     * while operating on the returned entries.
     */
    protected Map<K, CacheEntry<K, V>> getEntries() {
        return _entries;
    }

    private boolean isOutDated(CacheEntry<K, V> cacheEntry) {
        final Long expire = cacheEntry.getExpire();
        return expire != null && expire <= currentTimeMillis();
    }

    /**
     * @return the removed {@link CacheEntry} or null if no cache entry was removed
     */
    private CacheEntry<K, V> internalRemove(K key) {
        final CacheEntry<K, V> entry;
        synchronized (_lock) {
            if (_entries.isEmpty()) {
                _first = null;
                _last = null;
                entry = null;
            } else {
                entry = _entries.remove(key);
                if (entry != null) {
                    // Remove the entry from the LinkedList
                    // noinspection ObjectEquality
                    if (entry == _first) {
                        setFirst(_first.getNext());
                    } else {
                        //noinspection ObjectEquality
                        if (entry == _last) {
                            setLast(_last.getPrevious());
                        } else {
                            final CacheEntry<K, V> previous = entry.getPrevious();
                            final CacheEntry<K, V> next = entry.getNext();
                            previous.setNext(next);
                            next.setPrevious(previous);
                        }
                    }
                }
            }
        }
        return entry;
    }

    private void internalPut(CacheEntry<K, V> newEntry) {
        synchronized (_lock) {
            final Integer maxSize = _capacity;
            if (maxSize != null && size() >= maxSize) {
                // max size reached, remove outdated cache entries ...
                cleanUpLifetimeExpired();
                if (size() >= maxSize) {
                    // still to many cache entries, remove last entry (depends on the implementation) ...
                    removeLast();
                }
            }
            final K key = newEntry.getKey();
            final Long expire = newEntry.getExpire();
            if (_nearstExpiringTime > 0 && expire != null && expire <= _nearstExpiringTime) {
                _nearstExpiringTime = expire;
            }
            final CacheEntry<K, V> oldEntry = _entries.put(key, newEntry);
            if (oldEntry != null) {
                // This should not happen very often, but we need to ensure,
                // that there aren't two entries with the same key in the list,
                // so we put oldEntry back into the map ...
                _entries.put(key, oldEntry);
                // ... replace its value ...
                handleRemove(oldEntry);
                oldEntry.setValue(newEntry.getExpire(), newEntry.getValue());
                // ... and simulate a hit ...
                _numberOfHits++;
                oldEntry.hit();
                updateListAfterHit(oldEntry);
            } else {
                updateListAfterPut(newEntry);
            }
        }
    }

    @Override
    public void resetStatistics() {
        if (_listenerInvoker.beforeResetStatistics(this)) {
            _numberOfDrops = 0;
            _numberOfHits = 0;
            _numberOfRequests = 0;
            _listenerInvoker.afterResetStatistics(this);
        }
    }

    @Override
    public CloseableIterator<K> iterator() {
        final Set<K> keys;
        synchronized (_lock) {
            keys = new HashSet<>(_entries.keySet());
        }
        return asCloseableIterator(keys.iterator());
    }

    @Override
    public void close() throws Exception {
        clear();
    }
}

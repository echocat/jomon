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

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.concurrent.ThreadSafe;

/**
 * <p>A Cache based on the Least Recently Used algorithm. If the cache is full then the element that hasn't been access for the biggest period of time will
 * be removed.</p>
 */
@ThreadSafe
public class LruCache<K, V> extends InMemoryBasedCacheSupport<K, V> {

    public LruCache(@Nonnull Class<? extends K> keyType, @Nonnull Class<? extends V> valueType) {
        super(keyType, valueType);
    }

    /**
     * Add the new CacheEntry <b>before</b> the rest.
     */
    @Override
    @OverridingMethodsMustInvokeSuper
    protected void updateListAfterPut(CacheEntry<K, V> newEntry) {
        if (_last == null) {
            _last = newEntry;
            _first = newEntry;
        } else {
            _first.setPrevious(newEntry);
            newEntry.setNext(_first);
            setFirst(newEntry);
        }
    }

    /**
     * Move the entry to the beginning of the LinkedList.
     */
    @Override
    protected void updateListAfterHit(CacheEntry<K, V> entry) {
        if (entry != null && !entry.equals(_first)) {
            if (entry.equals(_last)) {
                setLast(entry.getPrevious());
            } else {
                final CacheEntry<K, V> previous = entry.getPrevious();
                final CacheEntry<K, V> next = entry.getNext();

                previous.setNext(next);
                next.setPrevious(previous);
            }
            _first.setPrevious(entry);
            entry.setNext(_first);
            setFirst(entry);
        }
    }
}

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
import javax.annotation.concurrent.ThreadSafe;

/**
 * <p>A Cache based on the AbstractCache. If the cache is full then the first element that has been put into the cache will be removed from the cache.</p>
 */
@ThreadSafe
public class FifoCache<K, V> extends InMemoryBasedCacheSupport<K, V> {

    public FifoCache(@Nonnull Class<? extends K> keyType, @Nonnull Class<? extends V> valueType) {
        super(keyType, valueType);
    }

    /**
     * Add the new CacheEntry *before* the rest (fifo)
     */
    @Override
    protected void updateListAfterPut(CacheEntry<K, V> newEntry) {
        if (_last == null) {
            _last = newEntry;
            _first = newEntry;
        } else {
            _first.setPrevious(newEntry);
            newEntry.setNext(_first);
            _first = newEntry;
        }
    }

    /**
     * Does nothing.
     */
    @Override
    protected void updateListAfterHit(CacheEntry<K, V> entry) {
    }

}

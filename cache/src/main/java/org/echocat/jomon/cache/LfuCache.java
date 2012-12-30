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

package org.echocat.jomon.cache;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 * <p>A Cache based on the Least Frequently Used algorithm. If the cache is full then the element that has been accessed the fewest times will be removed.</p>
 */
@ThreadSafe
public class LfuCache<K, V> extends InMemoryBasedCacheSupport<K, V> {

    public LfuCache(@Nonnull Class<? extends K> keyType, @Nonnull Class<? extends V> valueType) {
        super(keyType, valueType);
    }

    /**
     * Adds the new cache entry after <code>this&#46;_last</code>.
     */
    @Override
    protected void updateListAfterPut(CacheEntry<K, V> newEntry) {
        newEntry.setPrevious(_last);
        if (_last != null) {
            _last.setNext(newEntry);
        }
        setLast(newEntry);
    }


    /**
     * Resort the linked list. If this CacheEntry has more hits than the previous one move it up.
     */
    @Override
    protected void updateListAfterHit(CacheEntry<K, V> entry) {
        if (entry != null && !entry.equals(_first)) {
            if (entry.getHits() > entry.getPrevious().getHits()) {

                // Swap the positions
                final CacheEntry<K, V> beforePrevious = entry.getPrevious().getPrevious();
                final CacheEntry<K, V> previous = entry.getPrevious();
                final CacheEntry<K, V> next = entry.getNext();

                if (beforePrevious != null) {
                    beforePrevious.setNext(entry);
                } else {
                    _first = entry;
                }
                entry.setPrevious(beforePrevious);

                previous.setPrevious(entry);
                previous.setNext(next);

                entry.setNext(previous);

                if (next == null) {
                    setLast(previous);
                } else {
                    next.setPrevious(previous);
                }
            }
        }
    }
}

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

package org.echocat.jomon.runtime.iterators;

import org.echocat.jomon.runtime.CollectionUtils;
import org.echocat.jomon.runtime.repository.Limited;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class LimitedIterator<T> implements CloseableIterator<T> {

    private final Iterator<T> _inputIterator;
    private final Long _maximumEntriesToReturn;
    private Long _entriesReturned;

    public LimitedIterator(@Nullable Iterator<T> inputIterator, @Nullable Limited limited) {
        _inputIterator = inputIterator != null ? inputIterator : CollectionUtils.<T>emptyIterator();
        _maximumEntriesToReturn = limited != null ? limited.getMaximumOfEntriesToReturn() : null;
        _entriesReturned = 0l;
    }

    @Override
    public boolean hasNext() {
        return (_inputIterator.hasNext() && (_maximumEntriesToReturn == null || _entriesReturned < _maximumEntriesToReturn));
    }

    @Override
    public T next() {
        if(_maximumEntriesToReturn == null || _entriesReturned < _maximumEntriesToReturn) {
            _entriesReturned++;
            return _inputIterator.next();
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public void remove() {
        _inputIterator.remove();
    }

    @Override
    public void close() {
        if (_inputIterator instanceof CloseableIterator) {
            // noinspection OverlyStrongTypeCast
            ((CloseableIterator) _inputIterator).close();
        }
    }

}

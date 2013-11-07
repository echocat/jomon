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

import org.echocat.jomon.runtime.repository.Paged;

import javax.annotation.Nullable;
import java.util.Iterator;

public class PagedIterator<T> implements CloseableIterator<T> {

    private final Iterator<T> _inputIterator;

    protected PagedIterator(@Nullable Iterator<T> inputIterator, @Nullable Paged paged) {
        _inputIterator = new LimitedIterator<>(inputIterator, paged);
        skip(paged);
    }

    protected void skip(@Nullable Paged paged) {
        skip(paged != null ? paged.getEntriesToSkip() : null);
    }
    protected void skip(@Nullable Long entriesToSkip) {
        if (entriesToSkip != null) {
            for (long i = 0; i < entriesToSkip; i++) {
                if (_inputIterator.hasNext()) {
                    _inputIterator.next();
                }
            }
        }
    }

    @Override
    public void close() {
        if (_inputIterator instanceof CloseableIterator) {
            // noinspection OverlyStrongTypeCast
            ((CloseableIterator) _inputIterator).close();
        }
    }

    @Override
    public boolean hasNext() {
        return _inputIterator.hasNext();
    }

    @Override
    public T next() {
        return _inputIterator.next();
    }

    @Override
    public void remove() {
        _inputIterator.remove();
    }

}

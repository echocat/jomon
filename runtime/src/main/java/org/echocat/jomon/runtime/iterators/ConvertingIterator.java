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

import java.util.Iterator;

public abstract class ConvertingIterator<I, R> implements CloseableIterator<R> {

    private final Iterator<I> _inputIterator;

    public ConvertingIterator(Iterator<I> inputIterator) {
        _inputIterator = inputIterator;
    }

    protected abstract R convert(I input);

    @Override
    public boolean hasNext() {
        return _inputIterator.hasNext();
    }

    @Override
    public R next() {
        final I input = _inputIterator.next();
        final R output = convert(input);
        return output;
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

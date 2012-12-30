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

package org.echocat.jomon.runtime.iterators;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

/**
 * Uses a given input iterator to fetch results once hasNext is called.
 * Once it was called a batch of X given entries will be processed and return for the upcoming hasNext/next invocations'.
 * As soon as there are no further entries in output iteration further will be request in case the input iterator still has further elements.
 *
 * @param <T> type of result iteration
 * @param <S> type of input iteration
 */
public abstract class BatchConvertingIterator<S, T> implements CloseableIterator<T> {
    
    private final int _batchSize;
    private final Iterator<S> _input;
    private Iterator<T> _output;

    protected BatchConvertingIterator(@Nonnull Iterator<S> input, @Nonnegative int batchSize) {
        _input = input;
        _batchSize = batchSize;
    }

    @Nonnull
    protected abstract Iterator<T> convert(@Nonnull Collection<S> input);

    @Override
    public boolean hasNext() {
        if ((_output == null || !_output.hasNext()) && _input.hasNext()) {
            _output = prepareAndProcessBatch();
        }
        return _output != null && _output.hasNext();
    }

    @Override
    @Nullable
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException("This iterator has no further elements.");
        }
        return _output.next();
    }

    @Override
    public void remove() {
        if (_output == null) {
            throw new NoSuchElementException("This iterator has no elements.");
        }
        _output.remove();
    }

    @Nonnull
    private Iterator<T> prepareAndProcessBatch() {
        final Collection<S> batch = new ArrayList<>(_batchSize);
        // Create sub list for entries about to be processed
        for (int a = 0; a < _batchSize && _input.hasNext(); a++) {
            final S entry = _input.next();
            if (entry != null) {
                batch.add(entry);
            }
        }
        final Iterator<T> iterator = convert(batch);
        return iterator;
    }

    @Override
    public void close() {
        if (_input instanceof AutoCloseable) {
            closeQuietly((AutoCloseable) _input);
        }
    }
}

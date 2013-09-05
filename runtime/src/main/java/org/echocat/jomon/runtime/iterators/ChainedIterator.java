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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class ChainedIterator<I, O> implements CloseableIterator<O>  {

    private static final Logger LOG = LoggerFactory.getLogger(ChainedIterator.class);

    private final Iterator<I> _inputs;
    private Iterator<O> _current;

    public ChainedIterator(@Nonnull Iterator<I> inputs) {
        _inputs = inputs;
    }

    public ChainedIterator(@Nonnull Iterable<I> inputs) {
        this(inputs.iterator());
    }

    @SafeVarargs
    public ChainedIterator(@Nonnull I... inputs) {
        this(Arrays.asList(inputs));
    }

    @Override
    public boolean hasNext() {
        while ((_current == null || !_current.hasNext()) && _inputs.hasNext()) {
            beforeNext();
            final I nextInput = _inputs.next();
            _current = nextIterator(nextInput);
        }
        return _current != null && _current.hasNext();
    }

    @Override
    public O next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return _current.next();
    }

    @Nullable
    protected abstract Iterator<O> nextIterator(@Nullable I input);

    protected void beforeNext() {
        final Iterator<O> current = getCurrent();
        if (current instanceof AutoCloseable) {
            try {
                ((AutoCloseable)current).close();
            } catch (Exception e) {
                throw new RuntimeException("Could not close the other iterator '" + getCurrent() + "' before redirecting to the next iterator.", e);
            }
        }
    }

    @Override
    public void remove() {
        if (_current != null) {
            _current.remove();
        }
    }

    @Nullable
    protected Iterator<O> getCurrent() {
        return _current;
    }

    @Override
    public void close() {
        final Iterator<O> current = getCurrent();
        if (current instanceof AutoCloseable) {
            try {
                ((AutoCloseable)current).close();
            } catch (Exception ignored) {}
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } catch (Exception e) {
            LOG.warn("Could not clean close the iterator '" + this + "'. "
                + " But now this object will be cleaned by the garbage collector.", e);
        } finally {
            super.finalize();
        }
    }
}

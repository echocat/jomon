package org.echocat.jomon.cache.support;

import org.echocat.jomon.cache.Cache;
import org.echocat.jomon.runtime.iterators.CloseableIterator;
import org.echocat.jomon.runtime.util.IdEnabled;

import javax.annotation.Nonnull;
import java.util.Iterator;

import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

public class PutInCacheIterator<K, T extends IdEnabled<K>> implements CloseableIterator<T> {

    private final Iterator<T> _delegate;
    private final Cache<K, T> _cache;

    public PutInCacheIterator(@Nonnull Iterator<T> delegate, @Nonnull Cache<K, T> cache) {
        _delegate = delegate;
        _cache = cache;
    }

    @Override
    public void close() {
        if (_delegate instanceof AutoCloseable) {
            closeQuietly((AutoCloseable) _delegate);
        }
    }

    @Override
    public boolean hasNext() {
        return _delegate.hasNext();
    }

    @Override
    public T next() {
        final T next = _delegate.next();
        if (next != null) {
            _cache.put(next.getId(), next);
        }
        return next;
    }

    @Override
    public void remove() {
        _delegate.remove();
    }

}

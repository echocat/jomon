package org.echocat.jomon.cache.support;

import org.echocat.jomon.cache.Cache;
import org.echocat.jomon.runtime.CollectionUtils;
import org.echocat.jomon.runtime.iterators.CloseableIterator;
import org.echocat.jomon.runtime.util.IdEnabled;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

public abstract class OptimizedByCacheIterator<K, T extends IdEnabled<K>> implements CloseableIterator<T> {

    private final Cache<K, T> _cache;
    private final Iterable<K> _ids;

    private volatile Iterator<T> _fromCache;
    private volatile List<K> _idsNotInCache;
    private volatile Iterator<T> _delegate;

    public OptimizedByCacheIterator(@Nonnull Cache<K, T> cache, @Nonnull Iterable<K> ids) {
        _cache = cache;
        _ids = ids;
    }

    @Nullable
    protected abstract Iterator<T> getDelegateFor(@Nonnull List<K> idsNotInCache);

    @Override
    public boolean hasNext() {
        final boolean result;
        if (_fromCache == null) {
            final List<T> fromCache = new ArrayList<>();
            final List<K> idsNotInCache = new ArrayList<>();
            for (K id : _ids) {
                final T cached = _cache.get(id);
                if (cached != null) {
                    fromCache.add(cached);
                } else {
                    idsNotInCache.add(id);
                }
            }
            _fromCache = fromCache.iterator();
            _idsNotInCache = idsNotInCache;
        }
        if (_fromCache.hasNext()) {
            result = true;
        } else if (!_fromCache.hasNext() && _delegate == null) {
            final Iterator<T> delegate = getDelegateFor(_idsNotInCache);
            _delegate = new PutInCacheIterator<>(delegate != null ? delegate : CollectionUtils.<T>emptyIterator(), _cache);
            result = _delegate.hasNext();
        } else {
            result = _delegate.hasNext();
        }
        return result;
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return _delegate != null ? _delegate.next() : _fromCache.next();
    }

    @Override
    public void close() {
        if (_delegate instanceof AutoCloseable) {
            closeQuietly((AutoCloseable) _delegate);
        }
    }

    @Override public void remove() { throw new UnsupportedOperationException(); }
}

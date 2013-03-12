package org.echocat.jomon.cache.support;

import org.echocat.jomon.cache.Cache;
import org.echocat.jomon.runtime.repository.IdsOnlyEnabledQuery;
import org.echocat.jomon.runtime.util.IdEnabled;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public abstract class OptimizeQueryByCacheIterator<ID, T extends IdEnabled<ID>, Q extends IdsOnlyEnabledQuery<ID, Q>> extends OptimizedByCacheIterator<ID, T> {

    @Nonnull
    private final Q _query;

    public OptimizeQueryByCacheIterator(@Nonnull Cache<ID, T> cache, @Nonnull Q query) {
        super(cache, getIdsOf(query));
        _query = query;
    }

    @Nonnull
    private static <ID> Iterable<ID> getIdsOf(@Nonnull IdsOnlyEnabledQuery<ID, ?> query) {
        final Iterable<ID> ids = query.getIds();
        return ids != null && query.hasIdsOnly() ? ids : Collections.<ID>emptyList();
    }

    @Override
    @Nullable
    protected Iterator<T> getDelegateFor(@Nonnull List<ID> idsNotInCache) {
        return getDelegateFor( _query.asIdsOnly(idsNotInCache));
    }

    @Nullable
    protected abstract Iterator<T> getDelegateFor(@Nonnull Q query);


}

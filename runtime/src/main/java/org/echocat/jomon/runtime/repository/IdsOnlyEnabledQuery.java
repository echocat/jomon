package org.echocat.jomon.runtime.repository;

import javax.annotation.Nonnull;

public interface IdsOnlyEnabledQuery<ID, T extends IdsOnlyEnabledQuery<ID, T>> extends Query {

    public Iterable<ID> getIds();

    public boolean hasIdsOnly();

    @Nonnull
    public T asIdsOnly(@Nonnull Iterable<ID> ids);

}

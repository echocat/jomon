package org.echocat.jomon.cache.support;

import org.echocat.jomon.runtime.util.IdEnabled;

import javax.annotation.Nonnull;

public class SimpleIdEnabled implements IdEnabled<Integer> {
    private final Integer _id;

    protected SimpleIdEnabled(@Nonnull Integer id) {
        _id = id;
    }

    @Override
    @Nonnull
    public Integer getId() {
        return _id;
    }

    @Override
    @SuppressWarnings("OverlyStrongTypeCast")
    public boolean equals(Object obj) {
        boolean result = false;
        if(obj != null && obj instanceof SimpleIdEnabled) {
            result = ((SimpleIdEnabled) obj).getId().equals(_id);
        }
        return result;
    }
}

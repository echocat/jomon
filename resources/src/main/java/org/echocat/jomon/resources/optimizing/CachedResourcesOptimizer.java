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

package org.echocat.jomon.resources.optimizing;

import org.echocat.jomon.cache.Cache;
import org.echocat.jomon.cache.management.CacheProvider;
import org.echocat.jomon.resources.Resource;
import org.echocat.jomon.resources.optimizing.OptimizationContext.Feature;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import static org.echocat.jomon.cache.management.DefaultCacheDefinition.lruCache;

public abstract class CachedResourcesOptimizer implements ResourcesOptimizer {

    public static final Feature SKIP_CACHE = OptimizationContextUtils.feature(CachedResourcesOptimizer.class, "skipCache");

    private final Cache<CacheKey, Collection<Resource>> _cache;

    protected CachedResourcesOptimizer(@Nonnull CacheProvider cacheProvider) {
        // noinspection unchecked, RedundantCast
        _cache = (Cache<CacheKey, Collection<Resource>>) (Object) cacheProvider.provide(getClass(), "resources", lruCache(CacheKey.class, Collection.class).withCapacity(1000));
    }

    @Nonnull
    @Override
    public Collection<Resource> optimize(@Nonnull Collection<Resource> inputs, @Nonnull OptimizationContext context) throws Exception {
        final CacheKey cacheKey = new CacheKey(inputs, context.getFeatures());
        Collection<Resource> outputs = !context.isFeatureEnabled(SKIP_CACHE) ? _cache.get(cacheKey) : null;
        if (outputs == null || !allExists(outputs)) {
            outputs = optimizeIfAbsent(inputs, context);
            _cache.put(cacheKey, outputs);
        }
        return outputs;
    }

    protected boolean allExists(@Nonnull Collection<Resource> outputs) throws IOException {
        boolean allExists = true;
        for (final Resource output : outputs) {
            if (!output.isExisting()) {
                allExists = false;
                break;
            }
        }
        return allExists;
    }

    @Nonnull
    public abstract Collection<Resource> optimizeIfAbsent(@Nonnull Collection<Resource> inputs, @Nonnull OptimizationContext context) throws Exception;

    protected class CacheKey {
        private final Collection<Resource> _resources;
        private final Set<Feature> _features;

        public CacheKey(@Nonnull Collection<Resource> resources, @Nonnull Set<Feature> features) {
            _resources = resources;
            _features = features;
        }

        @Override
        public boolean equals(Object o) {
            final boolean result;
            if (this == o) {
                result = true;
            } else if (!(o instanceof CacheKey)) {
                result = false;
            } else {
                final CacheKey that = (CacheKey) o;
                result = _features.equals(that._features) && _resources.equals(that._resources);
            }
            return result;
        }

        @Override
        public int hashCode() {
            int result = _resources.hashCode();
            result = 31 * result + _features.hashCode();
            return result;
        }
    }
}

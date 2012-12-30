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

package org.echocat.jomon.resources.optimizing;

import org.echocat.jomon.cache.management.CacheProvider;
import org.echocat.jomon.resources.Resource;
import org.echocat.jomon.resources.ResourceType;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

public class CombinedResourcesOptimizer extends CachedResourcesOptimizer {

    private final Iterable<ResourceOptimizer> _resourceOptimizers;
    
    private Set<ResourceType> _types;
    
    private Iterable<ResourcesOptimizer> _preResourcesOptimizers;
    private Iterable<ResourcesOptimizer> _postResourcesOptimizers;

    public CombinedResourcesOptimizer(@Nonnull Iterable<ResourceOptimizer> resourceOptimizers, @Nonnull CacheProvider cacheProvider) {
        super(cacheProvider);
        _resourceOptimizers = resourceOptimizers;
        rebuildTypes();
    }

    public Iterable<ResourcesOptimizer> getPreResourcesOptimizers() {
        return _preResourcesOptimizers;
    }

    public void setPreResourcesOptimizers(Iterable<ResourcesOptimizer> preResourcesOptimizers) {
        _preResourcesOptimizers = preResourcesOptimizers;
        rebuildTypes();
    }

    public Iterable<ResourcesOptimizer> getPostResourcesOptimizers() {
        return _postResourcesOptimizers;
    }

    public void setPostResourcesOptimizers(Iterable<ResourcesOptimizer> postResourcesOptimizers) {
        _postResourcesOptimizers = postResourcesOptimizers;
        rebuildTypes();
    }

    @Nonnull
    @Override
    public Collection<Resource> optimizeIfAbsent(@Nonnull Collection<Resource> inputs, @Nonnull OptimizationContext context) throws Exception {
        final Collection<Resource> preProcessed = preProcess(inputs, context);
        final Collection<Resource> processed = process(preProcessed, context);
        final Collection<Resource> postProcessed = postProcess(processed, context);
        return postProcessed;
    }

    @Override
    public boolean isSupporting(@Nonnull ResourceType type) {
        return _types.contains(type);
    }

    @Nonnull
    @Override
    public Set<ResourceType> getSupportedResourceTypes() {
        return unmodifiableSet(_types);
    }

    @Nonnull
    protected Collection<Resource> preProcess(@Nonnull Collection<Resource> inputs, @Nonnull OptimizationContext context) throws Exception {
        Collection<Resource> outputs = inputs;
        if (_preResourcesOptimizers != null) {
            for (ResourcesOptimizer preResourcesOptimizer : _preResourcesOptimizers) {
                outputs = preResourcesOptimizer.optimize(outputs, context);
            }
        }
        return outputs;
    }

    @Nonnull
    protected Collection<Resource> process(@Nonnull Collection<Resource> inputs, @Nonnull OptimizationContext context) throws Exception {
        final Collection<Resource> outputs = new ArrayList<>();
        for (Resource input : inputs) {
            Resource resource = input;
            for (ResourceOptimizer resourceOptimizer : _resourceOptimizers) {
                if (resourceOptimizer.isSupporting(resource.getType())) {
                    resource = resourceOptimizer.optimize(resource, context);
                }
            }
            outputs.add(resource);
        }
        return outputs;
    }

    @Nonnull
    protected Collection<Resource> postProcess(@Nonnull Collection<Resource> inputs, @Nonnull OptimizationContext context) throws Exception {
        Collection<Resource> outputs = inputs;
        if (_postResourcesOptimizers != null) {
            for (ResourcesOptimizer postResourcesOptimizer : _postResourcesOptimizers) {
                outputs = postResourcesOptimizer.optimize(outputs, context);
            }
        }
        return outputs;
    }

    protected void rebuildTypes() {
        final Set<ResourceType> types = new HashSet<>();
        for (ResourceOptimizer resourceOptimizer : _resourceOptimizers) {
            types.addAll(resourceOptimizer.getSupportedResourceTypes());
        }
        if (_preResourcesOptimizers != null) {
            for (ResourcesOptimizer preResourcesOptimizer : _preResourcesOptimizers) {
                types.addAll(preResourcesOptimizer.getSupportedResourceTypes());
            }
        }
        if (_postResourcesOptimizers != null) {
            for (ResourcesOptimizer postResourcesOptimizer : _postResourcesOptimizers) {
                types.addAll(postResourcesOptimizer.getSupportedResourceTypes());
            }
        }
        _types = types;
    }
}

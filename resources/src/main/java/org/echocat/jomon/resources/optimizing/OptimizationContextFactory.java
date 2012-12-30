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

import org.echocat.jomon.resources.PathBasedResourceProvider;
import org.echocat.jomon.resources.Resource;
import org.echocat.jomon.resources.ResourceRepository;
import org.echocat.jomon.resources.ResourceRequestUriGenerator;
import org.echocat.jomon.resources.optimizing.OptimizationContext.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.util.Map.Entry;

import static com.google.common.collect.Iterables.isEmpty;
import static java.lang.Boolean.TRUE;
import static java.util.Arrays.asList;
import static java.util.Collections.synchronizedMap;
import static java.util.Collections.unmodifiableSet;

public class OptimizationContextFactory {

    private static final Logger LOG = LoggerFactory.getLogger(OptimizationContextFactory.class);

    private final ResourcesOptimizer _optimizer;
    private final PathBasedResourceProvider<Resource> _resourceProvider;
    private final ResourceRequestUriGenerator _resourceRequestUriGenerator;
    private final ResourceRepository<Resource> _resourceRepository;

    public OptimizationContextFactory(@Nonnull ResourcesOptimizer optimizer, @Nonnull PathBasedResourceProvider<Resource> resourceProvider, @Nonnull ResourceRequestUriGenerator resourceRequestUriGenerator, @Nonnull ResourceRepository<Resource> resourceRepository) {
        _optimizer = optimizer;
        _resourceProvider = resourceProvider;
        _resourceRequestUriGenerator = resourceRequestUriGenerator;
        _resourceRepository = resourceRepository;
    }

    @Nonnull
    public OptimizationContext create(@Nullable Feature... withFeatures) {
        return create(withFeatures != null ? asList(withFeatures) : null);
    }

    @Nonnull
    public OptimizationContext create(@Nullable Iterable<Feature> withFeatures) {
        final OptimizationContextImpl context = new OptimizationContextImpl();
        if (withFeatures != null) {
            for (Feature withFeature : withFeatures) {
                if (withFeature != null) {
                    context.setFeature(withFeature, true);
                }
            }
        }
        return context;
    }

    @ThreadSafe
    protected class OptimizationContextImpl implements OptimizationContext {

        private final Map<Feature, Boolean> _features = synchronizedMap(new HashMap<Feature, Boolean>());

        @Override
        public boolean isFeatureEnabled(@Nonnull Feature feature) {
            return TRUE.equals(_features.get(feature));
        }

        @Override
        public void setFeature(@Nonnull Feature feature, boolean enabled) {
            _features.put(feature, enabled);
        }

        @Override
        @Nonnull
        public Set<Feature> getFeatures() {
            final Set<Feature> features = new HashSet<>();
            synchronized (_features) {
                for (Entry<Feature, Boolean> entry : _features.entrySet()) {
                    if (TRUE.equals(entry.getValue())) {
                        features.add(entry.getKey());
                    }
                }
            }
            return unmodifiableSet(features);
        }

        @Nonnull
        @Override
        public Resource optimize(@Nonnull Resource input) throws Exception {
            final Collection<Resource> outputs = optimize(Collections.<Resource>singletonList(input));
            final Resource output;
            if (isEmpty(outputs)) {
                LOG.warn("This resource " + input + " could not be optimized by " + _optimizer + ". The optimizer returned no results. This will return the original one. This could cause other problems but we hope that this will help. Check the configuration of you optimizers to prevent this problem in the future.");
                output = input;
            } else if (outputs.size() == 1) {
                output = outputs.iterator().next();
            } else {
                LOG.warn("This resource " + input + " could not be optimized by " + _optimizer + ". The optimizer returned " + outputs.size() + " results. This will return the original one. This could cause other problems but we hope that this will help. Check the configuration of you optimizers to prevent this problem in the future.");
                output = input;
            }
            return output;
        }

        @Nonnull
        @Override
        public Collection<Resource> optimize(@Nonnull Collection<Resource> inputs) throws Exception {
            final Collection<Resource> optimized = _optimizer.optimize(inputs, this);
            for (Resource resource : optimized) {
                resource.touch();
                if (resource.isGenerated()) {
                    _resourceRepository.save(resource);
                }
            }
            return optimized;
        }

        @Override
        @Nullable
        public Resource findAndOptimizeFor(@Nonnull String uri) throws Exception {
            final Resource unoptimized = _resourceProvider.findBy(uri);
            final Resource optimized;
            if (unoptimized != null) {
                optimized = optimize(unoptimized);
            } else {
                optimized = null;
            }
            return optimized;
        }

        @Override
        @Nullable
        public String findOptimizeAndReturnUriFor(@Nonnull String uri) throws Exception {
            final Resource optimized = findAndOptimizeFor(uri);
            return optimized != null ? _resourceRequestUriGenerator.generate(optimized) : null;
        }
    }

}

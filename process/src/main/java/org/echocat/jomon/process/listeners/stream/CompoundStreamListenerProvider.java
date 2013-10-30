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

package org.echocat.jomon.process.listeners.stream;

import org.echocat.jomon.process.GeneratedProcess;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.google.common.collect.Iterables.concat;
import static java.util.ServiceLoader.load;
import static org.echocat.jomon.runtime.CollectionUtils.asImmutableList;

public class CompoundStreamListenerProvider implements StreamListenerProvider {

    @Nullable
    private final StreamListener<?> _fallback;
    @Nonnull
    private final Iterable<StreamListenerProvider> _delegates;

    public CompoundStreamListenerProvider(boolean respectSystemProviders, @Nullable StreamListenerProvider... delegates) {
        this(null, respectSystemProviders, delegates);
    }

    public CompoundStreamListenerProvider(boolean respectSystemProviders, @Nullable Iterable<StreamListenerProvider> delegates) {
        this(null, respectSystemProviders, delegates);
    }

    public CompoundStreamListenerProvider(@Nullable StreamListener<?> fallback, boolean respectSystemProviders, @Nullable StreamListenerProvider... delegates) {
        this(fallback, respectSystemProviders, asImmutableList(delegates));
    }

    public CompoundStreamListenerProvider(@Nullable StreamListener<?> fallback, boolean respectSystemProviders, @Nullable Iterable<StreamListenerProvider> delegates) {
        _fallback = fallback;
        _delegates = respectSystemProviders ? concat(delegates, createSystemProviders()) : delegates;
    }

    @Nonnull
    protected Iterable<? extends StreamListenerProvider> createSystemProviders() {
        return asImmutableList(load(StreamListenerProvider.class));
    }

    @Nullable
    @Override
    public <P extends GeneratedProcess<?, ?>> StreamListener<P> provideFor(@Nonnull Class<P> referenceType, @Nonnull String configuration) {
        StreamListener<P> result = null;
        for (StreamListenerProvider delegate : _delegates) {
            result = delegate.provideFor(referenceType, configuration);
            if (result != null) {
                break;
            }
        }
        if (result == null && _fallback != null && _fallback.canHandleReferenceType(referenceType)) {
            //noinspection unchecked
            result = (StreamListener<P>) _fallback;
        }
        return result;
    }

}

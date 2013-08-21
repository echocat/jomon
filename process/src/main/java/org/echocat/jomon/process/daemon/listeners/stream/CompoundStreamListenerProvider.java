package org.echocat.jomon.process.daemon.listeners.stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.google.common.collect.Iterables.concat;
import static java.util.ServiceLoader.load;
import static org.echocat.jomon.runtime.CollectionUtils.asImmutableList;

public class CompoundStreamListenerProvider implements StreamListenerProvider {

    @Nullable
    private final StreamListener _fallback;
    @Nonnull
    private final Iterable<StreamListenerProvider> _delegates;

    public CompoundStreamListenerProvider(boolean respectSystemProviders, @Nullable StreamListenerProvider... delegates) {
        this(null, respectSystemProviders, delegates);
    }

    public CompoundStreamListenerProvider(boolean respectSystemProviders, @Nullable Iterable<StreamListenerProvider> delegates) {
        this(null, respectSystemProviders, delegates);
    }

    public CompoundStreamListenerProvider(@Nullable StreamListener fallback, boolean respectSystemProviders, @Nullable StreamListenerProvider... delegates) {
        this(fallback, respectSystemProviders, asImmutableList(delegates));
    }

    public CompoundStreamListenerProvider(@Nullable StreamListener fallback, boolean respectSystemProviders, @Nullable Iterable<StreamListenerProvider> delegates) {
        _fallback = fallback;
        _delegates = respectSystemProviders ? concat(delegates, createSystemProviders()) : delegates;
    }

    @Nonnull
    protected Iterable<? extends StreamListenerProvider> createSystemProviders() {
        return asImmutableList(load(StreamListenerProvider.class));
    }

    @Nullable
    @Override
    public StreamListener provideFor(@Nonnull String configuration) {
        StreamListener result = null;
        for (StreamListenerProvider delegate : _delegates) {
            result = delegate.provideFor(configuration);
            if (result != null) {
                break;
            }
        }
        if (result == null && _fallback != null) {
            result = _fallback;
        }
        return result;
    }

}

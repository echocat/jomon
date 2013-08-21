package org.echocat.jomon.process.daemon.listeners.stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static java.lang.Boolean.TRUE;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.split;
import static org.echocat.jomon.runtime.CollectionUtils.asImmutableSet;

public abstract class StreamListenerProviderSupport<T extends StreamListenerSupport<T>> implements StreamListenerProvider {

    @Nonnull
    private final Class<T> _type;
    @Nonnull
    private final String _name;
    @Nonnull
    private final Set<String> _requiredParameters;

    public StreamListenerProviderSupport(@Nonnull Class<T> type, @Nonnull String name, @Nullable String... requiredParameters) {
        this(type, name, asImmutableSet(requiredParameters));
    }

    public StreamListenerProviderSupport(@Nonnull Class<T> type, @Nonnull String name, @Nullable Iterable<String> requiredParameters) {
        _type = type;
        _name = name;
        _requiredParameters = asImmutableSet(requiredParameters);
    }

    @Nullable
    @Override
    public T provideFor(@Nonnull String configuration) {
        final String[] parts = split(configuration, '|');
        final T result;
        if (parts.length > 0) {
            if (_name.equals(parts[0])) {
                final Map<String, String> parameters = toParameters(parts);
                checkForRequiredParameters(parameters);
                result = createInstanceBy(_type, parameters);
                configure(result, parameters);
            } else {
                result = null;
            }
        } else {
            result = null;
        }
        return result;
    }

    @Nonnull
    protected abstract T createInstanceBy(@Nonnull Class<T> type, @Nonnull Map<String, String> parameters);

    protected void configure(@Nonnull T instance, @Nonnull Map<String, String> parameters) {
        configureFormatter(instance, parameters);
        configureRecordProcessStarted(instance, parameters);
        configureRecordProcessTerminated(instance, parameters);
    }

    protected void configureFormatter(@Nonnull T instance, @Nonnull Map<String, String> parameters) {
        final String value = parameters.get("pattern");
        if (value != null) {
            instance.whichFormatsMessagesWith(value);
        }
    }

    protected void configureRecordProcessStarted(@Nonnull T instance, @Nonnull Map<String, String> parameters) {
        instance.whichRecordsProcessStart(getBooleanValue(parameters, "recordProcessStarted", false));
    }

    protected void configureRecordProcessTerminated(@Nonnull T instance, @Nonnull Map<String, String> parameters) {
        instance.whichRecordsProcessTermination(getBooleanValue(parameters, "recordProcessTerminated", false));
    }

    protected boolean getBooleanValue(@Nonnull Map<String, String> parameters, @Nonnull String key, boolean defaultValue) {
        return isTrue(parameters.get(key), defaultValue);
    }

    protected boolean isTrue(@Nullable String value, boolean defaultValue) {
        final String trimmedValue = value != null ? value.trim() : null;
        final boolean result;
        if (trimmedValue != null) {
            result = trimmedValue.isEmpty() || TRUE.toString().equalsIgnoreCase(trimmedValue);
        } else {
            result = defaultValue;
        }
        return result;
    }

    protected void checkForRequiredParameters(@Nonnull Map<String, String> parameters) {
        final Set<String> missingParameters = new TreeSet<>();
        for (String requiredParameter : _requiredParameters) {
            if (!parameters.containsKey(requiredParameter)) {
                missingParameters.add(requiredParameter);
            }
        }
        if (!missingParameters.isEmpty()) {
            throw new IllegalArgumentException("Required parameters missing: " + join(missingParameters));
        }
    }

    @Nonnull
    protected Map<String, String> toParameters(@Nonnull String[] parts) {
        final Map<String, String> result = new HashMap<>();
        for (int i = 1; i < parts.length; i++) {
            final String[] part = split(parts[i], "=", 2);
            if (part.length >= 1) {
                final String key = part[0].trim();
                if (!key.isEmpty()) {
                    result.put(key, part.length >= 2 ? part[1] : "");
                }
            }
        }
        return result;
    }

}

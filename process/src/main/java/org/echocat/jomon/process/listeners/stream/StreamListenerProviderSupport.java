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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static java.lang.Boolean.TRUE;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.split;
import static org.echocat.jomon.runtime.CollectionUtils.asImmutableSet;

public abstract class StreamListenerProviderSupport implements StreamListenerProvider {

    @Nonnull
    private final Class<? extends LineBasedStreamListenerSupport<?, ?>> _type;
    @Nonnull
    private final String _name;
    @Nonnull
    private final Set<String> _requiredParameters;

    @SuppressWarnings("rawtypes")
    public StreamListenerProviderSupport(@Nonnull Class<? extends LineBasedStreamListenerSupport> type, @Nonnull String name, @Nullable String... requiredParameters) {
        this(type, name, asImmutableSet(requiredParameters));
    }

    @SuppressWarnings("rawtypes")
    public StreamListenerProviderSupport(@Nonnull Class<? extends LineBasedStreamListenerSupport> type, @Nonnull String name, @Nullable Iterable<String> requiredParameters) {
        //noinspection unchecked
        _type = (Class<? extends LineBasedStreamListenerSupport<?, ?>>) (Object) type;
        _name = name;
        _requiredParameters = asImmutableSet(requiredParameters);
    }

    @Nullable
    @Override
    public <P extends GeneratedProcess<?, ?>> StreamListener<P> provideFor(@Nonnull Class<P> processType, @Nonnull String configuration) {
        final String[] parts = split(configuration, '|');
        final LineBasedStreamListenerSupport<?, ?> result;
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
        // noinspection unchecked
        return result.canHandleReferenceType(processType) ? (StreamListener<P>) result : null;
    }

    @Nonnull
    protected abstract LineBasedStreamListenerSupport<?, ?> createInstanceBy(@Nonnull Class<? extends LineBasedStreamListenerSupport<?, ?>> listenerType, @Nonnull Map<String, String> parameters);

    protected void configure(@Nonnull LineBasedStreamListenerSupport<?, ?> instance, @Nonnull Map<String, String> parameters) {
        configureFormatter(instance, parameters);
        configureRecordProcessStarted(instance, parameters);
        configureRecordProcessStartupSuccessful(instance, parameters);
        configureRecordProcessTerminated(instance, parameters);
    }

    protected void configureFormatter(@Nonnull LineBasedStreamListenerSupport<?, ?> instance, @Nonnull Map<String, String> parameters) {
        final String value = parameters.get("pattern");
        if (value != null) {
            instance.whichFormatsMessagesWith(value);
        }
    }

    protected void configureRecordProcessStarted(@Nonnull LineBasedStreamListenerSupport<?, ?> instance, @Nonnull Map<String, String> parameters) {
        if (instance instanceof LineBasedAndStateEnabledStreamListenerSupport) {
            ((LineBasedAndStateEnabledStreamListenerSupport)instance).whichRecordsProcessStart(getBooleanValue(parameters, "recordProcessStarted", false));
        }
    }

    protected void configureRecordProcessStartupSuccessful(@Nonnull LineBasedStreamListenerSupport<?, ?> instance, @Nonnull Map<String, String> parameters) {
        if (instance instanceof LineBasedAndStateEnabledStreamListenerSupport) {
            ((LineBasedAndStateEnabledStreamListenerSupport)instance).whichRecordsProcessStartupSuccessful(getBooleanValue(parameters, "recordProcessStartupSuccessful", false));
        }
    }

    protected void configureRecordProcessTerminated(@Nonnull LineBasedStreamListenerSupport<?, ?> instance, @Nonnull Map<String, String> parameters) {
        if (instance instanceof LineBasedAndStateEnabledStreamListenerSupport) {
            ((LineBasedAndStateEnabledStreamListenerSupport)instance).whichRecordsProcessTermination(getBooleanValue(parameters, "recordProcessTerminated", false));
        }
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

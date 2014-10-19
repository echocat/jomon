/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012-2014 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.runtime.util;

import org.echocat.jomon.runtime.util.ValueProvider.Support;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.*;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyMap;
import static org.echocat.jomon.runtime.CollectionUtils.asImmutableMap;
import static org.echocat.jomon.runtime.CollectionUtils.emptyIterator;

public class ValueProviderUtils {

    @Nonnull
    public static final ValueProvider EMPTY_VALUE_PROVIDER = new EmptyValueProvider();

    private ValueProviderUtils() {}

    @Nonnull
    public static ValueProvider emptyValueProvider() {
        return EMPTY_VALUE_PROVIDER;
    }

    @Nonnull
    public static ValueProvider asImmutableValueProvider(@Nullable ValueProvider original) {
        return original != null ? new ImmutableValueProvider(original) : emptyValueProvider();
    }

    @Nonnull
    public static ValueProvider asImmutableValueProvider(@Nullable Object... keyToValue) {
        final Map<Key<?>, Object> asMap = asImmutableMap(keyToValue);
        return asImmutableValueProvider(asMap);
    }

    @Nonnull
    public static ValueProvider asImmutableValueProvider(@Nullable Map<Key<?>, ?> keyToValue) {
        return new ValueProvider.Impl(keyToValue != null ? keyToValue : Collections.<Key<?>, Object>emptyMap());
    }

    @Nonnull
    public static MutableValueProvider asValueProvider(@Nullable Object... keyToValue) {
        final MutableValueProvider.Impl result = new MutableValueProvider.Impl();
        setAll(result, keyToValue);
        return result;
    }

    @Nonnull
    public static MutableValueProvider asValueProvider(@Nullable Map<Key<?>, ?> keyToValue) {
        final MutableValueProvider.Impl result = new MutableValueProvider.Impl();
        setAll(result, keyToValue);
        return result;
    }

    @Nonnull
    public static MutableValueProvider asValueProvider(@Nullable ValueProvider original) {
        final MutableValueProvider.Impl result = new MutableValueProvider.Impl();
        setAll(result, original);
        return result;
    }

    public static boolean contains(@Nullable ValueProvider provider, @Nonnull Key<?> key) {
        return provider != null ? provider.contains(key) : key.getDefaultValue() != null;
    }

    public static <T> T get(@Nullable ValueProvider provider, @Nonnull Key<T> key) {
        return get(provider, key, key.getDefaultValue());
    }

    public static <T> T get(@Nullable ValueProvider provider, @Nonnull Key<T> key, @Nullable T defaultValue) {
        final T result;
        if (provider != null) {
            result = provider.get(key, defaultValue);
        } else {
            result = defaultValue;
        }
        return result;
    }

    public static boolean isEnabled(@Nullable ValueProvider provider, @Nonnull Key<Boolean> key) {
        return isEnabled(provider, key, key.getDefaultValue());
    }

    public static boolean isEnabled(@Nullable ValueProvider provider, @Nonnull Key<Boolean> key, boolean defaultValue) {
        return TRUE.equals(get(provider, key, defaultValue));
    }

    public static <T> boolean is(@Nullable ValueProvider valueProvider, @Nonnull Key<T> key, @Nullable T expected, @Nullable T defaultValue) {
        final T value = get(valueProvider, key, defaultValue);
        return expected != null ? expected.equals(value) : value == null;
    }

    public static <T> boolean is(@Nullable ValueProvider valueProvider, @Nonnull Key<T> key, @Nullable T expected) {
        return is(valueProvider, key, expected, key.getDefaultValue());
    }


    public static <T> void set(@Nonnull MutableValueProvider provider, @Nonnull Key<T> key, @Nullable T value) {
        provider.set(key, value);
    }

    public static <T> void remove(@Nonnull MutableValueProvider provider, @Nonnull Key<T> key) {
        provider.remove(key);
    }

    public static void setAll(@Nonnull MutableValueProvider target, @Nullable ValueProvider of) {
        if (of != null) {
            for (final Entry<Key<Object>, Object> keyAndValue : of) {
                target.set(keyAndValue.getKey(), keyAndValue.getValue());
            }
        }
    }

    public static void setAll(@Nonnull MutableValueProvider target, @Nullable Map<Key<?>, ?> keyToValue) {
        if (keyToValue != null) {
            for (final Map.Entry<Key<?>, ?> keyAndValue : keyToValue.entrySet()) {
                // noinspection unchecked
                target.set((Key<Object>) keyAndValue.getKey(), keyAndValue.getValue());
            }
        }
    }

    public static void setAll(@Nonnull MutableValueProvider target, @Nullable Object... keyToValue) {
        if (keyToValue != null) {
            final int length = keyToValue.length;
            if (length % 2 == 1) {
                throw new IllegalArgumentException("You must provide an even number of arguments.");
            }
            for (int i = 0; i < length; i += 2) {
                // noinspection unchecked
                target.set((Key<Object>) keyToValue[i], keyToValue[i + 1]);
            }
        }
    }

    @ThreadSafe
    @Immutable
    public static class ImmutableValueProvider extends Support {

        @Nonnull
        private final ValueProvider _original;

        public ImmutableValueProvider(@Nonnull ValueProvider original) {
            _original = original;
        }

        @Override
        public boolean contains(@Nonnull Key<?> key) {
            return _original.contains(key);
        }

        @Override
        @Nullable
        public <T> T get(@Nonnull Key<T> key) { return _original.get(key); }

        @Override
        @Nullable
        public <T> T get(@Nonnull Key<T> key, @Nullable T defaultValue) { return _original.get(key, defaultValue); }

        @Override
        public Iterator<Entry<Key<Object>, Object>> iterator() {
            final Iterator<Entry<Key<Object>, Object>> i = _original.iterator();
            return new Iterator<Entry<Key<Object>, Object>>() {
                @Override public boolean hasNext() { return i.hasNext(); }
                @Override public Entry<Key<Object>, Object> next() { return i.next(); }
                @Override public void remove() { throw new UnsupportedOperationException(); }
            };
        }

        @Override
        public Map<Key<Object>, Object> toMap() {
            final Map<Key<Object>, Object> result = new HashMap<>();
            for (final Entry<Key<Object>, Object> keyAndValue : _original) {
                result.put(keyAndValue.getKey(), keyAndValue.getValue());
            }
            return asImmutableMap(result);
        }
    }

    @ThreadSafe
    @Immutable
    public static class EmptyValueProvider extends Support {

        @Override
        public boolean contains(@Nonnull Key<?> key) {
            return key.getDefaultValue() != null;
        }

        @Nullable
        @Override
        public <T> T get(@Nonnull Key<T> key) {
            return key.getDefaultValue();
        }

        @Nullable
        @Override
        public <T> T get(@Nonnull Key<T> key, @Nullable T defaultValue) {
            return defaultValue;
        }

        @Override
        public Iterator<Entry<Key<Object>, Object>> iterator() {
            return emptyIterator();
        }

        @Override
        public Map<Key<Object>, Object> toMap() {
            return emptyMap();
        }
    }


}

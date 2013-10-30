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

package org.echocat.jomon.runtime.util;

import org.echocat.jomon.runtime.iterators.ConvertingIterator;
import org.echocat.jomon.runtime.util.Entry.Impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static java.lang.Boolean.TRUE;
import static org.echocat.jomon.runtime.CollectionUtils.asMap;

@NotThreadSafe
public class Hints implements Iterable<Entry<Hint<?>, Object>> {

    public static final Hints EMPTY_HINTS = unmodifiableHints(new Hints());

    @Nonnull
    public static Hints unmodifiableHints(@Nullable Hints hints) {
        return new Hints(hints) {
            @Override public <T> void set(@Nonnull Hint<T> hint, @Nullable T value) { throw new UnsupportedOperationException(); }
            @Override public <T> void remove(@Nonnull Hint<T> hint) { throw new UnsupportedOperationException(); }

            @Override
            public Iterator<Entry<Hint<?>, Object>> iterator() {
                final Iterator<Entry<Hint<?>, Object>> i = super.iterator();
                return new Iterator<Entry<Hint<?>, Object>>() {
                    @Override public boolean hasNext() { return i.hasNext(); }
                    @Override public Entry<Hint<?>, Object> next() { return i.next(); }
                    @Override public void remove() { throw new UnsupportedOperationException(); }
                };
            }
        };
    }

    public static Hints hints(@Nullable Object... parameters) {
        final Map<Hint<?>, Object> plainHints = asMap(parameters);
        return new Hints(parameters != null ? plainHints : null);
    }

    @Nonnull
    public static Hints nonNullHints(@Nullable Hints hints) {
        return hints != null ? hints : EMPTY_HINTS;
    }

    private final Map<Hint<?>, Object> _hintToValue = new HashMap<>();

    public Hints() {}

    public Hints(@Nullable Hints hints) {
        setAll(hints);
    }

    public Hints(@Nullable Map<Hint<?>, Object> hints) {
        if (hints != null) {
            _hintToValue.putAll(hints);
        }
    }

    public <T> void set(@Nonnull Hint<T> hint, @Nullable T value) {
        final Class<T> valueType = hint.getValueType();
        if (value != null && !valueType.isInstance(value)) {
            throw new IllegalArgumentException("The given value is not of type " + hint.getValueType().getName() + " which is expected by " + hint + ".");
        }
        if (!hint.isNullValueAllowed() && value == null) {
            throw new NullPointerException(hint + " does not accept null values.");
        }
        getHintToValue().put(hint, value);
    }

    public void setAll(@Nullable Hints hints) {
        if (hints != null) {
            _hintToValue.putAll(hints.getHintToValue());
        }
    }

    @Nullable
    public <T> T get(@Nonnull Hint<T> hint) {
        return get(hint, hint.getDefaultValue());
    }

    @Nullable
    public static <T> T get(@Nullable Hints hints, @Nonnull Hint<T> hint) {
        return hints != null ? hints.get(hint) : null;
    }

    @Nullable
    public <T> T get(@Nonnull Hint<T> hint, @Nullable T defaultValue) {
        final Class<T> valueType = hint.getValueType();
        final Object plainValue = getHintToValue().get(hint);
        final T value = valueType.cast(plainValue);
        return value != null ? value : defaultValue;
    }

    @Nullable
    public static <T> T get(@Nullable Hints hints, @Nonnull Hint<T> hint, @Nullable T defaultValue) {
        return hints != null ? hints.get(hint, defaultValue) : null;
    }

    public boolean isEnabled(@Nonnull Hint<Boolean> hint) {
        return isEnabled(hint, hint.getDefaultValue());
    }

    public static boolean isEnabled(@Nullable Hints hints, @Nonnull Hint<Boolean> hint) {
        return isEnabled(hints, hint, hint.getDefaultValue());
    }

    public boolean isEnabled(@Nonnull Hint<Boolean> hint, boolean defaultValue) {
        final Boolean value = get(hint, defaultValue);
        return TRUE.equals(value);
    }

    public static boolean isEnabled(@Nullable Hints hints, @Nonnull Hint<Boolean> hint, boolean defaultValue) {
        return hints != null ? hints.isEnabled(hint, defaultValue) : defaultValue;
    }

    public <T> void remove(@Nonnull Hint<T> hint) {
        getHintToValue().remove(hint);
    }

    public boolean isSet(@Nonnull Hint<?> hint) {
        return getHintToValue().get(hint) != null || hint.getDefaultValue() != null;
    }

    public static boolean isSet(@Nullable Hints hints, @Nonnull Hint<?> hint) {
        return hints != null && hints.isSet(hint);
    }

    public <T> boolean is(@Nonnull Hint<T> hint, @Nullable T expected, @Nullable T defaultValue) {
        final T value = get(hint, defaultValue);
        return expected != null ? expected.equals(value) : value == null;
    }

    public static <T> boolean is(@Nullable Hints hints, @Nonnull Hint<T> hint, @Nullable T expected, @Nullable T defaultValue) {
        return hints != null && hints.is(hint, expected, defaultValue);
    }

    public <T> boolean is(@Nonnull Hint<T> hint, @Nullable T expected) {
        return is(hint, expected, hint.getDefaultValue());
    }

    public static <T> boolean is(@Nullable Hints hints, @Nonnull Hint<T> hint, @Nullable T expected) {
        return hints != null && hints.is(hint, expected);
    }

    @Override
    public Iterator<Entry<Hint<?>, Object>> iterator() {
        return new ConvertingIterator<Map.Entry<Hint<?>, Object>, Entry<Hint<?>, Object>>(getHintToValue().entrySet().iterator()) { @Override protected Entry<Hint<?>, Object> convert(Map.Entry<Hint<?>, Object> input) {
            return new Impl<Hint<?>, Object>(input.getKey(), input.getValue());
        }};
    }

    @Nonnull
    protected Map<Hint<?>, Object> getHintToValue() {
        return _hintToValue;
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else if (!(o instanceof Hints)) {
            result = false;
        } else {
            result = getHintToValue().equals(((Hints)o).getHintToValue());
        }
        return result;
    }

    @Override
    public int hashCode() {
        return getHintToValue().hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append('{');
        boolean first = true;
        for (Entry<Hint<?>, Object> hintToValue : this) {
            if (first) {
                first = false;
            } else {
               sb.append(',');
            }
            sb.append(hintToValue.getKey()).append("=").append(hintToValue.getValue());
        }
        sb.append('}');
        return sb.toString();
    }
}

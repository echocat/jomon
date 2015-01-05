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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.echocat.jomon.runtime.CollectionUtils.asMap;

@NotThreadSafe
public class Hints extends MutableValueProvider.Base {

    public static final Hints EMPTY_HINTS = unmodifiableHints(new Hints());

    @Nonnull
    public static Hints unmodifiableHints(@Nullable Hints hints) {
        return new Hints(hints) {
            @Override public <T> void set(@Nonnull Hint<T> hint, @Nullable T value) { throw new UnsupportedOperationException(); }
            @Override public <T> void remove(@Nonnull Hint<T> hint) { throw new UnsupportedOperationException(); }

            @Override
            public Iterator<Entry<Key<Object>, Object>> iterator() {
                final Iterator<Entry<Key<Object>, Object>> i = super.iterator();
                return new Iterator<Entry<Key<Object>, Object>>() {
                    @Override public boolean hasNext() { return i.hasNext(); }
                    @Override public Entry<Key<Object>, Object> next() { return i.next(); }
                    @Override public void remove() { throw new UnsupportedOperationException(); }
                };
            }
        };
    }

    @Nonnull
    public static Hints hints(@Nullable Object... parameters) {
        final Map<Hint<?>, Object> plainHints = asMap(parameters);
        return new Hints(parameters != null ? plainHints : null);
    }

    @Nonnull
    public static Hints nonNullHints(@Nullable Hints hints) {
        return hints != null ? hints : EMPTY_HINTS;
    }

    @Nonnull
    private final Map<Key<Object>, Object> _keyToValue = new HashMap<>();

    public Hints() {}

    public Hints(@Nullable Hints hints) {
        ValueProviderUtils.setAll(this, hints);
    }

    public Hints(@Nullable Map<Hint<?>, Object> hints) {
        ValueProviderUtils.setAll(this, hints);
    }

    public <T> void set(@Nonnull Hint<T> hint, @Nullable T value) {
        set((Key<T>) hint, value);
    }

    @Nullable
    public <T> T get(@Nonnull Hint<T> hint) {
        return get(hint, hint.getDefaultValue());
    }

    @Nullable
    public <T> T get(@Nonnull Hint<T> hint, @Nullable T defaultValue) {
        return get((Key<T>) hint, defaultValue);
    }

    public <T> void remove(@Nonnull Hint<T> hint) {
        remove((Key<T>) hint);
    }

    @Override
    @Nonnull
    protected Map<Key<Object>, Object> getKeyToValue() {
        return _keyToValue;
    }

    /**
     * @deprecated Use {@link ValueProviderUtils#isEnabled} in the future.
     */
    @Deprecated
    public boolean isEnabled(@Nonnull Hint<Boolean> hint) {
        return ValueProviderUtils.isEnabled(this, hint);
    }

    /**
     * @deprecated Use {@link ValueProviderUtils#isEnabled} in the future.
     */
    @Deprecated
    public boolean isEnabled(@Nonnull Hint<Boolean> hint, boolean defaultValue) {
        return ValueProviderUtils.isEnabled(this, hint, defaultValue);
    }

    /**
     * @deprecated Use {@link ValueProviderUtils#setAll} in the future.
     */
    @Deprecated
    public void setAll(@Nullable Hints hints) {
        ValueProviderUtils.setAll(this, hints);
    }

    /**
     * @deprecated Use {@link ValueProviderUtils#get} in the future.
     */
    @Deprecated
    @Nullable
    public static <T> T get(@Nullable Hints hints, @Nonnull Hint<T> hint) {
        return ValueProviderUtils.get(hints, hint);
    }

    /**
     * @deprecated Use {@link ValueProviderUtils#get} in the future.
     */
    @Deprecated
    @Nullable
    public static <T> T get(@Nullable Hints hints, @Nonnull Hint<T> hint, @Nullable T defaultValue) {
        return ValueProviderUtils.get(hints, hint, defaultValue);
    }

    /**
     * @deprecated Use {@link ValueProviderUtils#isEnabled} in the future.
     */
    @Deprecated
    public static boolean isEnabled(@Nullable Hints hints, @Nonnull Hint<Boolean> hint) {
        return ValueProviderUtils.isEnabled(hints, hint);
    }

    /**
     * @deprecated Use {@link ValueProviderUtils#isEnabled} in the future.
     */
    @Deprecated
    public static boolean isEnabled(@Nullable Hints hints, @Nonnull Hint<Boolean> hint, boolean defaultValue) {
        return ValueProviderUtils.isEnabled(hints, hint, defaultValue);
    }

    /**
     * @deprecated Use {@link ValueProvider#contains} in the future.
     */
    @Deprecated
    public boolean isSet(@Nonnull Hint<?> hint) {
        return contains(hint);
    }

    /**
     * @deprecated Use {@link ValueProviderUtils#contains} in the future.
     */
    @Deprecated
    public static boolean isSet(@Nullable Hints hints, @Nonnull Hint<?> hint) {
        return ValueProviderUtils.contains(hints, hint);
    }

    /**
     * @deprecated Use {@link ValueProviderUtils#is} in the future.
     */
    @Deprecated
    public <T> boolean is(@Nonnull Hint<T> hint, @Nullable T expected, @Nullable T defaultValue) {
        return ValueProviderUtils.is(this, hint, expected, defaultValue);
    }

    /**
     * @deprecated Use {@link ValueProviderUtils#is} in the future.
     */
    @Deprecated
    public static <T> boolean is(@Nullable Hints hints, @Nonnull Hint<T> hint, @Nullable T expected, @Nullable T defaultValue) {
        return ValueProviderUtils.is(hints, hint, expected, defaultValue);
    }

    /**
     * @deprecated Use {@link ValueProviderUtils#is} in the future.
     */
    @Deprecated
    public <T> boolean is(@Nonnull Hint<T> hint, @Nullable T expected) {
        return ValueProviderUtils.is(this, hint, expected);
    }

    /**
     * @deprecated Use {@link ValueProviderUtils#is} in the future.
     */
    @Deprecated
    public static <T> boolean is(@Nullable Hints hints, @Nonnull Hint<T> hint, @Nullable T expected) {
        return ValueProviderUtils.is(hints, hint, expected);
    }

}

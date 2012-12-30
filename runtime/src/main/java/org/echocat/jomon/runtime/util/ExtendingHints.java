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

package org.echocat.jomon.runtime.util;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.iterators.FilterIterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;

import static com.google.common.collect.Iterators.concat;

public class ExtendingHints extends Hints {

    protected static final Object NULL = new Object();

    private final Hints _superHints;

    public ExtendingHints(@Nonnull Hints superHints) {
        _superHints = superHints;
    }

    public ExtendingHints(@Nonnull Hints superHints, @Nullable Map<Hint<?>, Object> hints) {
        super(hints);
        _superHints = superHints;
    }

    public ExtendingHints(@Nonnull Hints superHints, @Nullable Hints hints) {
        super(hints);
        _superHints = superHints;
    }

    @Override
    public <T> T get(@Nonnull Hint<T> hint, @Nullable T defaultValue) {
        T result = super.get(hint, null);
        // noinspection ObjectEquality
        if (result == NULL) {
            result = null;
        } else if (result == null) {
            result = _superHints.get(hint, defaultValue);
        }
        return result != null ? result : defaultValue;
    }

    @Override
    public <T> void remove(@Nonnull Hint<T> hint) {
        // noinspection unchecked
        set(hint, (T) NULL);
    }

    @Override
    public boolean isSet(@Nonnull Hint<?> hint) {
        final Object value = getHintToValue().get(hint);
        // noinspection ObjectEquality
        return (value != null && value != NULL) || hint.getDefaultValue() != null;
    }

    @Override
    public Iterator<Entry<Hint<?>, Object>> iterator() {
        return new FilterIterator<>(concat(super.iterator(), _superHints.iterator()), new Predicate<Entry<Hint<?>, Object>>() {
            @Override
            public boolean evaluate(Entry<Hint<?>, Object> entry) {
                // noinspection ObjectEquality
                return entry != null && entry.getValue() != NULL;
            }
        });
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else if (!(o instanceof ExtendingHints) || !super.equals(o)) {
            result = false;
        } else {
            final ExtendingHints that = (ExtendingHints) o;
            result = _superHints.equals(that._superHints);
        }
        return result;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + _superHints.hashCode();
        return result;
    }
}

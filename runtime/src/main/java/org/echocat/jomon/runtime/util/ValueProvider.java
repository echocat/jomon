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

import org.echocat.jomon.runtime.iterators.ConvertingIterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.echocat.jomon.runtime.CollectionUtils.countElementsOf;

public interface ValueProvider extends Iterable<Entry<Key<Object>, Object>> {

    public boolean contains(@Nonnull Key<?> key);

    @Nullable
    public <T> T get(@Nonnull Key<T> key);

    @Nullable
    public <T> T get(@Nonnull Key<T> key, @Nullable T defaultValue);

    public Map<Key<Object>, Object> toMap();

    public abstract static class Base extends Support {

        @Override
        public boolean contains(@Nonnull Key<?> key) {
            return getKeyToValue().get((Key) key) != null || key.getDefaultValue() != null;
        }

        @Nullable
        @Override
        public <T> T get(@Nonnull Key<T> key) {
            return get(key, key.getDefaultValue());
        }

        @Nullable
        @Override
        public <T> T get(@Nonnull Key<T> key, @Nullable T defaultValue) {
            final Class<T> valueType = key.getValueType();
            // noinspection SuspiciousMethodCalls
            final Object plainValue = getKeyToValue().get(key);
            final T value = valueType.cast(plainValue);
            return value != null ? value : defaultValue;
        }

        @Override
        public Iterator<Entry<Key<Object>, Object>> iterator() {
            return new ConvertingIterator<Map.Entry<Key<Object>, Object>, Entry<Key<Object>, Object>>(getKeyToValue().entrySet().iterator()) {
                @Override
                protected Entry<Key<Object>, Object> convert(Map.Entry<Key<Object>, Object> input) {
                    return new Entry.Impl<>(input.getKey(), input.getValue());
                }
            };
        }

        @Override
        public Map<Key<Object>, Object> toMap() {
            return new HashMap<>(getKeyToValue());
        }

        @Nonnull
        protected abstract Map<Key<Object>, Object> getKeyToValue();

    }

    public abstract static class Support implements ValueProvider {

        @Override
        public boolean equals(Object o) {
            boolean result;
            if (this == o) {
                result = true;
            } else if (!(o instanceof ValueProvider)) {
                result = false;
            } else {
                result = true;
                final ValueProvider that = (ValueProvider) o;
                long numberOfKeys = 0;
                for (final Entry<Key<Object>, Object> keyAndValue : this) {
                    final Key<Object> key = keyAndValue.getKey();
                    final Object value = keyAndValue.getValue();
                    final Object thatValue = that.get(key, null);
                    result = value != null ? value.equals(thatValue) : thatValue == null;
                    if (!result) {
                        break;
                    }
                    numberOfKeys++;
                }
                if (result) {
                    final long numberOfThatKeys = countElementsOf(that.iterator());
                    result = numberOfKeys == numberOfThatKeys;
                }

            }
            return result;
        }

        @Override
        public int hashCode() {
            int result = 0;
            for (final Entry<Key<Object>, Object> keyAndValue : this) {
                final Key<Object> key = keyAndValue.getKey();
                final Object value = keyAndValue.getValue();
                result = 31 * result + (key != null ? key.getName().hashCode() : 0);
                result = 31 * result + (value != null ? value.hashCode() : 0);
            }
            return result;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append('{');
            boolean first = true;
            for (final Entry<Key<Object>, Object> keyAndValue : this) {
                if (first) {
                    first = false;
                } else {
                    sb.append(',');
                }
                sb.append(keyAndValue.getKey()).append("=").append(keyAndValue.getValue());
            }
            sb.append('}');
            return sb.toString();
        }


    }

    @ThreadSafe
    @Immutable
    public static class Impl extends Base {

        @Nonnull
        private final Map<Key<Object>, Object> _keyToValue;

        public Impl(@Nonnull Map<Key<?>, ?> keyToValue) {
            // noinspection unchecked
            _keyToValue = (Map) keyToValue;
        }

        @Nonnull
        @Override
        protected Map<Key<Object>, Object> getKeyToValue() {
            return _keyToValue;
        }

    }

}

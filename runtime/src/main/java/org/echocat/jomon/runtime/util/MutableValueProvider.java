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
import java.util.Map;

public interface MutableValueProvider extends ValueProvider {

    public <T> void set(@Nonnull Key<T> key, @Nullable T value);

    public <T> void remove(@Nonnull Key<T> key);

    public abstract static class Base extends ValueProvider.Base implements MutableValueProvider {

        @Override
        public <T> void set(@Nonnull Key<T> key, @Nullable T value) {
            final Class<T> valueType = key.getValueType();
            if (value != null && !valueType.isInstance(value)) {
                throw new IllegalArgumentException("The given value is not of type " + key.getValueType().getName() + " which is expected by " + key + ".");
            }
            if (!key.isNullValueAllowed() && value == null) {
                throw new NullPointerException(key + " does not accept null values.");
            }
            // noinspection unchecked
            getKeyToValue().put((Key<Object>) key, value);
        }

        @Override
        public <T> void remove(@Nonnull Key<T> key) {
            // noinspection SuspiciousMethodCalls
            getKeyToValue().remove(key);
        }

    }

    @NotThreadSafe
    public static class Impl extends Base {

        @Nonnull
        private final Map<Key<Object>, Object> _keyToValue;

        public Impl() {
            this(new HashMap<Key<?>, Object>());
        }

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

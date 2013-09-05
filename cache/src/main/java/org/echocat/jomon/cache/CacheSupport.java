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

package org.echocat.jomon.cache;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class CacheSupport<K, V> implements Cache<K, V> {

    private final Class<? extends K> _keyType;
    private final Class<? extends V> _valueType;

    protected CacheSupport(@Nonnull Class<? extends K> keyType, @Nonnull Class<? extends V> valueType) {
        _keyType = keyType;
        _valueType = valueType;
    }

    protected void checkKey(@Nullable K key) {
        if (key != null && !_keyType.isInstance(key)) {
            throw new IllegalArgumentException("Key '" + key + "' is not of required type: " + _keyType.getName());
        }
    }

    protected void checkValue(@Nullable V value) {
        if (value != null && !_valueType.isInstance(value)) {
            throw new IllegalArgumentException("Value '" + value + "' is not of required type: " + _valueType.getName());
        }
    }

    protected void checkValueAfterProducing(@Nullable V value) {
        if (value != null && !_valueType.isInstance(value)) {
            throw new IllegalStateException("Value '" + value + "' is not of required type: " + _valueType.getName());
        }
    }

    @Nonnull
    @Override
    public Class<? extends K> getKeyType() {
        return _keyType;
    }

    @Nonnull
    @Override
    public Class<? extends V> getValueType() {
        return _valueType;
    }
}

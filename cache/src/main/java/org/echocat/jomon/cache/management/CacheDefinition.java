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

package org.echocat.jomon.cache.management;

import org.echocat.jomon.cache.Cache;

import javax.annotation.Nonnull;

public abstract class CacheDefinition<K, V, T extends CacheDefinition<K, V, T>> {

    private final Class<? extends Cache<K, V>> _requiredType;
    private final Class<K> _keyType;
    private final Class<V> _valueType;

    protected CacheDefinition(@Nonnull Class<? extends Cache<?, ?>> requiredType, @Nonnull Class<K> keyType, @Nonnull Class<V> valueType) {
        // noinspection unchecked
        _requiredType = (Class<? extends Cache<K, V>>) requiredType;
        _keyType = keyType;
        _valueType = valueType;
    }

    @Nonnull
    public Class<? extends Cache<?, ?>> getRequiredType() {
        return _requiredType;
    }

    @Nonnull
    public Class<K> getKeyType() {
        return _keyType;
    }

    @Nonnull
    public Class<V> getValueType() {
        return _valueType;
    }

    @Nonnull
    protected T thisInstance() {
        //noinspection unchecked
        return (T) this;
    }

}

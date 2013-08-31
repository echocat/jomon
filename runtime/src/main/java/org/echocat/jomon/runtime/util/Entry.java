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

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * <h1>Synopsis</h1>
 * This represents an entry with a key and a value.
 */
public interface Entry<K, V> {

    /**
     * @return the key of this entry. It could be an implemented value of <code>K</code> or null.
     */
    public K getKey();

    /**
     * @return the value of this entry. It could be an implemented value of <code>V</code> or null.
     */
    public V getValue();

    @ThreadSafe
    @Immutable
    public static class Impl<K, V> extends BaseImpl<K, V> {

        private final V _value;

        public Impl(K key, V value) {
            super(key);
            _value = value;
        }

        @Override
        public V getValue() {
            return _value;
        }
    }

    public abstract static class BaseImpl<K, V> implements Entry<K, V> {

        private final K _key;

        protected BaseImpl(K key) {
            _key = key;
        }

        @Override
        public K getKey() {
            return _key;
        }

        @Override
        public String toString() {
            return "Entry{key=" + _key + ", value=" + getValue() + '}';
        }

        @Override
        public boolean equals(Object o) {
            final boolean result;
            if (this == o) {
                result = true;
            } else if (!(o instanceof Entry)) {
                result = false;
            } else {
                final Entry<?, ?> impl = (Entry<?, ?>) o;
                result = getKey() != null ? getKey().equals(impl.getKey()) : impl.getKey() == null;
            }
            return result;
        }

        @Override
        public int hashCode() {
            return getKey() != null ? getKey().hashCode() : 0;
        }
    }
}

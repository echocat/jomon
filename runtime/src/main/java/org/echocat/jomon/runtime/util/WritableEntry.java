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

import javax.annotation.concurrent.ThreadSafe;

public interface WritableEntry<K, V> extends Entry<K, V> {

    public void setValue(V value);

    @ThreadSafe
    public static class Impl<K, V> extends Entry.BaseImpl<K, V> implements WritableEntry<K, V> {

        private volatile V _value;

        public Impl(K key) {
            super(key);
        }

        public Impl(K key, V value) {
            this(key);
            _value = value;
        }

        @Override
        public V getValue() {
            return _value;
        }

        @Override
        public void setValue(V value) {
            _value = value;
        }
    }
}

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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;

public interface ValueProducer<K, V> {

    @Nullable
    public V produce(@Nullable K key) throws Exception;

    public static class CallableAdapter<V> implements ValueProducer<Void, V> {

        @Nullable
        public static <V> ValueProducer<Void, V> valueProducerFor(@Nullable Callable<V> callable) {
            return callable != null ? new CallableAdapter<>(callable) : null;
        }

        @Nonnull
        private final Callable<V> _callable;

        public CallableAdapter(@Nonnull Callable<V> callable) {
            _callable = callable;
        }

        @Nullable
        @Override
        public V produce(@Nullable Void key) throws Exception {
            return _callable.call();
        }

    }
}

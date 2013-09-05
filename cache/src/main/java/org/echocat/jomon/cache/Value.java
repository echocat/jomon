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

import org.echocat.jomon.runtime.util.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import static org.echocat.jomon.cache.Value.Lazy.ValueState.*;
import static org.echocat.jomon.runtime.util.ProducingType.blocking;
import static org.echocat.jomon.runtime.util.ProducingType.nonBlocking;

public interface Value<V> {

    @Nullable
    public V get();

    @ThreadSafe
    public static class Fixed<V> implements Value<V> {

        @Nullable
        public static <V> Fixed<V> fixed(@Nullable V value) {
            return value != null ? new Fixed<>(value) : null;
        }

        private final V _value;

        public Fixed(V value) {
            _value = value;
        }

        @Override
        public V get() {
            return _value;
        }

        @Override
        public String toString() {
            final V value = get();
            return value != null ? value.toString() : "null";
        }

        @Override
        public boolean equals(Object o) {
            final boolean result;
            if (this == o) {
                result = true;
            } else if (!(o instanceof Value)) {
                result = false;
            } else {
                final Value<?> that = (Value<?>) o;
                final V value = get();
                result = value != null ? value.equals(that.get()) : that.get() == null;
            }
            return result;
        }

        @Override
        public int hashCode() {
            final V value = get();
            return value != null ? value.hashCode() : 0;
        }
    }

    @ThreadSafe
    public static class Lazy<K, V> implements Value<V> {

        private final K _key;
        private final ValueProducer<K, V> _producer;
        private final ProducingType _producingType;

        private volatile V _value;
        private volatile Exception _producingException;
        private volatile ValueState _valueState = notProducedYet;

        public Lazy(@Nullable K key, @Nullable ValueProducer<K, V> producer, @Nonnull ProducingType producingType) {
            _key = key;
            _producer = producer;
            _producingType = producingType;
        }

        @Override
        public V get() {
            final V result;
            if (_valueState == produced) {
                result = _value;
            } else {
                if (_producingType == blocking) {
                    synchronized (_producer) {
                        if (_valueState == notProducedYet) {
                            callProducer();
                        } else {
                            waitWhileProducerIsActive();
                        }
                    }
                } else if (_producingType == nonBlocking) {
                    callProducer();
                } else {
                    throw new IllegalStateException("Could not handle producingType " + _producingType + ".");
                }
                if (_valueState == produced) {
                    result = _value;
                } else if (_valueState == producingFailed) {
                    throw new ValueProducingFailedException(_key, _producingException);
                } else {
                    throw new IllegalStateException("Unexpected _valueState: " + _valueState);
                }
            }
            return result;
        }

        @Nullable
        public V getWithoutProducing() {
            return _value;
        }

        @GuardedBy("_producer")
        private void callProducer() {
            _valueState = producing;
            try {
                _value = _producer.produce(_key);
                _valueState = produced;
            } catch (Exception e) {
                _producingException = e;
                _valueState = producingFailed;
            } finally {
                try {
                    if (_valueState == producing) { // ... this might be the case if an Error was thrown
                        _valueState = producingFailed;
                    }
                } finally {
                    try {
                        if (_producingType == blocking) {
                            _producer.notifyAll();
                        }
                    } finally {
                        if (_valueState == produced && _producer instanceof PostProducing) {
                            try {
                                // noinspection unchecked
                                ((PostProducing) _producer).postProducing(_key, _value);
                            } catch (Exception e) {
                                _producingException = e;
                                _valueState = producingFailed;
                            }
                        }

                    }
                }
            }
        }

        @GuardedBy("_producer")
        private void waitWhileProducerIsActive() {
            if (_producingType != blocking) {
                throw new IllegalStateException("This method could only be used if producingType is " + blocking + ".");
            }
            while (_valueState == producing) {
                try {
                    _producer.wait(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new GotInterruptedException("Got interrupted while waiting for production of cache value for key " + _key + ".", e);
                }
            }
        }

        static enum ValueState {
            notProducedYet,
            producing,
            produced,
            producingFailed
        }
    }
}

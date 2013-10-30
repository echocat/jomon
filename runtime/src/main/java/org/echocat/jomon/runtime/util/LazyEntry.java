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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.GuardedBy;

import static org.echocat.jomon.runtime.util.LazyEntry.ValueState.*;

public class LazyEntry<K, V> extends Entry.BaseImpl<K, V> implements WritableEntry<K, V> {

    private static final Logger LOG = LoggerFactory.getLogger(LazyEntry.class);

    private final ValueProducer<K, V> _producer;

    private volatile V _value;
    private volatile Exception _producingException;
    private volatile ValueState _valueState = notProducedYet;

    public LazyEntry(K key, ValueProducer<K, V> producer) {
        super(key);
        _producer = producer;
    }

    @Override
    public V getValue() {
        final V result;
        if (_valueState == produced) {
            result = _value;
        } else {
            synchronized (_producer) {
                waitWhileProducerIsActive();
                if (_valueState == notProducedYet) {
                    callProducer();
                }
            }
            if (_valueState == produced) {
                result = _value;
            } else if (_valueState == producingFailed) {
                throw new ValueProducingFailedException(getKey(), _producingException);
            } else {
                throw new IllegalStateException("Unexpected valueState: " + _valueState);
            }
        }
        return result;
    }

    @Override
    public void setValue(V value) {
        synchronized (_producer) {
            waitWhileProducerIsActive();
            _value = value;
            _valueState = produced;
        }
    }

    @GuardedBy("_producer")
    protected void callProducer() {
        _valueState = producing;
        try {
            _value = _producer.produce(getKey());
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
                    if (_valueState == produced && _producer instanceof PostProducing) {
                        try {
                            // noinspection unchecked
                            ((PostProducing) _producer).postProducing(getKey(), _value);
                        } catch (Exception e) {
                            _producingException = e;
                            _valueState = producingFailed;
                        }
                    }
                } finally {
                    _producer.notifyAll();
                }
            }
        }
    }

    @GuardedBy("_producer")
    protected void waitWhileProducerIsActive() {
        while (_valueState == producing) {
            try {
                _producer.wait(10000);
                if (_valueState == producing) {
                    LOG.warn("Still waiting for valueProducer " + _producer + " to produce value for key " + getKey() + "...");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new GotInterruptedException("Got interrupted while waiting for production of cache value for key " + getKey() + ".", e);
            }
        }
    }

    public static enum ValueState {
        notProducedYet,
        producing,
        produced,
        producingFailed
    }
}

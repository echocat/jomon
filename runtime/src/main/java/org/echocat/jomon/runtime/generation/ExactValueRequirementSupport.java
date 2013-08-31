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

package org.echocat.jomon.runtime.generation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlAdapter;

@ThreadSafe
@Immutable
public abstract class ExactValueRequirementSupport<T> implements ExactValueRequirement<T> {

    private final T _value;

    protected ExactValueRequirementSupport(@Nullable T value) {
        _value = value;
    }

    @Override
    @Nullable
    public T getValue() {
        return _value;
    }

    protected abstract static class Container<T> {

        private T _value;

        @XmlAttribute(name = "value")
        public T getValue() {
            return _value;
        }

        public void setValue(T value) {
            _value = value;
        }
    }

    protected abstract static class Adapter<T, C extends Container<T>, R extends ExactValueRequirementSupport<T>> extends XmlAdapter<C, R> {

        @Override
        public R unmarshal(C v) throws Exception {
            final R result;
            if (v != null) {
                result = newExactValueRequirement(v.getValue());
            } else {
                result = null;
            }
            return result;
        }

        @Override
        public C marshal(R v) throws Exception {
            final C result;
            if (v != null) {
                result = newContainer();
                result.setValue(v.getValue());
            } else {
                result = null;
            }
            return result;
        }

        @Nonnull
        protected abstract C newContainer();

        @Nonnull
        protected abstract R newExactValueRequirement(@Nullable T value);
    }



}

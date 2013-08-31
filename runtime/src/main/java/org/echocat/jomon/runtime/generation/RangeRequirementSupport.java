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

import org.echocat.jomon.runtime.annotations.Excluding;
import org.echocat.jomon.runtime.annotations.Including;
import org.echocat.jomon.runtime.util.Range;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlAdapter;

@ThreadSafe
@Immutable
public abstract class RangeRequirementSupport<T, R extends Range<T>> implements RangeRequirement<T, R> {

    private final R _range;

    protected RangeRequirementSupport(@Nonnull R range) {
        _range = range;
    }

    @Override
    @Nonnull
    public R getValue() {
        return _range;
    }

    protected abstract static class Container<T> {

        private T _from;
        private T _to;

        @XmlAttribute(name = "from")
        public T getFrom() {
            return _from;
        }

        public void setFrom(T from) {
            _from = from;
        }

        @XmlAttribute(name = "to")
        public T getTo() {
            return _to;
        }

        public void setTo(T to) {
            _to = to;
        }
    }

    protected abstract static class Adapter<T, C extends Container<T>, R extends Range<T>> extends XmlAdapter<C, R> {

        @Override
        public R unmarshal(C v) throws Exception {
            final R result;
            if (v != null) {
                result = newRangeRequirement(v.getFrom(), v.getTo());
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
                result.setFrom(v.getFrom());
                result.setTo(v.getTo());
            } else {
                result = null;
            }
            return result;
        }

        @Nonnull
        protected abstract C newContainer();

        @Nonnull
        protected abstract R newRangeRequirement(@Nullable @Including T from, @Nullable @Excluding T to);
    }



}

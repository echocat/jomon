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

package org.echocat.jomon.runtime.numbers;

import org.echocat.jomon.runtime.annotations.Excluding;
import org.echocat.jomon.runtime.annotations.Including;
import org.echocat.jomon.runtime.util.RangeSupport;
import org.echocat.jomon.runtime.util.SignificantableWithMinAndMaxBasedOn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.bind.annotation.adapters.XmlAdapter;

@ThreadSafe
@Immutable
public abstract class NumberRange<N extends Number> extends RangeSupport<N> implements SignificantableWithMinAndMaxBasedOn<N, N> {

    protected NumberRange(@Nullable @Including N from, @Nullable @Excluding N to) {
        super(from, to);
        if (from != null && to != null && isGreaterThan(from, to)) {
            throw new IllegalArgumentException("From value (" + from + ") is greater than to value (" + to + ")");
        }
    }

    @Override
    public boolean apply(@Nullable N toTest) {
        return matchesFrom(toTest) && matchesTo(toTest);
    }

    protected boolean matchesFrom(@Nonnull N toTest) {
        final N from = getFrom();
        return from == null || isGreaterThan(toTest, from) || from.equals(toTest);
    }

    protected boolean matchesTo(@Nonnull N toTest) {
        final N to = getTo();
        return to == null || isGreaterThan(to, toTest);
    }

    protected abstract boolean isGreaterThan(@Nonnull N what, @Nonnull N as);

    @Override
    public abstract boolean isSignificant(@Nonnull N minValue, @Nonnull N maxValue, @Nonnull N base);

    protected abstract static class Container<N extends Number> {

        private N _from;
        private N _to;

        public N getFrom() {
            return _from;
        }

        public void setFrom(N from) {
            _from = from;
        }

        public N getTo() {
            return _to;
        }

        public void setTo(N to) {
            _to = to;
        }
    }

    protected abstract static class Adapter<N extends Number, C extends Container<N>, NR extends NumberRange<N>> extends XmlAdapter<C, NR> {

        @Override
        public NR unmarshal(C v) throws Exception {
            final NR result;
            if (v != null) {
                result = newNumberRange(v.getFrom(), v.getTo());
            } else {
                result = null;
            }
            return result;
        }

        @Override
        public C marshal(NR v) throws Exception {
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
        protected abstract NR newNumberRange(@Nullable @Including N from, @Nullable @Excluding N to);
    }
}

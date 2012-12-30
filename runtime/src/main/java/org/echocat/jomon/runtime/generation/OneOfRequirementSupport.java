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

package org.echocat.jomon.runtime.generation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

@ThreadSafe
@Immutable
public abstract class OneOfRequirementSupport<T> implements OneOfRequirement<T> {

    private final Set<T> _oneOf;

    public OneOfRequirementSupport(@Nonnull Collection<T> possibleValues) {
        if (possibleValues.isEmpty()) {
            throw new IllegalArgumentException("You have to provide at least one gender.");
        }
        _oneOf = new HashSet<>(possibleValues);
    }

    public OneOfRequirementSupport(@Nonnull T first, @Nullable T... others) {
        _oneOf = new HashSet<>();
        _oneOf.add(first);
        if (others != null) {
            _oneOf.addAll(asList(others));
        }
    }

    @Nonnull
    @Override
    public Set<T> getPossibleValues() {
        return unmodifiableSet(_oneOf);
    }

    protected abstract static class Container<T> {

        private Set<T> _possibleValues;

        @XmlElement(name = "possibleValues")
        public Set<T> getPossibleValues() {
            return _possibleValues;
        }

        public void setPossibleValues(Set<T> possibleValues) {
            _possibleValues = possibleValues;
        }
    }

    protected abstract static class Adapter<T, C extends Container<T>, R extends OneOfRequirementSupport<T>> extends XmlAdapter<C, R> {

        @Override
        public R unmarshal(C v) throws Exception {
            final R result;
            if (v != null) {
                result = newOneOfRequirement(v.getPossibleValues());
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
                result.setPossibleValues(v.getPossibleValues());
            } else {
                result = null;
            }
            return result;
        }

        @Nonnull
        protected abstract C newContainer();

        @Nonnull
        protected abstract R newOneOfRequirement(@Nullable Set<T> possibleValues);
    }



}

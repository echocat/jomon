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
import org.echocat.jomon.runtime.generation.RangeRequirementSupport;
import org.echocat.jomon.runtime.numbers.IntegerRangeRequirement.Adapter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@ThreadSafe
@Immutable
@XmlJavaTypeAdapter(Adapter.class)
public class IntegerRangeRequirement extends RangeRequirementSupport<Integer, IntegerRange> implements IntegerRequirement {

    public IntegerRangeRequirement(@Nullable @Including Integer from, @Nullable @Excluding Integer to) {
        this(new IntegerRange(from, to));
    }

    public IntegerRangeRequirement(@Nonnull IntegerRange range) {
        super(range);
    }

    @XmlRootElement(name = "integerRangeRequirement")
    @XmlType(name = "integerRangeRequirement")
    public static class Container extends RangeRequirementSupport.Container<Integer> {}


    public static class Adapter extends RangeRequirementSupport.Adapter<Integer, Container, IntegerRange> {

        @Nonnull
        @Override
        protected Container newContainer() {
            return new Container();
        }

        @Nonnull
        @Override
        protected IntegerRange newRangeRequirement(@Nullable @Including Integer from, @Nullable @Excluding Integer to) {
            return new IntegerRange(from, to);
        }
    }

}


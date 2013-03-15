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

package org.echocat.jomon.runtime.numbers;

import org.echocat.jomon.runtime.annotations.Excluding;
import org.echocat.jomon.runtime.annotations.Including;
import org.echocat.jomon.runtime.generation.RangeRequirementSupport;
import org.echocat.jomon.runtime.numbers.DoubleRangeRequirement.Adapter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;import java.lang.Double;import java.lang.Override;

@ThreadSafe
@Immutable
@XmlJavaTypeAdapter(Adapter.class)
public class DoubleRangeRequirement extends RangeRequirementSupport<Double, DoubleRange> implements DoubleRequirement {
    
    public DoubleRangeRequirement(@Nullable @Including Double from, @Nullable @Excluding Double to) {
        this(new DoubleRange(from, to));
    }

    public DoubleRangeRequirement(@Nonnull DoubleRange range) {
        super(range);
    }

    @XmlRootElement(name = "doubleRangeRequirement")
    @XmlType(name = "doubleRangeRequirement")
    public static class Container extends RangeRequirementSupport.Container<Double> {}


    public static class Adapter extends RangeRequirementSupport.Adapter<Double, Container, DoubleRange> {

        @Nonnull
        @Override
        protected Container newContainer() {
            return new Container();
        }

        @Nonnull
        @Override
        protected DoubleRange newRangeRequirement(@Nullable @Including Double from, @Nullable @Excluding Double to) {
            return new DoubleRange(from, to);
        }
    }

    
}

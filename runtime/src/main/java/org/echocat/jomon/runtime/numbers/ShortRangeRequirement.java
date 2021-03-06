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
import org.echocat.jomon.runtime.numbers.ShortRangeRequirement.Adapter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@ThreadSafe
@Immutable
@XmlJavaTypeAdapter(Adapter.class)
public class ShortRangeRequirement extends RangeRequirementSupport<Short, ShortRange> implements ShortRequirement {

    public ShortRangeRequirement(@Nullable @Including Short from, @Nullable @Excluding Short to) {
        this(new ShortRange(from, to));
    }

    public ShortRangeRequirement(@Nonnull ShortRange range) {
        super(range);
    }

    @XmlRootElement(name = "shortRangeRequirement")
    @XmlType(name = "shortRangeRequirementType")
    public static class Container extends RangeRequirementSupport.Container<Short> {

        @Override
        @XmlAttribute(name = "from")
        public Short getFrom() {
            return super.getFrom();
        }

        @Override
        public void setFrom(Short from) {
            super.setFrom(from);
        }

        @Override
        public Short getTo() {
            return super.getTo();
        }

        @Override
        @XmlAttribute(name = "to")
        public void setTo(Short to) {
            super.setTo(to);
        }

    }


    public static class Adapter extends RangeRequirementSupport.Adapter<Short, Container, ShortRange> {

        @Nonnull
        @Override
        protected Container newContainer() {
            return new Container();
        }

        @Nonnull
        @Override
        protected ShortRange newRangeRequirement(@Nullable @Including Short from, @Nullable @Excluding Short to) {
            return new ShortRange(from, to);
        }
    }

}


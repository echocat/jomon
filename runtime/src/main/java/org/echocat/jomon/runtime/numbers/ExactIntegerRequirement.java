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

import org.echocat.jomon.runtime.generation.ExactValueRequirementSupport;
import org.echocat.jomon.runtime.numbers.ExactIntegerRequirement.Adapter;

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
public class ExactIntegerRequirement extends ExactValueRequirementSupport<Integer> implements IntegerRequirement {

    public ExactIntegerRequirement(@Nullable Integer value) {
        super(value);
    }

    @XmlRootElement(name = "exactIntegerRequirement")
    @XmlType(name = "exactIntegerRequirementType")
    public static class Container extends ExactValueRequirementSupport.Container<Integer> {

        @Override
        @XmlAttribute(name = "value")
        public Integer getValue() {
            return super.getValue();
        }

        @Override
        public void setValue(Integer value) {
            super.setValue(value);
        }

    }


    public static class Adapter extends ExactValueRequirementSupport.Adapter<Integer, Container, ExactIntegerRequirement> {

        @Nonnull
        @Override
        protected Container newContainer() {
            return new Container();
        }

        @Nonnull
        @Override
        protected ExactIntegerRequirement newExactValueRequirement(@Nullable Integer value) {
            return new ExactIntegerRequirement(value);
        }
    }

}

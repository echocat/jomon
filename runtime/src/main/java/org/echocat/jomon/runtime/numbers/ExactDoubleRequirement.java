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
import org.echocat.jomon.runtime.numbers.ExactDoubleRequirement.Adapter;

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
public class ExactDoubleRequirement extends ExactValueRequirementSupport<Double> implements DoubleRequirement {

    public ExactDoubleRequirement(@Nullable Double value) {
        super(value);
    }

    @XmlRootElement(name = "exactDoubleRequirement")
    @XmlType(name = "exactDoubleRequirement")
    public static class Container extends ExactValueRequirementSupport.Container<Double> {}


    public static class Adapter extends ExactValueRequirementSupport.Adapter<Double, Container, ExactDoubleRequirement> {

        @Nonnull
        @Override
        protected Container newContainer() {
            return new Container();
        }

        @Nonnull
        @Override
        protected ExactDoubleRequirement newExactValueRequirement(@Nullable Double value) {
            return new ExactDoubleRequirement(value);
        }
    }

}

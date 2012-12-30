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

package org.echocat.jomon.runtime.logic;

import org.echocat.jomon.runtime.logic.ExactBooleanRequirement.Adapter;
import org.echocat.jomon.runtime.generation.ExactValueRequirementSupport;

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
public class ExactBooleanRequirement extends ExactValueRequirementSupport<Boolean> implements BooleanRequirement {

    public ExactBooleanRequirement(@Nullable Boolean value) {
        super(value);
    }

    @XmlRootElement(name = "exactBooleanRequirement")
    @XmlType(name = "exactBooleanRequirement")
    public static class Container extends ExactValueRequirementSupport.Container<Boolean> {}


    public static class Adapter extends ExactValueRequirementSupport.Adapter<Boolean, Container, ExactBooleanRequirement> {

        @Nonnull
        @Override
        protected Container newContainer() {
            return new Container();
        }

        @Nonnull
        @Override
        protected ExactBooleanRequirement newExactValueRequirement(@Nullable Boolean value) {
            return new ExactBooleanRequirement(value);
        }
    }

}

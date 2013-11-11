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

package org.echocat.jomon.runtime.util;

import org.echocat.jomon.runtime.generation.ExactValueRequirementSupport;
import org.echocat.jomon.runtime.util.ExactDurationRequirement.Adapter;

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
public class ExactDurationRequirement extends ExactValueRequirementSupport<Duration> implements DurationRequirement {

    public ExactDurationRequirement(@Nullable String value) {
        this(value != null ? new Duration(value) : null);
    }

    public ExactDurationRequirement(@Nullable Long value) {
        this(value != null ? new Duration(value) : null);
    }

    public ExactDurationRequirement(@Nullable Duration value) {
        super(value);
    }

    @XmlRootElement(name = "exactDurationRequirement")
    @XmlType(name = "exactDurationRequirementType")
    public static class Container extends ExactValueRequirementSupport.Container<Duration> {

        @Override
        @XmlAttribute(name = "value")
        public Duration getValue() {
            return super.getValue();
        }

        @Override
        public void setValue(Duration value) {
            super.setValue(value);
        }

    }


    public static class Adapter extends ExactValueRequirementSupport.Adapter<Duration, Container, ExactDurationRequirement> {

        @Nonnull
        @Override
        protected Container newContainer() {
            return new Container();
        }

        @Nonnull
        @Override
        protected ExactDurationRequirement newExactValueRequirement(@Nullable Duration value) {
            return new ExactDurationRequirement(value);
        }
    }

}

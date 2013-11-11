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

package org.echocat.jomon.runtime.date;

import org.echocat.jomon.runtime.date.ExactDateRequirement.Adapter;
import org.echocat.jomon.runtime.generation.ExactValueRequirementSupport;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;

@ThreadSafe
@Immutable
@XmlJavaTypeAdapter(Adapter.class)
public class ExactDateRequirement extends ExactValueRequirementSupport<Date> implements DateRequirement {

    public ExactDateRequirement(@Nullable Date value) {
        super(value);
    }

    @XmlRootElement(name = "exactDateRequirement")
    @XmlType(name = "exactDateRequirementType")
    public static class Container extends ExactValueRequirementSupport.Container<Date> {

        @Override
        @XmlAttribute(name = "value")
        public Date getValue() {
            return super.getValue();
        }

        @Override
        public void setValue(Date value) {
            super.setValue(value);
        }

    }


    public static class Adapter extends ExactValueRequirementSupport.Adapter<Date, Container, ExactDateRequirement> {

        @Nonnull
        @Override
        protected Container newContainer() {
            return new Container();
        }

        @Nonnull
        @Override
        protected ExactDateRequirement newExactValueRequirement(@Nullable Date value) {
            return new ExactDateRequirement(value);
        }
    }

}

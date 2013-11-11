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
import org.echocat.jomon.runtime.numbers.ExactShortRequirement.Adapter;

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
public class ExactShortRequirement extends ExactValueRequirementSupport<Short> implements ShortRequirement {

    public ExactShortRequirement(@Nullable Short value) {
        super(value);
    }

    @XmlRootElement(name = "exactShortRequirement")
    @XmlType(name = "exactShortRequirementType")
    public static class Container extends ExactValueRequirementSupport.Container<Short> {

        @Override
        @XmlAttribute(name = "value")
        public Short getValue() {
            return super.getValue();
        }

        @Override
        public void setValue(Short value) {
            super.setValue(value);
        }

    }


    public static class Adapter extends ExactValueRequirementSupport.Adapter<Short, Container, ExactShortRequirement> {

        @Nonnull
        @Override
        protected Container newContainer() {
            return new Container();
        }

        @Nonnull
        @Override
        protected ExactShortRequirement newExactValueRequirement(@Nullable Short value) {
            return new ExactShortRequirement(value);
        }
    }

}

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

import org.echocat.jomon.runtime.annotations.Excluding;
import org.echocat.jomon.runtime.annotations.Including;
import org.echocat.jomon.runtime.date.DateRangeRequirement.Adapter;
import org.echocat.jomon.runtime.generation.RangeRequirementSupport;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;

@ThreadSafe
@Immutable
@XmlJavaTypeAdapter(Adapter.class)
public class DateRangeRequirement extends RangeRequirementSupport<Date, DateRange> implements DateRequirement {

    public DateRangeRequirement(@Nullable @Including Date from, @Nullable @Excluding Date to) {
        this(new DateRange(from, to));
    }

    public DateRangeRequirement(@Nonnull DateRange range) {
        super(range);
    }

    @XmlRootElement(name = "dateRangeRequirement")
    @XmlType(name = "dateRangeRequirement")
    public static class Container extends RangeRequirementSupport.Container<Date> {}


    public static class Adapter extends RangeRequirementSupport.Adapter<Date, Container, DateRange> {

        @Nonnull
        @Override
        protected Container newContainer() {
            return new Container();
        }

        @Nonnull
        @Override
        protected DateRange newRangeRequirement(@Nullable @Including Date from, @Nullable @Excluding Date to) {
            return new DateRange(from, to);
        }
    }

}

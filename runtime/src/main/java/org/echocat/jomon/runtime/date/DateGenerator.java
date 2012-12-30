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

package org.echocat.jomon.runtime.date;

import org.echocat.jomon.runtime.generation.Generator;
import org.apache.commons.math3.random.RandomData;
import org.apache.commons.math3.random.RandomDataImpl;

import javax.annotation.Nonnull;
import java.util.Date;

import static java.lang.Long.MAX_VALUE;

public class DateGenerator implements Generator<Date, DateRequirement> {

    private static final RandomData RANDOM = new RandomDataImpl();
    private static final DateGenerator INSTANCE = new DateGenerator();

    @Nonnull
    public static Date generateDate(@Nonnull DateRequirement requirement) {
        return INSTANCE.generate(requirement);
    }

    @Override
    @Nonnull
    public Date generate(@Nonnull DateRequirement requirement) {
        final Date date;
        if (requirement instanceof ExactDateRequirement) {
            date = generateExact((ExactDateRequirement) requirement);
        } else if (requirement instanceof DateRangeRequirement) {
            date = generateInRange((DateRangeRequirement) requirement);
        } else {
            throw new IllegalArgumentException("Don't know how to handle requirement: " + requirement);
        }
        return date;
    }

    @Nonnull
    protected Date generateExact(@Nonnull ExactDateRequirement requirement) {
        return requirement.getValue();
    }

    @Nonnull
    protected Date generateInRange(@Nonnull DateRangeRequirement requirement) {
        final DateRange range = requirement.getValue();
        final Date from = range.getFrom();
        final long fromAsTimestamp = from != null ? from.getTime() : 0;
        final Date to = range.getTo();
        final long toAsTimestamp = to != null ? to.getTime() : MAX_VALUE;
        final long randomTimestamp = RANDOM.nextSecureLong(fromAsTimestamp, toAsTimestamp);
        return new Date(randomTimestamp);
    }
}

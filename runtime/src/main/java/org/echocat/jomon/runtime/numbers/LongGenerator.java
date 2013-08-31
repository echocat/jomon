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

import org.echocat.jomon.runtime.generation.Generator;
import org.apache.commons.math3.random.RandomData;
import org.apache.commons.math3.random.RandomDataImpl;

import javax.annotation.Nonnull;

import static java.lang.Long.MAX_VALUE;
import static java.lang.Long.MIN_VALUE;

public class LongGenerator implements Generator<Long, NumberRequirement<Long>> {

    private static final RandomData RANDOM = new RandomDataImpl();
    private static final LongGenerator INSTANCE = new LongGenerator();

    @Nonnull
    public static Long generateLong(@Nonnull NumberRequirement<Long> requirement) {
        return INSTANCE.generate(requirement);
    }

    @Override
    @Nonnull
    public Long generate(@Nonnull NumberRequirement<Long> requirement) {
        final Long Long;
        if (requirement instanceof ExactLongRequirement) {
            Long = generateExact((ExactLongRequirement) requirement);
        } else if (requirement instanceof LongRangeRequirement) {
            Long = generateInRange((LongRangeRequirement) requirement);
        } else {
            throw new IllegalArgumentException("Don't know how to handle requirement: " + requirement);
        }
        return Long;
    }

    @Nonnull
    protected Long generateExact(@Nonnull ExactLongRequirement requirement) {
        final Long value = requirement.getValue();
        return value != null ? value : 0;
    }

    @Nonnull
    protected Long generateInRange(@Nonnull LongRangeRequirement requirement) {
        final LongRange range = requirement.getValue();
        final Long from = range.getFrom();
        final Long to = range.getTo();
        return RANDOM.nextSecureLong(from != null ? from : MIN_VALUE, to != null ? to : MAX_VALUE);
    }
}

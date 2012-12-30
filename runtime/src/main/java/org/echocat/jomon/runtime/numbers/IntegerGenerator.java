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

package org.echocat.jomon.runtime.numbers;

import org.echocat.jomon.runtime.generation.Generator;
import org.apache.commons.math3.random.RandomData;
import org.apache.commons.math3.random.RandomDataImpl;

import javax.annotation.Nonnull;

public class IntegerGenerator implements Generator<Integer, NumberRequirement<Integer>> {

    private static final RandomData RANDOM = new RandomDataImpl();
    private static final IntegerGenerator INSTANCE = new IntegerGenerator();

    @Nonnull
    public static Integer generateInteger(@Nonnull NumberRequirement<Integer> requirement) {
        return INSTANCE.generate(requirement);
    }

    @Override
    @Nonnull
    public Integer generate(@Nonnull NumberRequirement<Integer> requirement) {
        final Integer integer;
        if (requirement instanceof ExactIntegerRequirement) {
            integer = generateExact((ExactIntegerRequirement) requirement);
        } else if (requirement instanceof IntegerRangeRequirement) {
            integer = generateInRange((IntegerRangeRequirement) requirement);
        } else {
            throw new IllegalArgumentException("Don't know how to handle requirement: " + requirement);
        }
        return integer;
    }

    @Nonnull
    protected Integer generateExact(@Nonnull ExactIntegerRequirement requirement) {
        return requirement.getValue();
    }

    @Nonnull
    protected Integer generateInRange(@Nonnull IntegerRangeRequirement requirement) {
        final IntegerRange range = requirement.getValue();
        return RANDOM.nextSecureInt(range.getFrom(), range.getTo());
    }
}

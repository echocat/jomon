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

import static java.lang.Double.MAX_VALUE;
import static java.lang.Double.MIN_VALUE;

public class DoubleGenerator implements Generator<Double, NumberRequirement<Double>> {

    private static final RandomData RANDOM = new RandomDataImpl();
    private static final DoubleGenerator INSTANCE = new DoubleGenerator();

    @Nonnull
    public static Double generateDouble(@Nonnull NumberRequirement<Double> requirement) {
        return INSTANCE.generate(requirement);
    }

    @Override
    @Nonnull
    public Double generate(@Nonnull NumberRequirement<Double> requirement) {
        final Double value;
        if (requirement instanceof ExactDoubleRequirement) {
            value = generateExact((ExactDoubleRequirement) requirement);
        } else if (requirement instanceof DoubleRangeRequirement) {
            value = generateInRange((DoubleRangeRequirement) requirement);
        } else {
            throw new IllegalArgumentException("Don't know how to handle requirement: " + requirement);
        }
        return value;
    }

    @Nonnull
    protected Double generateExact(@Nonnull ExactDoubleRequirement requirement) {
        final Double value = requirement.getValue();
        return value != null ? value : 0;
    }

    @Nonnull
    protected Double generateInRange(@Nonnull DoubleRangeRequirement requirement) {
        final DoubleRange range = requirement.getValue();
        final Double from = range.getFrom();
        final Double to = range.getTo();
        return RANDOM.nextUniform(from != null ? from : MIN_VALUE, to != null ? to : MAX_VALUE);
    }
}

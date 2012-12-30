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

public class ShortGenerator implements Generator<Short, NumberRequirement<Short>> {

    private static final RandomData RANDOM = new RandomDataImpl();
    private static final ShortGenerator INSTANCE = new ShortGenerator();

    @Nonnull
    public static Short generateShort(@Nonnull NumberRequirement<Short> requirement) {
        return INSTANCE.generate(requirement);
    }

    @Override
    @Nonnull
    public Short generate(@Nonnull NumberRequirement<Short> requirement) {
        final Short Short;
        if (requirement instanceof ExactShortRequirement) {
            Short = generateExact((ExactShortRequirement) requirement);
        } else if (requirement instanceof ShortRangeRequirement) {
            Short = generateInRange((ShortRangeRequirement) requirement);
        } else {
            throw new IllegalArgumentException("Don't know how to handle requirement: " + requirement);
        }
        return Short;
    }

    @Nonnull
    protected Short generateExact(@Nonnull ExactShortRequirement requirement) {
        return requirement.getValue();
    }

    @Nonnull
    protected Short generateInRange(@Nonnull ShortRangeRequirement requirement) {
        final ShortRange range = requirement.getValue();
        return (short) RANDOM.nextSecureInt(range.getFrom(), range.getTo());
    }
}

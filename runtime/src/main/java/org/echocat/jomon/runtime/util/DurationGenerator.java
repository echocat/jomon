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

import org.echocat.jomon.runtime.generation.Generator;
import org.apache.commons.math3.random.RandomData;
import org.apache.commons.math3.random.RandomDataImpl;

import javax.annotation.Nonnull;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class DurationGenerator implements Generator<Duration, DurationRequirement> {

    private static final RandomData RANDOM = new RandomDataImpl();
    private static final DurationGenerator INSTANCE = new DurationGenerator();

    @Nonnull
    public static Duration generateDuration(@Nonnull DurationRequirement requirement) {
        return INSTANCE.generate(requirement);
    }

    @Override
    @Nonnull
    public Duration generate(@Nonnull DurationRequirement requirement) {
        final Duration duration;
        if (requirement instanceof ExactDurationRequirement) {
            duration = generateExact((ExactDurationRequirement) requirement);
        } else if (requirement instanceof DurationRangeRequirement) {
            duration = generateInRange((DurationRangeRequirement) requirement);
        } else if (requirement instanceof IncreasingDurationRequirement) {
            duration = generateIncreasingDuration((IncreasingDurationRequirement) requirement);
        } else {
            throw new IllegalArgumentException("Don't know how to handle requirement: " + requirement);
        }
        return duration;
    }

    @Nonnull
    protected Duration generateExact(@Nonnull ExactDurationRequirement requirement) {
        return requirement.getValue();
    }

    @Nonnull
    protected Duration generateInRange(@Nonnull DurationRangeRequirement requirement) {
        final DurationRange range = requirement.getValue();
        final Duration from = range.getFrom();
        final Duration to = range.getTo();
        final long milliseconds = RANDOM.nextSecureLong(from != null ? from.in(MILLISECONDS) : 0, to != null ? to.in(MILLISECONDS) : Long.MAX_VALUE);
        return new Duration(milliseconds);
    }

    @Nonnull
    protected Duration generateIncreasingDuration(@Nonnull IncreasingDurationRequirement requirement) {
        return requirement.next();
    }
}

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

package org.echocat.jomon.runtime.logic;

import org.echocat.jomon.runtime.generation.Generator;

import javax.annotation.Nonnull;

public class BooleanGenerator implements Generator<Boolean, BooleanRequirement> {

    private static final BooleanGenerator INSTANCE = new BooleanGenerator();

    @Nonnull
    public static Boolean generateBoolean(@Nonnull BooleanRequirement requirement) {
        return INSTANCE.generate(requirement);
    }

    @Override
    @Nonnull
    public Boolean generate(@Nonnull BooleanRequirement requirement) {
        final Boolean Boolean;
        if (requirement instanceof ExactBooleanRequirement) {
            Boolean = generateExact((ExactBooleanRequirement) requirement);
        } else {
            throw new IllegalArgumentException("Don't know how to handle requirement: " + requirement);
        }
        return Boolean;
    }

    @Nonnull
    protected Boolean generateExact(@Nonnull ExactBooleanRequirement requirement) {
        return requirement.getValue();
    }
}

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

package org.echocat.jomon.runtime.generation;

import org.echocat.jomon.runtime.util.SerialGenerator;
import org.echocat.jomon.runtime.util.SimpleLongSerialGenerator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.echocat.jomon.runtime.generation.StringRequirement.UNIQUE_VALUE_PLACE_HOLDER;

public class StringGenerator implements Generator<String, StringRequirement> {

    private final SerialGenerator<?> _serialGenerator;

    public StringGenerator() {
        this(null);
    }

    public StringGenerator(@Nullable SerialGenerator<?> serialGenerator) {
        _serialGenerator = serialGenerator != null ? serialGenerator : new SimpleLongSerialGenerator();
    }

    @Override
    @Nonnull
    public String generate(@Nonnull StringRequirement requirement) {
        final String pattern = requirement.getPattern();
        final Object uniqueValue = _serialGenerator.next();
        return pattern.replace(UNIQUE_VALUE_PLACE_HOLDER, uniqueValue.toString());
    }
}

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

package org.echocat.jomon.process;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class GeneratedProcessGenerator extends ProcessGenerator {

    private static final GeneratedProcessGenerator INSTANCE = new GeneratedProcessGenerator();

    @Nonnull
    public static GeneratedProcess generateAn(@Nonnull GeneratedProcessRequirement requirement) {
        return INSTANCE.generate(requirement);
    }

    public GeneratedProcessGenerator() {
    }

    public GeneratedProcessGenerator(@Nullable ProcessRepository repository) {
        super(repository);
    }
}

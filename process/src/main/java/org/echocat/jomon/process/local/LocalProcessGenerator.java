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

package org.echocat.jomon.process.local;

import org.echocat.jomon.runtime.generation.Generator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class LocalProcessGenerator implements Generator<LocalGeneratedProcess, LocalGeneratedProcessRequirement> {

    private static final LocalProcessGenerator INSTANCE = new LocalProcessGenerator();

    @Nonnull
    public static LocalGeneratedProcess generateAn(@Nonnull LocalGeneratedProcessRequirement requirement) {
        return INSTANCE.generate(requirement);
    }

    private final LocalProcessRepository _repository;

    public LocalProcessGenerator() {
        this(null);
    }

    public LocalProcessGenerator(@Nullable LocalProcessRepository repository) {
        _repository = repository != null ? repository : LocalProcessRepository.getInstance();
    }

    @Nonnull
    @Override
    public LocalGeneratedProcess generate(@Nonnull LocalGeneratedProcessRequirement requirement) {
        return _repository.generate(requirement);
    }

}

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

package org.echocat.jomon.process;

import org.echocat.jomon.runtime.generation.Generator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class ProcessGenerator implements Generator<GeneratedProcess, GeneratedProcessRequirement> {

    private static final ProcessGenerator INSTANCE = new ProcessGenerator();

    @Nonnull
    public static GeneratedProcess generateAn(@Nonnull GeneratedProcessRequirement requirement) {
        return INSTANCE.generate(requirement);
    }

    private final ProcessRepository _repository;

    public ProcessGenerator() {
        this(null);
    }

    public ProcessGenerator(@Nullable ProcessRepository repository) {
        _repository = repository != null ? repository : ProcessRepository.getInstance();
    }

    @Nonnull
    @Override
    public GeneratedProcess generate(@Nonnull GeneratedProcessRequirement requirement) {
        return _repository.generate(requirement);
    }

}

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

package org.echocat.jomon.process.local.execution;

import org.echocat.jomon.process.execution.BaseProcessExecuter;
import org.echocat.jomon.process.local.LocalGeneratedProcess;
import org.echocat.jomon.process.local.LocalGeneratedProcessRequirement;
import org.echocat.jomon.process.local.LocalProcessRepository;
import org.echocat.jomon.runtime.generation.Generator;

import javax.annotation.Nonnull;
import java.io.File;

import static org.echocat.jomon.process.local.LocalProcessRepository.processRepository;

public class LocalProcessExecuter extends BaseProcessExecuter<File, Long, LocalGeneratedProcessRequirement, LocalGeneratedProcess, Generator<LocalGeneratedProcess, LocalGeneratedProcessRequirement>, LocalProcessExecuter>{

    private static final LocalProcessExecuter INSTANCE = new LocalProcessExecuter();

    @Nonnull
    public static LocalProcessExecuter getIstance() {
        return INSTANCE;
    }

    @Nonnull
    public static LocalProcessExecuter processExecuter() {
        return getIstance();
    }

    @Nonnull
    public static LocalProcessExecuter executer() {
        return getIstance();
    }


    public LocalProcessExecuter() {
        this(processRepository());
    }

    public LocalProcessExecuter(@Nonnull LocalProcessRepository processRepository) {
        super(processRepository);
    }

}

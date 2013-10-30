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

package org.echocat.jomon.process.local.daemon;

import org.echocat.jomon.process.daemon.BaseProcessDaemonQuery;
import org.echocat.jomon.process.daemon.ProcessDaemonRepository;
import org.echocat.jomon.process.daemon.ProcessDaemonRequirement;
import org.echocat.jomon.process.local.LocalGeneratedProcess;
import org.echocat.jomon.process.local.LocalGeneratedProcessRequirement;
import org.echocat.jomon.runtime.generation.Generator;

import javax.annotation.Nonnull;
import java.io.File;

import static org.echocat.jomon.process.local.LocalProcessRepository.processRepository;

public class LocalProcessDaemonRepository extends ProcessDaemonRepository<File, Long, LocalGeneratedProcess, LocalProcessDaemon<?>, ProcessDaemonRequirement<File, Long, LocalGeneratedProcess, LocalProcessDaemon<?>>, BaseProcessDaemonQuery<File, Long, ?, LocalProcessDaemon<?>>, Generator<LocalGeneratedProcess, LocalGeneratedProcessRequirement>> {

    @Nonnull
    private static final LocalProcessDaemonRepository INSTANCE = new LocalProcessDaemonRepository();

    @Nonnull
    public static LocalProcessDaemonRepository getInstance() {
        return INSTANCE;
    }

    @Nonnull
    public static LocalProcessDaemonRepository localProcessDaemonRepository() {
        return getInstance();
    }

    @Nonnull
    public static LocalProcessDaemonRepository processDaemonRepository() {
        return getInstance();
    }

    public LocalProcessDaemonRepository() {
        super(processRepository());
    }

    public LocalProcessDaemonRepository(@Nonnull Generator<LocalGeneratedProcess, LocalGeneratedProcessRequirement> processGenerator) {
        super(processGenerator);
    }

}

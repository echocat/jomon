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

package org.echocat.jomon.process.daemon;

import org.echocat.jomon.process.GeneratedProcess;

import javax.annotation.Nonnull;

import static java.lang.Thread.currentThread;

public class ProcessMonitor<E, ID, P extends GeneratedProcess<E, ID>, R extends ProcessDaemonRequirement<E, ID, P, ?>> implements Runnable {

    @Nonnull
    private final R _requirement;
    @Nonnull
    private final P _process;

    public ProcessMonitor(@Nonnull R requirement, @Nonnull P process) {
        _requirement = requirement;
        _process = process;
    }

    @Override
    public void run() {
        boolean gotInterrupted = false;
        boolean regularEnd = false;
        do {
            try {
                final boolean success = _requirement.getExitCodeValidator().apply(_process.waitFor());
                _requirement.getStartupListener().notifyProcessTerminated(_process, success);
                _requirement.getStreamListener().notifyProcessTerminated(_process, success);
                regularEnd = true;
            } catch (InterruptedException ignored) {
                // We are required to ignore an interrupt here for now because we are required to wait for the end of the process.
                // Otherwise we have the risk, that not all events are covered. We will reset the interrupted flag after this.
                gotInterrupted = true;
            }
        } while(!regularEnd);
        if (gotInterrupted) {
            currentThread().interrupt();
        }
    }

}

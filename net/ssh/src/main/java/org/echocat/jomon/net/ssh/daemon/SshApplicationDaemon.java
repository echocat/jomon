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

package org.echocat.jomon.net.ssh.daemon;

import org.echocat.jomon.net.ssh.Ssh;
import org.echocat.jomon.net.ssh.SshGeneratedProcess;
import org.echocat.jomon.net.ssh.SshProcess.Id;
import org.echocat.jomon.process.CouldNotStartException;
import org.echocat.jomon.process.daemon.ProcessDaemon;

import javax.annotation.Nonnull;

public abstract class SshApplicationDaemon<R extends SshApplicationDaemonRequirement<?>> extends ProcessDaemon<String, Id, SshGeneratedProcess, R, Ssh> {

    protected SshApplicationDaemon(@Nonnull R requirement) throws CouldNotStartException {
        this(Ssh.ssh(), requirement);
    }

    protected SshApplicationDaemon(@Nonnull Ssh ssh, @Nonnull R requirement) throws CouldNotStartException {
        super(ssh, requirement);
    }

}

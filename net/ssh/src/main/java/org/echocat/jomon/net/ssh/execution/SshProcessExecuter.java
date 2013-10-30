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

package org.echocat.jomon.net.ssh.execution;

import org.echocat.jomon.net.ssh.Ssh;
import org.echocat.jomon.net.ssh.SshGeneratedProcess;
import org.echocat.jomon.net.ssh.SshGeneratedProcessRequirement;
import org.echocat.jomon.net.ssh.SshProcess.Id;
import org.echocat.jomon.process.execution.BaseProcessExecuter;
import org.echocat.jomon.runtime.generation.Generator;

import javax.annotation.Nonnull;

import static org.echocat.jomon.net.ssh.Ssh.ssh;

public class SshProcessExecuter extends BaseProcessExecuter<String, Id, SshGeneratedProcessRequirement, SshGeneratedProcess, Generator<SshGeneratedProcess, SshGeneratedProcessRequirement>, SshProcessExecuter> {

    private static final SshProcessExecuter INSTANCE = new SshProcessExecuter();

    @Nonnull
    public static SshProcessExecuter getIstance() {
        return INSTANCE;
    }

    @Nonnull
    public static SshProcessExecuter sshProcessExecuter() {
        return getIstance();
    }

    @Nonnull
    public static SshProcessExecuter executer() {
        return getIstance();
    }

    public SshProcessExecuter() {
        this(ssh());
    }

    public SshProcessExecuter(@Nonnull Ssh ssh) {
        this(ssh.getProcessRepository());
    }

    public SshProcessExecuter(@Nonnull Generator<SshGeneratedProcess, SshGeneratedProcessRequirement> processGenerator) {
        super(processGenerator);
    }

}
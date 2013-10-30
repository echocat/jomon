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

package org.echocat.jomon.net.ssh;

import org.echocat.jomon.process.BaseGeneratedProcessRequirement;
import org.echocat.jomon.process.Pty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.InetSocketAddress;

import static org.echocat.jomon.net.ssh.SshRemote.Impl.remote;

public class SshGeneratedProcessRequirement extends BaseGeneratedProcessRequirement<String, SshGeneratedProcessRequirement> {

    @Nonnull
    public static SshGeneratedProcessRequirement process(@Nonnull String remote, @Nonnull String executable) {
        return process(remote(remote), executable);
    }

    @Nonnull
    public static SshGeneratedProcessRequirement process(@Nonnull InetSocketAddress address, @Nullable String user, @Nullable String password, @Nonnull String executable) {
        return process(remote(address, user, password), executable);
    }

    @Nonnull
    public static SshGeneratedProcessRequirement process(@Nonnull InetSocketAddress address, @Nullable String user, @Nonnull String executable) {
        return process(address, user, null, executable);
    }

    @Nonnull
    public static SshGeneratedProcessRequirement process(@Nonnull InetSocketAddress address, @Nonnull String executable) {
        return process(address, null, executable);
    }

    @Nonnull
    public static SshGeneratedProcessRequirement process(@Nonnull SshContext context, @Nonnull String executable) {
        return new SshGeneratedProcessRequirement(context, executable);
    }

    @Nonnull
    private final SshContext _context;

    @Nullable
    private Pty _pty;
    private boolean _agentForwarding = true;

    public SshGeneratedProcessRequirement(@Nonnull SshContext context, @Nonnull String executable) {
        super(executable);
        _context = context;
    }

    @Nonnull
    public SshGeneratedProcessRequirement withPty(@Nullable Pty pty) {
        _pty = pty;
        return thisObject();
    }

    @Nonnull
    public SshGeneratedProcessRequirement withAgentForwarding(boolean agentForwarding) {
        _agentForwarding = agentForwarding;
        return thisObject();
    }

    @Nonnull
    public SshGeneratedProcessRequirement withAgentForwarding() {
        return withAgentForwarding(true);
    }

    @Nonnull
    public SshGeneratedProcessRequirement withoutAgentForwarding() {
        return withAgentForwarding(false);
    }

    @Nonnull
    public SshGeneratedProcessRequirement with(@Nullable Pty pty) {
        return withPty(pty);
    }

    @Nonnull
    public SshContext getContext() {
        return _context;
    }

    @Nullable
    public Pty getPty() {
        return _pty;
    }

    public boolean isAgentForwarding() {
        return _agentForwarding;
    }

    @Override
    public String toString() {
        return getContext() + ": " + super.toString();
    }

}

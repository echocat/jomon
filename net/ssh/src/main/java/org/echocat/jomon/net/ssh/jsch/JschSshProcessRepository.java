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

package org.echocat.jomon.net.ssh.jsch;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.echocat.jomon.net.ssh.*;
import org.echocat.jomon.process.CouldNotStartException;
import org.echocat.jomon.process.Pty;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Map.Entry;

import static org.echocat.jomon.net.ssh.SshProcess.Id.Impl.id;
import static org.echocat.jomon.net.ssh.SshSessionRequirement.session;
import static org.echocat.jomon.net.ssh.jsch.JschUtils.open;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

public class JschSshProcessRepository implements SshProcessRepository {

    @Nonnull
    private final JschSshSessionGenerator _sessionGenerator;

    public JschSshProcessRepository(@Nonnull JschSshSessionGenerator sessionGenerator) {
        _sessionGenerator = sessionGenerator;
    }

    @Override
    @Nonnull
    public SshGeneratedProcess generate(@Nonnull SshGeneratedProcessRequirement requirement) {
        boolean success = false;
        final SshSession sshSession = getSessionOf(requirement);
        // noinspection ObjectEquality
        final boolean closeSession = requirement.getContext() != sshSession;
        try {
            final Session session = getJschSessionOf(sshSession);
            final ChannelExec exec = open(session, ChannelExec.class);
            try {
                final JschSshGeneratedProcess result = createAndConfigureProcess(requirement, exec, sshSession, closeSession);
                success = true;
                return result;
            } catch (final Exception e) {
                throw new CouldNotStartException("Could not start or configure process.", e);
            } finally {
                if (!success) {
                    try {
                        exec.disconnect();
                    } catch (final Exception ignored) {}
                }
            }
        } finally {
            if (!success && closeSession) {
                closeQuietly(sshSession);
            }
        }
    }

    @Nonnull
    protected JschSshGeneratedProcess createAndConfigureProcess(@Nonnull SshGeneratedProcessRequirement requirement, @Nonnull ChannelExec exec, @Nonnull SshSession session, boolean closeContext) throws IOException {
        exec.setCommand(requirement.getCompleteCommandLineAsString());
        final Pty pty = requirement.getPty();
        exec.setPty(pty != null);
        if (pty != null) {
            exec.setPtyType(pty.getType(), pty.getCharacterWidth(), pty.getCharacterHeight(), pty.getPixelWidth(), pty.getPixelWidth());
        }
        for (final Entry<String, String> entry : requirement.getEnvironment().entrySet()) {
            exec.setEnv(entry.getKey(), entry.getValue());
        }
        try {
            exec.connect();
        } catch (final JSchException e) {
            throw new SshException(e.getMessage(), e);
        }
        return new JschSshGeneratedProcess(requirement, exec, id(null, session.getRemote()), closeContext);
    }


    @Nonnull
    protected SshSession getSessionOf(@Nonnull SshGeneratedProcessRequirement requirement) {
        final SshContext context = requirement.getContext();
        final SshSession result;
        if (context instanceof SshSession) {
            result = (SshSession) context;
        } else if (context instanceof SshRemote) {
            result = _sessionGenerator.generate(session((SshRemote) context));
        } else {
            throw new UnsupportedOperationException("This implementation of " + SshProcessRepository.class.getName() + " does not support a " + SshContext.class.getName() + " of type " + context.getClass().getName() + ".");
        }
        return result;
    }

    @Nonnull
    protected Session getJschSessionOf(@Nonnull SshSession sshSession) {
        if (!(sshSession instanceof JschSshSession)) {
            throw new IllegalArgumentException("Could not handle sessions of type " + sshSession.getClass().getName() + ".");
        }
        return ((JschSshSession) sshSession).getSession();
    }

    @Nonnull
    protected JschSshSessionGenerator getSessionGenerator() {
        return _sessionGenerator;
    }

}

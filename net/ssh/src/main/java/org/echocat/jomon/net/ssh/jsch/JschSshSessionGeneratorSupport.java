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

import com.jcraft.jsch.*;
import org.echocat.jomon.net.ssh.*;

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;

public abstract class JschSshSessionGeneratorSupport implements SshSessionGenerator {

    @Nonnull
    @Override
    public SshSession generate(@Nonnull SshSessionRequirement requirement) {
        requirement.getRemote();
        final JSch jsch = createJsch(requirement);
        configureJsch(jsch, requirement);
        final Session session = createSessionBy(jsch, requirement);
        configureSession(session, requirement);
        try {
            session.connect();
        } catch (JSchException e) {
            throw new SshConnectionException("Could not connect to " + requirement.getRemote() + ".", e);
        }
        return new JschSshSession(requirement.getRemote(), session);
    }

    @Nonnull
    protected Session createSessionBy(@Nonnull JSch jsch, @Nonnull SshSessionRequirement requirement) {
        final SshRemote remote = requirement.getRemote();
        final InetSocketAddress address = remote.getAddress();
        try {
            return jsch.getSession(remote.getUser(), address.getHostName(), address.getPort());
        } catch (JSchException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("UnusedParameters")
    @Nonnull
    protected JSch createJsch(@Nonnull SshSessionRequirement requirement) {
        return new JSch();
    }

    protected void configureJsch(@Nonnull JSch jsch, @Nonnull SshSessionRequirement requirement) {
        final IdentityRepository identityRepository = createIdentityRepository(requirement);
        jsch.setIdentityRepository(identityRepository);
    }

    @SuppressWarnings("UnusedParameters")
    protected void configureSession(@Nonnull Session session, @Nonnull SshSessionRequirement requirement) {
        session.setUserInfo(new NoopUserInfo());
    }

    @Nonnull
    protected abstract IdentityRepository createIdentityRepository(@Nonnull SshSessionRequirement requirement);

    protected static class NoopUserInfo implements UserInfo, UIKeyboardInteractive {

        @Override
        public String getPassword() { return null; }

        @Override
        public boolean promptYesNo(String str) { return true; }

        @Override
        public String getPassphrase() { return null; }

        @Override
        public boolean promptPassphrase(String message) { return true; }

        @Override
        public boolean promptPassword(String message) { return true; }

        @Override
        public void showMessage(String message) {}

        @Override
        public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt, boolean[] echo) {
            return new String[0];
        }
    }

}

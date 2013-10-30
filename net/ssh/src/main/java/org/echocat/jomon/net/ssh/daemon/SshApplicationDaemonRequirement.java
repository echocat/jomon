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

import org.echocat.jomon.net.ssh.SshGeneratedProcess;
import org.echocat.jomon.net.ssh.SshProcess.Id;
import org.echocat.jomon.process.daemon.ProcessDaemonRequirement;

import javax.annotation.Nonnull;

public interface SshApplicationDaemonRequirement<T extends SshApplicationDaemon<?>> extends ProcessDaemonRequirement<String, Id, SshGeneratedProcess, T> {

    public static class Base<B extends Base<B>> extends ProcessDaemonRequirement.Base<String, Id, SshGeneratedProcess, SshApplicationDaemon<?>, B> implements SshApplicationDaemonRequirement<SshApplicationDaemon<?>> {

        public Base(@SuppressWarnings("rawtypes") @Nonnull Class<? extends SshApplicationDaemon> type) {
            // noinspection unchecked
            super((Class<SshApplicationDaemon<?>>)(Class)type);
        }

    }

    public static class Impl extends SshApplicationDaemonRequirement.Base<Impl> {

        @Nonnull
        public static Impl sshApplicationDaemonOfType(@SuppressWarnings("rawtypes") @Nonnull Class<? extends SshApplicationDaemon> type) {
            // noinspection unchecked
            return new Impl(type);
        }

        public Impl(@SuppressWarnings("rawtypes") @Nonnull Class<? extends SshApplicationDaemon> type) {
            super(type);
        }

    }

}

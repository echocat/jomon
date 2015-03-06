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

import org.echocat.jomon.runtime.generation.Requirement;
import org.echocat.jomon.runtime.util.Hint;
import org.echocat.jomon.runtime.util.Hints;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.InetSocketAddress;

import static org.echocat.jomon.net.ssh.SshRemote.Impl.remote;
import static org.echocat.jomon.runtime.util.ValueProviderUtils.setAll;

public class SshSessionRequirement implements Requirement {

    @Nonnull
    public static SshSessionRequirement session(@Nonnull SshRemote remote) {
        return new SshSessionRequirement(remote);
    }

    @Nonnull
    public static SshSessionRequirement session(@Nonnull String remote) {
        return session(remote(remote));
    }

    @Nonnull
    public static SshSessionRequirement session(@Nonnull InetSocketAddress address, @Nullable String user, @Nullable String password) {
        return session(remote(address, user, password));
    }

    @Nonnull
    public static SshSessionRequirement session(@Nonnull InetSocketAddress address, @Nullable String user) {
        return session(address, user, null);
    }

    @Nonnull
    public static SshSessionRequirement session(@Nonnull InetSocketAddress address) {
        return session(address, null);
    }

    @Nonnull
    private final SshRemote _remote;
    @Nonnull
    private final Hints _hints = new Hints();

    public SshSessionRequirement(@Nonnull SshRemote remote) {
        _remote = remote;
    }

    @Nonnull
    public SshRemote getRemote() {
        return _remote;
    }

    @Nonnull
    public SshSessionRequirement withHints(@Nonnull Hints hints) {
        setAll(_hints, hints);
        return this;
    }

    @Nonnull
    public <T> SshSessionRequirement withHint(@Nonnull Hint<T> hint, @Nullable T value) {
        _hints.set(hint, value);
        return this;
    }

    @Nonnull
    public Hints getHints() {
        return _hints;
    }

}

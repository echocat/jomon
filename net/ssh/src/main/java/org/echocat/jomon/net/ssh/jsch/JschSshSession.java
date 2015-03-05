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

import com.jcraft.jsch.Session;
import org.echocat.jomon.net.ssh.SshRemote;
import org.echocat.jomon.net.ssh.SshSession;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.reflect.Field;

import static org.echocat.jomon.runtime.codec.HashFunctionUtils.asHexString;
import static org.echocat.jomon.runtime.reflection.ClassUtils.getFieldOf;

public class JschSshSession implements SshSession {

    private static final Field ID_FIELD = getFieldOf(Session.class, byte[].class, "session_id", false);

    @Nonnull
    private final SshRemote _remote;
    @Nonnull
    private final Session _session;

    public JschSshSession(@Nonnull SshRemote remote, @Nonnull Session session) {
        _remote = remote;
        _session = session;
    }

    @Nonnull
    @Override
    public SshRemote getRemote() {
        return _remote;
    }

    @Nonnull
    public Session getSession() {
        return _session;
    }

    @Override
    public String toString() {
        return getId() + "@" + _remote;
    }

    @Override
    public void close() throws IOException {
        getSession().disconnect();
    }

    @Nonnull
    @Override
    public String getId() {
        return asHexString(getIdAsBytes());
    }

    @Nonnull
    public byte[] getIdAsBytes() {
        try {
            return (byte[]) ID_FIELD.get(getSession());
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("Could not read id of session.", e);
        }

    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else if (!(o instanceof JschSshSession)) {
            result = false;
        } else {
            final JschSshSession that = (JschSshSession) o;
            result = getId().equals(that.getId());
        }
        return result;
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}

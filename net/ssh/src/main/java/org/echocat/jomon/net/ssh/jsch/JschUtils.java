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
import com.jcraft.jsch.NotifyingIO.EventConsumer;
import org.echocat.jomon.net.ssh.SshConnectionException;
import org.echocat.jomon.net.ssh.SshSystemException;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Class.forName;
import static org.echocat.jomon.runtime.reflection.ClassUtils.getFieldOf;

public class JschUtils {

    @Nonnull
    private static final Map<Class<? extends Channel>, String> TYPE_TO_NAME = createTypeToName();
    @Nonnull
    private static final Field IO_FIELD = getFieldOf(Channel.class, IO.class, "io", false);

    private static Map<Class<? extends Channel>, String> createTypeToName() {
        final Map<Class<? extends Channel>, String> typeToName = new HashMap<>();
        addIfPresent(typeToName, "com.jcraft.jsch.ChannelSession", "session");
        addIfPresent(typeToName, "com.jcraft.jsch.ChannelShell", "shell");
        addIfPresent(typeToName, "com.jcraft.jsch.ChannelExec", "exec");
        addIfPresent(typeToName, "com.jcraft.jsch.ChannelX11", "x11");
        addIfPresent(typeToName, "com.jcraft.jsch.ChannelAgentForwarding", "auth-agent@openssh.com");
        addIfPresent(typeToName, "com.jcraft.jsch.ChannelDirectTCPIP", "direct-tcpip");
        addIfPresent(typeToName, "com.jcraft.jsch.ChannelForwardedTCPIP", "forwarded-tcpip");
        addIfPresent(typeToName, "com.jcraft.jsch.ChannelSftp", "sftp");
        addIfPresent(typeToName, "com.jcraft.jsch.ChannelSubsystem", "subsystem");
        return Collections.unmodifiableMap(typeToName);
    }

    private static void addIfPresent(@Nonnull Map<Class<? extends Channel>, String> typeToName, @Nonnull String className, @Nonnull String typeName) {
        try {
            // noinspection unchecked
            typeToName.put((Class<? extends Channel>) forName(className), typeName);
        } catch (final ClassNotFoundException ignored) {}
    }

    @Nonnull
    public static <T extends Channel> T open(@Nonnull Session session, @Nonnull Class<T> type) {
        final String name = TYPE_TO_NAME.get(type);
        if (name == null) {
            throw new IllegalArgumentException("Type " + type.getName() + " is unknown.");
        }
        return open(session, type, name);
    }

    @Nonnull
    public static <T extends Channel> T open(@Nonnull Session session, @Nonnull Class<T> type, @Nonnull String typeName) {
        if (!session.isConnected()) {
            throw new SshConnectionException("Not connected.");
        }
        final Channel channel;
        try {
            channel = session.openChannel(typeName);
        } catch (final JSchException e) {
            throw new SshSystemException("Could not open channel '" + typeName + "'.", e);
        }
        if (!type.isInstance(channel)) {
            throw new SshSystemException("Created channel " + channel + "  is not of type " + type.getName() + ".");
        }
        return type.cast(channel);
    }

    public static void register(@Nonnull EventConsumer consumer, @Nonnull Channel on) {
        // noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (on) {
            try {
                final IO io = (IO) IO_FIELD.get(on);
                final NotifyingIO notifyingIO;
                if (io instanceof NotifyingIO) {
                    notifyingIO = (NotifyingIO) io;
                } else {
                    notifyingIO = new NotifyingIO(io);
                    IO_FIELD.set(on, notifyingIO);
                }
                notifyingIO.setEventConsumer(consumer);
            } catch (final Exception e) {
                throw new RuntimeException("Could not set the io field of '" + on + "'.", e);
            }
        }
    }

    private JschUtils() {}

}

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

import org.echocat.jomon.net.ssh.SshRemote.SerializerImpl;
import org.echocat.jomon.runtime.io.ChunkAwareSerializer;
import org.echocat.jomon.runtime.io.SerializableBy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

import static java.lang.Integer.parseInt;
import static java.lang.System.getProperty;
import static java.net.InetSocketAddress.createUnresolved;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.echocat.jomon.runtime.io.ByteUtils.*;
import static org.echocat.jomon.runtime.io.StreamUtils.*;

@SerializableBy(SerializerImpl.class)
public interface SshRemote extends SshContext {

    public static final int DEFAULT_PORT = 22;

    @Nonnull
    public InetSocketAddress getAddress();

    @Nonnull
    public String getUser();

    @Nullable
    public String getPassword();


    public static class Impl implements SshRemote {

        @Nonnull
        public static Impl remote(@Nonnull InetSocketAddress address, @Nullable String user, @Nullable String password) {
            return new Impl(address, user, password);
        }

        @Nonnull
        public static Impl remote(@Nonnull InetSocketAddress address, @Nullable String user) {
            return remote(address, user, null);
        }

        @Nonnull
        public static Impl remote(@Nonnull InetSocketAddress address) {
            return remote(address, null);
        }

        @Nonnull
        public static Impl remote(@Nonnull String remoteAsString) {
            final int lastAt = remoteAsString.lastIndexOf('@');
            final boolean hasUserPart = lastAt >= 0 && lastAt + 1 < remoteAsString.length();
            final String user = hasUserPart ? remoteAsString.substring(0, lastAt) : null;
            final String hostAndPort = hasUserPart ? remoteAsString.substring(lastAt + 1) : remoteAsString;
            final int lastDoubleDot = hostAndPort.lastIndexOf(':');
            final boolean hasPortPart = lastDoubleDot >= 0 && lastDoubleDot + 1 < hostAndPort.length();
            String host = hasPortPart ? hostAndPort.substring(0, lastDoubleDot) : hostAndPort;
            int port;
            try {
                port = hasPortPart ? parseInt(hostAndPort.substring(lastDoubleDot + 1)) : DEFAULT_PORT;
            } catch (NumberFormatException ignored) {
                host = hostAndPort;
                port = DEFAULT_PORT;
            }
            if (port <= 0) {
                throw new IllegalArgumentException("Illegal remote provided - containing illegal port: " + remoteAsString);
            }
            try {
                return remote(new InetSocketAddress(InetAddress.getByName(host), port), user);
            } catch (UnknownHostException e) {
                throw new IllegalArgumentException("Illegal remote provided - illegal host: " + remoteAsString, e);
            }
        }

        @Nonnull
        private final InetSocketAddress _address;
        @Nullable
        private final String _user;
        @Nullable
        private final String _password;

        public Impl(@Nonnull InetSocketAddress address, @Nullable String user, @Nullable String password) {
            _address = address;
            _user = user;
            _password = password;
        }

        @Nonnull
        @Override
        public InetSocketAddress getAddress() {
            return _address;
        }

        @Nonnull
        @Override
        public String getUser() {
            String user = getGivenUser();
            if (isEmpty(user)) {
                user = getProperty("user.name");
                if (isEmpty(user)) {
                    throw new RuntimeException("Could not determinate the current username.");
                }
            }
            return user;
        }

        @Nullable
        protected String getGivenUser() {
            return _user;
        }

        @Nullable
        @Override
        public String getPassword() {
            return _password;
        }

        @Override
        public String toString() {
            final InetSocketAddress address = getAddress();
            final String user = getGivenUser();
            final StringBuilder sb = new StringBuilder();
            if (!isEmpty(user)) {
                sb.append(user).append('@');
            }
            sb.append(address.getHostName());
            final int port = address.getPort();
            if (port != DEFAULT_PORT) {
                sb.append(':').append(port);
            }
            return sb.toString();
        }

        @Override
        public void close() throws IOException {}
    }

    public static class SerializerImpl implements ChunkAwareSerializer<SshRemote> {

        public static final byte REGULAR_TYPE = (Byte.MIN_VALUE + 1);
        public static final byte NULL_TYPE = (Byte.MIN_VALUE + 0);
        public static final int STRING_BUFFER_SIZE = DEFAULT_STRING_BUFFER_SIZE;
        public static final Charset CHARSET = DEFAULT_CHARSET;
        public static final int CHUNK_SIZE = 1 // type
            + DEFAULT_STRING_CHUNK_SIZE // hostname
            + INTEGER_CHUNK_SIZE // port
            + DEFAULT_STRING_CHUNK_SIZE // username
            + DEFAULT_STRING_CHUNK_SIZE // password
            ;

        @Override
        public int getChunkSize() {
            return CHUNK_SIZE;
        }

        @Override
        public void write(@Nullable SshRemote value, @Nonnull DataOutput to) throws IOException {
            if (value != null) {
                writeByte(REGULAR_TYPE, to);
                final InetSocketAddress address = value.getAddress();
                writeString(STRING_BUFFER_SIZE, address.getHostString(), CHARSET, to);
                writeInteger(address.getPort(), to);
                writeString(STRING_BUFFER_SIZE, value.getUser(), CHARSET, to);
                writeString(STRING_BUFFER_SIZE, value.getPassword(), CHARSET, to);
            } else {
                writeByte(NULL_TYPE, to);
                writeZeros(CHUNK_SIZE - 1, to);
            }
        }

        @Nullable
        @Override
        public SshRemote read(@Nonnull DataInput from) throws IOException {
            final short type = readByte(from);
            final SshRemote result;
            if (type == NULL_TYPE) {
                readZeros(from, CHUNK_SIZE - 1);
                result = null;
            } else if (type == REGULAR_TYPE) {
                final String hostString = readString(from, STRING_BUFFER_SIZE, CHARSET);
                final int port = readInteger(from);
                final String user = readString(from, STRING_BUFFER_SIZE, CHARSET);
                final String password = readString(from, STRING_BUFFER_SIZE, CHARSET);
                result = new Impl(createUnresolved(hostString, port), user, password);
            } else {
                throw new IOException("Found no valid type marker.");
            }
            return result;
        }
    }

}

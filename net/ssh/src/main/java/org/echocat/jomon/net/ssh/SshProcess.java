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

import org.echocat.jomon.net.ssh.SshProcess.Id;
import org.echocat.jomon.net.ssh.SshProcess.Id.SerializerImpl;
import org.echocat.jomon.runtime.io.ChunkAwareSerializer;
import org.echocat.jomon.runtime.io.Serializers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import static org.echocat.jomon.runtime.io.ByteUtils.LONG_CHUNK_SIZE;
import static org.echocat.jomon.runtime.io.StreamUtils.*;

public interface SshProcess extends org.echocat.jomon.process.Process<String, Id> {

    // IMPORTANT! This warning is suppressed because of an class import bug of the Java compiler. Only make the import of <code>@SerializableBy</code>
    // full qualified if you sure that this problem not longer persists.
    @SuppressWarnings("UnnecessaryFullyQualifiedName")
    @org.echocat.jomon.runtime.io.SerializableBy(SerializerImpl.class)
    public static interface Id {

        @Nullable
        public Long getPid();

        @Nonnull
        public SshRemote getRemote();

        public static class Impl implements Id {

            @Nonnull
            public static Id id(@Nullable Long pid, @Nonnull SshRemote session) {
                return new Impl(pid, session);
            }

            @Nullable
            private final Long _pid;
            @Nonnull
            private final SshRemote _remote;

            public Impl(@Nullable Long pid, @Nonnull SshRemote remote) {
                _pid = pid;
                _remote = remote;
            }

            @Override
            @Nullable
            public Long getPid() {
                return _pid;
            }

            @Override
            @Nonnull
            public SshRemote getRemote() {
                return _remote;
            }

            @Override
            public boolean equals(Object o) {
                final boolean result;
                if (this == o) {
                    result = true;
                } else if (!(o instanceof Id)) {
                    result = false;
                } else {
                    final Id that = (Id) o;
                    final Long pid = getPid();
                    result = (pid != null ? pid.equals(that.getPid()) : that.getPid() == null)
                        && (getRemote().equals(that.getRemote()));
                }
                return result;
            }

            @Override
            public int hashCode() {
                final Long pid = getPid();
                int result = pid != null ? pid.hashCode() : 0;
                result = 31 * result + getRemote().hashCode();
                return result;
            }

            @Override
            public String toString() {
                final Long pid = getPid();
                return (pid != null ? pid + "@" : "") + getRemote();
            }

        }

        public static class SerializerImpl implements ChunkAwareSerializer<Id> {

            public static final byte REGULAR_TYPE = (Byte.MIN_VALUE + 1);
            public static final byte NULL_TYPE = (Byte.MIN_VALUE + 0);
            public static final int CHUNK_SIZE = 1 // type
                + LONG_CHUNK_SIZE // pid
                + Serializers.getChunkSizeOf(SshRemote.class) // remote
                ;

            @Override
            public int getChunkSize() {
                return CHUNK_SIZE;
            }

            @Override
            public void write(@Nullable Id value, @Nonnull DataOutput to) throws IOException {
                if (value != null) {
                    writeByte(REGULAR_TYPE, to);
                    final Long pid = value.getPid();
                    writeLong(pid != null ? pid : 0, to);
                    writeObject(SshRemote.class, value.getRemote(), to);
                } else {
                    writeByte(NULL_TYPE, to);
                    writeZeros(CHUNK_SIZE - 1, to);
                }
            }

            @Nullable
            @Override
            public Id read(@Nonnull DataInput from) throws IOException {
                final short type = readByte(from);
                final Id result;
                if (type == NULL_TYPE) {
                    readZeros(from, CHUNK_SIZE - 1);
                    result = null;
                } else if (type == REGULAR_TYPE) {
                    final long pid = readLong(from);
                    final SshRemote remote = readObject(SshRemote.class, from);
                    result = new Impl(pid != 0 ? pid : null, remote);
                } else {
                    throw new IOException("Found no valid type marker.");
                }
                return result;
            }
        }
    }

}

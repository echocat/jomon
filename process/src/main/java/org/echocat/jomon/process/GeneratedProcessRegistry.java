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

package org.echocat.jomon.process;

import org.echocat.jomon.runtime.io.ChunkAwareSerializer;
import org.echocat.jomon.runtime.iterators.ConvertingIterator;
import org.echocat.jomon.runtime.util.ByteCount;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import java.io.*;
import java.util.*;

import static java.io.File.separator;
import static java.lang.System.getProperty;
import static java.lang.Thread.currentThread;
import static java.util.Collections.unmodifiableSet;
import static org.echocat.jomon.runtime.CollectionUtils.asIterator;
import static org.echocat.jomon.runtime.io.Serializers.getChunkAwareSerializerOf;
import static org.echocat.jomon.runtime.io.StreamUtils.writeZeros;
import static org.echocat.jomon.runtime.util.ByteCount.byteCountOf;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

@ThreadSafe
public class GeneratedProcessRegistry<ID, P extends GeneratedProcess<?, ID>> implements AutoCloseable {

    @Nonnull
    protected static final String DEFAULT_IDS_FILE_DIRECTORY_PATH_PREFIX = getProperty("user.home", ".") + separator + ".parentProcessRegistry";

    @Nonnull
    private final Map<ID, Long> _processIdToFilePosition = new HashMap<>();
    @Nonnull
    private final Set<Long> _freeFilePositions = new HashSet<>();
    @Nonnull
    private final File _file;
    @Nonnull
    private final RandomAccessFile _access;
    private final long _parentLocalProcessId;
    @Nonnull
    private final String _type;
    @Nonnull
    private final ChunkAwareSerializer<ID> _idSerializer;
    @Nonnull
    private final Class<ID> _idType;

    public GeneratedProcessRegistry(long parentLocalProcessId, @Nonnull String type, @Nonnull Class<ID> idType) {
        _parentLocalProcessId = parentLocalProcessId;
        _type = type;
        _idType = idType;
        final File idsFileDirectory = getIdsFileDirectoryFor(_type);
        _file = getIdsFileFor(idsFileDirectory, parentLocalProcessId);
        _access = openIdsFile(_file);
        _idSerializer = getChunkAwareSerializerOf(idType);
    }

    @Nonnull
    protected ByteCount getChunkSizeFor(@Nonnull Class<ID> idType, @Nullable ChunkAwareSerializer<ID> idSerializer) {
        final long byteCount;
        if (idType == Long.class) {
            byteCount = 8;
        } else if (idType == Integer.class) {
            byteCount = 4;
        } else if (idType == Short.class) {
            byteCount = 2;
        } else if (idType == Byte.class) {
            byteCount = 1;
        } else {
            if (idSerializer != null) {
                byteCount = idSerializer.getChunkSize();
            } else {
                throw new UnsupportedOperationException("This class does not support an idType '" + idType.getName() + "'.");
            }
        }
        return byteCountOf(byteCount);
    }

    @Nonnull
    public String getType() {
        return _type;
    }

    public long getParentLocalProcessId() {
        return _parentLocalProcessId;
    }

    @Nonnull
    protected RandomAccessFile openIdsFile(@Nonnull File file) {
        try {
            return new RandomAccessFile(file, "rw");
        } catch (final FileNotFoundException e) {
            throw new IllegalArgumentException("Could not open ids file (" + file + ").", e);
        }
    }

    public void register(@Nonnull P process) {
        if (process.isDaemon()) {
            final ID id = process.getId();
            if (id != null) {
                synchronized (this) {
                    if (!_processIdToFilePosition.containsKey(id)) {
                        registerUnknownProcessWith(id);
                        startAutomaticDeRegistrationFor(process);
                    }
                }
            }
        }
    }

    protected void startAutomaticDeRegistrationFor(@Nonnull P process) {
        final Thread waiter = new Thread(new WaitForEnd(process), "Wait for end of " + process);
        waiter.setDaemon(true);
        waiter.start();
    }

    @GuardedBy("this")
    protected void registerUnknownProcessWith(@Nonnull ID id) {
        final long position = getFreePosition();
        try {
            _access.seek(position);
            writeIdToChunk(id);
        } catch (final IOException e) {
            throw new RuntimeException("Could not write id " + id + " at position " + position + " in " + _access + ".", e);
        }
        _processIdToFilePosition.put(id, position);
        _freeFilePositions.remove(position);
    }

    protected void writeIdToChunk(@Nonnull ID id) throws IOException {
        _idSerializer.write(id, _access);
    }

    public void unregister(@Nonnull P process) {
        if (process.isDaemon()) {
            final ID id = process.getId();
            if (id != null) {
                synchronized (this) {
                    if (_processIdToFilePosition.containsKey(id)) {
                        unregisterKnownProcessWith(id);
                    }
                }
            }
        }
    }

    @Nonnull
    public Set<ID> getAllIds() {
        synchronized (this) {
            try {
                final Set<ID> result = new HashSet<>((int) (_access.length() / _idSerializer.getChunkSize()));
                _access.seek(0);
                boolean hasNext;
                do {
                    try {
                        final ID id = readIdFromChunk();
                        result.add(id);
                        hasNext = true;
                    } catch (final EOFException ignored) {
                        hasNext = false;
                    }
                } while (hasNext);
                return unmodifiableSet(result);
            } catch (final IOException e) {
                throw new RuntimeException("Could not read stored ids from " + _access + ".", e);
            }
        }
    }

    @Nonnull
    protected ID readIdFromChunk() throws IOException {
        return _idSerializer.read(_access);
    }


    @GuardedBy("this")
    protected void unregisterKnownProcessWith(@Nonnull ID id) {
        final Long position = _processIdToFilePosition.get(id);
        if (position != null) {
            try {
                _access.seek(position);
                writeZeros(_idSerializer.getChunkSize(), _access);
            } catch (final IOException e) {
                throw new RuntimeException("Could not delete id " + id + " at position " + position + " from " + _access + ".", e);
            }
            _processIdToFilePosition.remove(id);
            _freeFilePositions.add(position);
        }
    }

    @Nonnegative
    @GuardedBy("this")
    protected long getFreePosition() {
        final Iterator<Long> i = _freeFilePositions.iterator();
        final long result;
        if (i.hasNext()) {
            result = i.next();
        } else {
            try {
                result = _access.length();
            } catch (final IOException e) {
                throw new RuntimeException("Could not retrieve the length of " + _access + ".", e);
            }
        }
        return result;
    }

    public void clear() throws IOException {
        synchronized(this) {
            _access.setLength(0);
        }
    }

    @Nonnull
    protected static File getIdsFileFor(@Nonnull File idsFileDirectory, @Nonnegative long parentLocalProcessId) {
        return new File(idsFileDirectory, Long.toString(parentLocalProcessId));
    }

    @Nonnull
    public Iterable<GeneratedProcessRegistry<ID, P>> getKnownInstances() {
        return getKnownInstancesFor(_type, _idType);
    }

    @Nonnull
    public static <ID, P extends GeneratedProcess<?, ID>> Iterable<GeneratedProcessRegistry<ID, P>> getKnownInstancesFor(@Nonnull final String type, @Nonnull final Class<ID> idType) {
        final File idsFileDirectory = getIdsFileDirectoryFor(type);
        return new Iterable<GeneratedProcessRegistry<ID, P>>() {
            @Override
            public Iterator<GeneratedProcessRegistry<ID, P>> iterator() {
                final String[] plainIds = idsFileDirectory.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        boolean result;
                        try {
                            Long.valueOf(name);
                            result = true;
                        } catch (final NumberFormatException ignored) {
                            result = false;
                        }
                        return result;
                    }
                });
                final Iterator<String> ids = asIterator(plainIds);
                return new ConvertingIterator<String, GeneratedProcessRegistry<ID, P>>(ids) {
                    @Override
                    protected GeneratedProcessRegistry<ID, P> convert(String plainId) {
                        final long id = Long.valueOf(plainId);
                        return new GeneratedProcessRegistry<>(id, type, idType);
                    }
                };
            }
        };
    }

    @Override
    public void close() {
        synchronized (this) {
            try {
                closeQuietly(_access);
            } finally {
                _file.delete();
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    @Nonnull
    protected static File getIdsFileDirectoryFor(@Nonnull String type) {
        final String idsFileDirectoryPath = getProperty(GeneratedProcessRegistry.class + ".idsFileDirectory." + type, DEFAULT_IDS_FILE_DIRECTORY_PATH_PREFIX + "/" + type);
        final File idsFileDirectory = new File(idsFileDirectoryPath);
        idsFileDirectory.mkdirs();
        if (!idsFileDirectory.isDirectory()) {
            throw new IllegalArgumentException("Illegal idsFileDirectory specified: " + idsFileDirectoryPath);
        }
        return idsFileDirectory;
    }

    protected class WaitForEnd implements Runnable {

        private final P _process;

        public WaitForEnd(@Nonnull P process) {
            _process = process;
        }

        @Override
        public void run() {
            try {
                _process.waitFor();
                unregister(_process);
            } catch (final InterruptedException ignored) {
                currentThread().interrupt();
            } catch (final Exception ignored) {}
        }
    }
}

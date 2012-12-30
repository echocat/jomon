/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.process;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import java.io.*;
import java.util.*;

import static java.lang.System.getProperty;
import static java.lang.Thread.currentThread;
import static java.util.Collections.unmodifiableSet;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

@ThreadSafe
public class GeneratedProcessRegistry implements AutoCloseable {

    private static final String DEFAULT_IDS_FILE_DIRECTORY_PATH = getProperty("user.home", File.separator) + File.separator + ".parentProcessRegistry";

    public static final File IDS_FILE_DIRECTORY = getIdsFileDirectory();

    private final Map<Long, Long> _processIdToFilePosition = new HashMap<>();
    private final Set<Long> _freeFilePositions = new HashSet<>();
    private final File _file;
    private final RandomAccessFile _access;

    public GeneratedProcessRegistry(long parentProcessId) {
        _file = getIdsFileFor(parentProcessId);
        _access = openIdsFile(_file);
    }

    @Nonnull
    protected RandomAccessFile openIdsFile(@Nonnull File file) {
        try {
            return new RandomAccessFile(file, "rw");
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Could not open ids file (" + file + ").", e);
        }
    }

    public void register(@Nonnull GeneratedProcess process) {
        if (process.isDaemon()) {
            final long id = process.getId();
            synchronized (this) {
                if (!_processIdToFilePosition.containsKey(id)) {
                    registerUnknownProcessWith(id);
                    startAutomaticDeRegistrationFor(process);
                }
            }
        }
    }

    protected void startAutomaticDeRegistrationFor(@Nonnull GeneratedProcess process) {
        final Thread waiter = new Thread(new WaitForEnd(process), "Wait for end of " + process);
        waiter.setDaemon(true);
        waiter.start();
    }

    @GuardedBy("this")
    protected void registerUnknownProcessWith(long id) {
        final long position = getFreePosition();
        try {
            _access.seek(position);
            _access.writeLong(id);
        } catch (IOException e) {
            throw new RuntimeException("Could not write id " + id + " at position " + position + " in " + _access + ".", e);
        }
        _processIdToFilePosition.put(id, position);
        _freeFilePositions.remove(position);
    }

    public void unregister(@Nonnull GeneratedProcess process) {
        if (process.isDaemon()) {
            final long id = process.getId();
            synchronized (this) {
                if (_processIdToFilePosition.containsKey(id)) {
                    unregisterKnownProcessWith(id);
                }
            }
        }
    }

    @Nonnull
    public Set<Long> getAllIds() {
        synchronized (this) {
            try {
                final Set<Long> result = new HashSet<>((int) (_access.length() / 8));
                _access.seek(0);
                boolean hasNext;
                do {
                    try {
                        final long id = _access.readLong();
                        if (id != 0) {
                            result.add(id);
                        }
                        hasNext = true;
                    } catch (EOFException ignored) {
                        hasNext = false;
                    }
                } while (hasNext);
                return unmodifiableSet(result);
            } catch (IOException e) {
                throw new RuntimeException("Could not read stored ids from " + _access + ".", e);
            }
        }
    }

    @GuardedBy("this")
    protected void unregisterKnownProcessWith(long id) {
        final Long position = _processIdToFilePosition.get(id);
        if (position != null) {
            try {
                _access.seek(position);
                _access.writeLong(0);
            } catch (IOException e) {
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
            } catch (IOException e) {
                throw new RuntimeException("Could not retrieve the length of " + _access + ".", e);
            }
        }
        return result;
    }

    @Nonnull
    public static File getIdsFileFor(@Nonnegative long parentProcessId) {
        return new File(IDS_FILE_DIRECTORY, Long.toString(parentProcessId));
    }

    @Nonnull
    public static long[] getParentProcessIds() {
        final String[] plainIds = IDS_FILE_DIRECTORY.list(new FilenameFilter() { @Override public boolean accept(File dir, String name) {
            boolean result;
            try {
                Long.valueOf(name);
                result = true;
            } catch (NumberFormatException ignored) {
                result = false;
            }
            return result;
        }});
        final long[] ids = new long[plainIds.length];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = Long.valueOf(plainIds[i]);
        }
        return ids;
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
    private static File getIdsFileDirectory() {
        final String idsFileDirectoryPath = getProperty(GeneratedProcessRegistry.class + ".idsFileDirectory", DEFAULT_IDS_FILE_DIRECTORY_PATH);
        final File idsFileDirectory = new File(idsFileDirectoryPath);
        idsFileDirectory.mkdirs();
        if (!idsFileDirectory.isDirectory()) {
            throw new IllegalArgumentException("Illegal idsFileDirectory specified: " + idsFileDirectoryPath);
        }
        return idsFileDirectory;
    }

    protected class WaitForEnd implements Runnable {

        private final GeneratedProcess _process;

        public WaitForEnd(@Nonnull GeneratedProcess process) {
            _process = process;
        }

        @Override
        public void run() {
            try {
                _process.waitFor();
                unregister(_process);
            } catch (InterruptedException ignored) {
                currentThread().interrupt();
            } catch (Exception ignored) {}
        }
    }
}

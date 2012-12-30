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

package org.echocat.jomon.process.sigar;

import org.echocat.jomon.runtime.concurrent.Daemon;
import org.echocat.jomon.runtime.iterators.CloseableIterator;
import org.echocat.jomon.runtime.iterators.ConvertingIterator;
import org.echocat.jomon.process.*;
import org.echocat.jomon.process.Process;
import org.echocat.jomon.runtime.util.Duration;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarPermissionDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.*;

import static org.echocat.jomon.runtime.iterators.IteratorUtils.filter;
import static org.echocat.jomon.process.GeneratedProcessRegistry.getParentProcessIds;
import static org.echocat.jomon.process.Signal.*;
import static org.echocat.jomon.process.sigar.SigarInitializer.initializeSigar;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

@ThreadSafe
public class SigarProcessRepository extends ProcessRepository {

    private static final Map<Long, Long> ONE_LONG_INSTANCE = new WeakHashMap<>();
    private static final Logger LOG = LoggerFactory.getLogger(SigarProcessRepository.class);

    private final KillDaemonsOfDeadProcesses _killDaemonsOfDeadProcesses = new KillDaemonsOfDeadProcesses();

    private Sigar _sigar;
    private GeneratedProcessRegistry _generatedProcessRegistry;
    private Daemon _killDaemonsOfDeadProcessesDaemon;

    @Override
    public Process findOneBy(@Nonnull Long id) {
        Long idInstance;
        synchronized (ONE_LONG_INSTANCE) {
            idInstance = ONE_LONG_INSTANCE.get(id);
            if (idInstance == null && id != null) {
                ONE_LONG_INSTANCE.put(id, id);
                idInstance = id;
            }
        }
        Process process;
        try {
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (idInstance) {
                _sigar.getProcState(idInstance);
            }
            process = new SigarProcess(id, _sigar);
        } catch (SigarException ignored) {
            process = null;
        }
        return process;
    }

    @Nonnull
    @Override
    public CloseableIterator<Process> findBy(@Nonnull ProcessQuery query) {
        final CloseableIterator<Process> result;
        final List<Long> ids = query.getIds();
        if (ids != null) {
            result = new ToProcessConvertingIterator(ids);
        } else {
            try {
                result = new KnownIdsProcessIterator(_sigar.getProcList(), _sigar);
            } catch (SigarException e) {
                throw new RuntimeException("Could not get process list for " + query + ".", e);
            }
        }
        return filter(result, query);
    }

    @Nonnull
    @Override
    protected GeneratedProcess toControllableProcess(@Nonnull Process placeHolder, @Nonnull java.lang.Process original, boolean isDaemon) {
        final GeneratedProcess result = super.toControllableProcess(placeHolder, original, isDaemon);
        _generatedProcessRegistry.register(result);
        return result;
    }

    @Override
    public void send(@Nonnull Process to, @Nonnull Signal signal) {
        final int code;
        if (signal == terminate) {
            code = 15;
        } else if (signal == kill) {
            code = 9;
        } else if (signal == interrupt) {
            code = 2;
        } else {
            throw new IllegalArgumentException("Could not handle signal: " + signal);
        }
        try {
            _sigar.kill(to.getId(), code);
        } catch (SigarPermissionDeniedException e) {
            throw new AccessDeniedException("Could not kill: " + to, e);
        } catch (SigarException e) {
            throw new RuntimeException("Could not kill: " + to, e);
        }
    }

    @Override
    protected boolean couldHandleThisVirtualMachine() {
        boolean result;
        try {
            initializeSigar();
            result = true;
        } catch (Exception e) {
            LOG.info("Could not load sigar.", e);
            result = false;
        }
        return result;
    }

    @Override
    protected long getCurrentJvmId() {
        return _sigar.getPid();
    }

    @Override
    protected void init() {
        if (_sigar != null || _generatedProcessRegistry != null) {
            close();
        }
        initializeSigar();
        _sigar = new Sigar();
        _generatedProcessRegistry = new GeneratedProcessRegistry(_sigar.getPid());
        startDaemons();
    }

    protected void startDaemons() {
        final Daemon daemon = new Daemon(_killDaemonsOfDeadProcesses);
        daemon.setInterval(new Duration("1m"));
        try {
            daemon.init();
        } catch (Exception e) {
            throw new RuntimeException("Could not start killDaemonsOfDeadProcessesDaemon.", e);
        }
        daemon.run();
        _killDaemonsOfDeadProcessesDaemon = daemon;
    }

    @Override
    public void close() {
        try {
            closeQuietly(_generatedProcessRegistry);
        } finally {
            try {
                closeQuietly(_killDaemonsOfDeadProcessesDaemon);
            } finally {
                try {
                    final Sigar sigar = _sigar;
                    if (sigar != null) {
                        sigar.close();
                    }
                } catch (Exception ignored) {}
            }
        }
    }



    @Nonnull
    protected static SigarProcess toProcess(long pid, @Nonnull Sigar sigar) {
        return new SigarProcess(pid, sigar);
    }

    protected class ToProcessConvertingIterator extends ConvertingIterator<Long, Process> {

        public ToProcessConvertingIterator(@Nonnull Iterable<Long> ids) {
            this(ids.iterator());
        }

        public ToProcessConvertingIterator(@Nonnull Iterator<Long> ids) {
            super(ids);
        }

        @Override
        protected Process convert(@Nullable Long id) {
            return id != null ? findOneBy(id) : null;
        }

    }

    protected static class KnownIdsProcessIterator implements CloseableIterator<Process> {

        private final long[] _pids;
        private final Sigar _sigar;

        private int _index;

        public KnownIdsProcessIterator(@Nonnull long[] pids, @Nonnull Sigar sigar) {
            _pids = pids;
            _sigar = sigar;
        }

        @Override
        public void close() {}

        @Override
        public boolean hasNext() {
            return _index < _pids.length;
        }

        @Override
        public Process next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            final long pid = _pids[_index++];
            return toProcess(pid, _sigar);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    protected class KillDaemonsOfDeadProcesses implements Runnable {
        @Override
        public void run() {
            final Sigar sigar = _sigar;
            if (sigar != null) {
                for (long parentProcessId : getParentProcessIds()) {
                    if (findOneBy(parentProcessId) == null) {
                        try (final GeneratedProcessRegistry registry = new GeneratedProcessRegistry(parentProcessId)) {
                            for (Long processId : registry.getAllIds()) {
                                try {
                                    sigar.kill(processId, 9);
                                } catch (SigarException ignored) {}
                            }
                        }
                    }
                }
            }
        }
    }
}

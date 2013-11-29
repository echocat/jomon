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

package org.echocat.jomon.process.sigar;

import org.echocat.jomon.process.AccessDeniedException;
import org.echocat.jomon.process.GeneratedProcessRegistry;
import org.echocat.jomon.process.Signal;
import org.echocat.jomon.process.local.LocalGeneratedProcess;
import org.echocat.jomon.process.local.LocalProcess;
import org.echocat.jomon.process.local.LocalProcessQuery;
import org.echocat.jomon.process.local.LocalProcessRepository;
import org.echocat.jomon.runtime.concurrent.Daemon;
import org.echocat.jomon.runtime.iterators.CloseableIterator;
import org.echocat.jomon.runtime.iterators.ConvertingIterator;
import org.echocat.jomon.runtime.util.Duration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.*;

import static org.echocat.jomon.process.Signal.*;
import static org.echocat.jomon.process.sigar.SigarFacadeFactory.create;
import static org.echocat.jomon.process.sigar.SigarFacadeFactory.isAvialable;
import static org.echocat.jomon.runtime.iterators.IteratorUtils.filter;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

@ThreadSafe
public class SigarProcessRepository extends LocalProcessRepository {

    private static final Map<Long, Long> ONE_LONG_INSTANCE = new WeakHashMap<>();

    @Nonnull
    private final KillDaemonsOfDeadProcesses _killDaemonsOfDeadProcesses = new KillDaemonsOfDeadProcesses();

    @Nullable
    private final SigarFacade _sigar;
    @Nullable
    private final GeneratedProcessRegistry<Long, LocalGeneratedProcess> _generatedProcessRegistry;
    @Nullable
    private final Daemon _killDaemonsOfDeadProcessesDaemon;

    public SigarProcessRepository() {
        _sigar = isAvialable() ? create() : null;
        _generatedProcessRegistry = _sigar != null ? new GeneratedProcessRegistry<Long, LocalGeneratedProcess>(_sigar.getPid(), "local", Long.class) : null;
        _killDaemonsOfDeadProcessesDaemon = _sigar != null ? startDaemon() : null;
    }

    @Override
    public LocalProcess findOneBy(@Nonnull Long id) {
        Long idInstance;
        synchronized (ONE_LONG_INSTANCE) {
            idInstance = ONE_LONG_INSTANCE.get(id);
            if (idInstance == null && id != null) {
                ONE_LONG_INSTANCE.put(id, id);
                idInstance = id;
            }
        }
        LocalProcess process;
        try {
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (idInstance) {
                getSigar().getProcState(idInstance);
            }
            process = new SigarProcess(id, getSigar());
        } catch (final Exception ignored) {
            process = null;
        }
        return process;
    }

    @Nonnull
    @Override
    public CloseableIterator<LocalProcess> findBy(@Nonnull LocalProcessQuery query) {
        final CloseableIterator<LocalProcess> result;
        final List<Long> ids = query.getIds();
        if (ids != null) {
            result = new ToProcessConvertingIterator(ids);
        } else {
            try {
                result = new KnownIdsProcessIterator(getSigar().getProcList(), getSigar());
            } catch (final Exception e) {
                throw new RuntimeException("Could not get process list for " + query + ".", e);
            }
        }
        return filter(result, query);
    }

    @Nonnull
    @Override
    protected LocalGeneratedProcess toControllableProcess(@Nonnull LocalProcess placeHolder, @Nonnull Process original, boolean isDaemon) {
        final LocalGeneratedProcess result = super.toControllableProcess(placeHolder, original, isDaemon);
        _generatedProcessRegistry.register(result);
        return result;
    }

    @Override
    public void send(@Nonnull LocalProcess to, @Nonnull Signal signal) {
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
        getSigar().kill(to.getId(), code);
    }

    @Nonnull
    protected SigarFacade getSigar() {
        if (_sigar == null) {
            throw new UnsupportedOperationException("This method is not available because sigar does not completely initialized on this platform. See previous log entries for more information.");
        }
        return _sigar;
    }

    @Override
    public boolean isAvailable() {
        return _sigar != null;
    }

    @Override
    public long getThisPid() {
        return getSigar().getPid();
    }

    @Nonnull
    protected Daemon startDaemon() {
        final Daemon daemon = new Daemon(_killDaemonsOfDeadProcesses);
        daemon.setInterval(new Duration("1m"));
        try {
            daemon.init();
        } catch (final Exception e) {
            throw new RuntimeException("Could not start killDaemonsOfDeadProcessesDaemon.", e);
        }
        daemon.run();
        return daemon;
    }

    @Override
    public void close() {
        try {
            closeQuietly(_generatedProcessRegistry);
        } finally {
            try {
                closeQuietly(_killDaemonsOfDeadProcessesDaemon);
            } finally {
                closeQuietly(getSigar());
            }
        }
    }



    @Nonnull
    protected static SigarProcess toProcess(long pid, @Nonnull SigarFacade sigar) {
        return new SigarProcess(pid, sigar);
    }

    protected class ToProcessConvertingIterator extends ConvertingIterator<Long, LocalProcess> {

        public ToProcessConvertingIterator(@Nonnull Iterable<Long> ids) {
            this(ids.iterator());
        }

        public ToProcessConvertingIterator(@Nonnull Iterator<Long> ids) {
            super(ids);
        }

        @Override
        protected LocalProcess convert(@Nullable Long id) {
            return id != null ? findOneBy(id) : null;
        }

    }

    protected static class KnownIdsProcessIterator implements CloseableIterator<LocalProcess> {

        private final long[] _pids;
        private final SigarFacade _sigar;

        private int _index;

        public KnownIdsProcessIterator(@Nonnull long[] pids, @Nonnull SigarFacade sigar) {
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
        public LocalProcess next() {
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
            final SigarFacade sigar = getSigar();
            if (sigar != null) {
                for (final GeneratedProcessRegistry<Long, LocalGeneratedProcess> registry : GeneratedProcessRegistry.<Long, LocalGeneratedProcess>getKnownInstancesFor("local", Long.class)) {
                    if (findOneBy(registry.getParentLocalProcessId()) == null) {
                        for (final Long processId : registry.getAllIds()) {
                            try {
                                sigar.kill(processId, 9);
                            } catch (IllegalArgumentException | AccessDeniedException ignored) {}
                        }
                    }
                }
            }
        }
    }
}

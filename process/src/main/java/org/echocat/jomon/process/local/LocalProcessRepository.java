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

package org.echocat.jomon.process.local;

import org.echocat.jomon.process.DummyProcess.LocalDummyProcess;
import org.echocat.jomon.process.Signal;
import org.echocat.jomon.runtime.generation.Generator;
import org.echocat.jomon.runtime.io.UncheckedIOException;
import org.echocat.jomon.runtime.repository.QueryableRepository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.util.ServiceLoader;

import static org.echocat.jomon.process.local.LocalProcessQuery.query;
import static org.echocat.jomon.runtime.CollectionUtils.countElementsOf;
import static org.echocat.jomon.runtime.CollectionUtils.findFirstOf;

@ThreadSafe
public abstract class LocalProcessRepository implements QueryableRepository<LocalProcessQuery, Long, LocalProcess>, Generator<LocalGeneratedProcess, LocalGeneratedProcessRequirement>, AutoCloseable {

    private static final LocalProcessRepository INSTANCE = createInstance();

    @Nonnull
    public static LocalProcessRepository getInstance() {
        return INSTANCE;
    }

    @Nonnull
    public static LocalProcessRepository processRepository() {
        return getInstance();
    }

    @Nullable
    public LocalProcess findOneBy(@Nonnull Process process) {
        return findOneBy(getIdOf(process));
    }

    @Override
    public LocalProcess findOneBy(@Nonnull Long id) {
        return findOneBy(query().withId(id));
    }

    @Override
    public LocalProcess findOneBy(@Nonnull LocalProcessQuery query) {
        return findFirstOf(findBy(query));
    }

    @Override
    public long countBy(@Nonnull LocalProcessQuery query) {
        return countElementsOf(findBy(query));
    }

    @Nonnull
    @Override
    public LocalGeneratedProcess generate(@Nonnull LocalGeneratedProcessRequirement requirement) throws UncheckedIOException {
        final ProcessBuilder pb = new ProcessBuilder();
        pb.command(requirement.getCompleteCommandLine());
        pb.environment().putAll(requirement.getEnvironment());
        pb.directory(requirement.getWorkingDirectory());
        final Process original;
        try {
            original = pb.start();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
        final long id = getIdOf(original);
        final LocalProcess placeHolder = findOneBy(id);
        final LocalProcess process = placeHolder != null ? placeHolder : new LocalDummyProcess(id);
        return toControllableProcess(process, original, requirement.isDaemon());
    }

    public abstract void send(@Nonnull LocalProcess to, @Nonnull Signal signal);

    @Override
    public void close() {}

    public abstract boolean isAvailable();

    public abstract long getThisPid();

    protected long getIdOf(@Nonnull Process process) {
        return ProcessPidResolver.getInstance().resolvePidOf(process);
    }

    @Nonnull
    protected static LocalProcessRepository createInstance() {
        final ServiceLoader<LocalProcessRepository> found = ServiceLoader.load(LocalProcessRepository.class);
        LocalProcessRepository processRepository = null;
        for (final LocalProcessRepository candidate : found) {
            if (candidate.isAvailable()) {
                processRepository = candidate;
                break;
            }
        }
        if (processRepository == null) {
            throw new IllegalArgumentException("Could not find any matching implementation of " + LocalProcessRepository.class.getName() + " for this virtual machine.");
        }
        closeOnJvmShutdown(processRepository);
        return processRepository;
    }

    private static void closeOnJvmShutdown(@Nonnull final LocalProcessRepository processRepository) {
        Runtime.getRuntime().addShutdownHook(new Thread() { @Override public void run() {
            processRepository.close();
        }});
    }

    @Nonnull
    protected LocalGeneratedProcess toControllableProcess(@Nonnull LocalProcess placeHolder, @Nonnull Process original, boolean isDaemon) {
        return new LocalGeneratedProcessImpl(placeHolder, original, isDaemon);
    }

}

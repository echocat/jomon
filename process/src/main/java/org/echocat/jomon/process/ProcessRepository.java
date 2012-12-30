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

import org.echocat.jomon.runtime.generation.Generator;
import org.echocat.jomon.runtime.io.UncheckedIOException;
import org.echocat.jomon.runtime.repository.QueryableRepository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.util.ServiceLoader;

import static org.echocat.jomon.runtime.CollectionUtils.countElementsOf;
import static org.echocat.jomon.runtime.CollectionUtils.findFirstOf;
import static org.echocat.jomon.process.ProcessQuery.query;

@SuppressWarnings("UnnecessaryFullyQualifiedName")
@ThreadSafe
public abstract class ProcessRepository implements QueryableRepository<ProcessQuery, Long, Process>, Generator<GeneratedProcess, GeneratedProcessRequirement>, AutoCloseable {

    private static final ProcessRepository INSTANCE = createInstance();

    @Nonnull
    public static ProcessRepository getInstance() {
        return INSTANCE;
    }

    @Nullable
    public Process findOneBy(@Nonnull java.lang.Process process) {
        return findOneBy(getIdOf(process));
    }

    @Override
    public Process findOneBy(@Nonnull Long id) {
        return findOneBy(query().withId(id));
    }

    @Override
    public Process findOneBy(@Nonnull ProcessQuery query) {
        return findFirstOf(findBy(query));
    }

    @Override
    public long countBy(@Nonnull ProcessQuery query) {
        return countElementsOf(findBy(query));
    }

    @Nonnull
    @Override
    public GeneratedProcess generate(@Nonnull GeneratedProcessRequirement requirement) throws UncheckedIOException {
        final ProcessBuilder pb = new ProcessBuilder();
        pb.command(requirement.getCompleteCommandLine());
        pb.environment().putAll(requirement.getEnvironment());
        pb.directory(requirement.getWorkingDirectory());
        final java.lang.Process original;
        try {
            original = pb.start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        final long id = getIdOf(original);
        final Process placeHolder = findOneBy(id);
        return toControllableProcess(placeHolder != null ? placeHolder : new DummyProcess(id), original, requirement.isDaemon());
    }

    public abstract void send(@Nonnull Process to, @Nonnull Signal signal);

    @Override
    public void close() {}

    protected abstract boolean couldHandleThisVirtualMachine();

    protected abstract void init();

    protected abstract long getCurrentJvmId();

    protected long getIdOf(@Nonnull java.lang.Process process) {
        return ProcessPidResolver.getInstance().resolvePidOf(process);
    }

    @Nonnull
    protected static ProcessRepository createInstance() {
        final ServiceLoader<ProcessRepository> found = ServiceLoader.load(ProcessRepository.class);
        ProcessRepository processRepository = null;
        for (ProcessRepository candidate : found) {
            if (candidate.couldHandleThisVirtualMachine()) {
                processRepository = candidate;
                break;
            }
        }
        if (processRepository == null) {
            throw new IllegalArgumentException("Could not find any matching implementation of " + ProcessRepository.class.getName() + " for this virtual machine.");
        }
        processRepository.init();
        closeOnJvmShutdown(processRepository);
        return processRepository;
    }

    private static void closeOnJvmShutdown(@Nonnull final ProcessRepository processRepository) {
        Runtime.getRuntime().addShutdownHook(new Thread() { @Override public void run() {
            processRepository.close();
        }});
    }

    @Nonnull
    protected GeneratedProcess toControllableProcess(@Nonnull Process placeHolder, @Nonnull java.lang.Process original, boolean isDaemon) {
        return new GeneratedProcessImpl(placeHolder, original, isDaemon);
    }

}

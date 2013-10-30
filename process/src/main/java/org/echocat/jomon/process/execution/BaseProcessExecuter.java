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

package org.echocat.jomon.process.execution;

import org.echocat.jomon.process.BaseGeneratedProcessRequirement;
import org.echocat.jomon.process.GeneratedProcess;
import org.echocat.jomon.runtime.generation.Generator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.io.IOException;

import static org.echocat.jomon.runtime.io.StreamType.stderr;
import static org.echocat.jomon.runtime.io.StreamType.stdout;

public class BaseProcessExecuter<
    E,
    ID,
    R extends BaseGeneratedProcessRequirement<E, ?>,
    P extends GeneratedProcess<E, ID>,
    G extends Generator<P, R>,
    T extends BaseProcessExecuter<E, ID, R, P, G, T>
> implements ProcessExecuter<R> {

    @Nonnull
    private final G _processGenerator;

    @Nonnegative
    private int _readSize = 4096;

    public BaseProcessExecuter(@Nonnull G processGenerator) {
        _processGenerator = processGenerator;
    }

    @Override
    @Nonnull
    public Response execute(@Nonnull R requirement) throws InterruptedException, IOException {
        try (final P process = _processGenerator.generate(requirement)) {
            try (final OutputMonitor<E, ID> stdoutMonitor = new OutputMonitor<>(process, stdout, _readSize)) {
                try (final OutputMonitor<E, ID> stderrMonitor = new OutputMonitor<>(process, stderr, _readSize)) {
                    final int exitCode = process.waitFor();
                    stdoutMonitor.waitFor();
                    stderrMonitor.waitFor();
                    return new Response(stdoutMonitor.getRecordedContent(), stderrMonitor.getRecordedContent(), exitCode);
                }
            }
        }
    }

    @Nonnull
    public T withReadSize(@Nonnegative int readSize) {
        setReadSize(readSize);
        return thisObject();
    }

    public void setReadSize(@Nonnegative int readSize) {
        _readSize = readSize;
    }

    @Nonnegative
    public int getReadSize() {
        return _readSize;
    }

    @Nonnull
    protected T thisObject() {
        // noinspection unchecked
        return (T) this;
    }
}

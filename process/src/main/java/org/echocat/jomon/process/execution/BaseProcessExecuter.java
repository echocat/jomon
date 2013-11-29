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
import org.echocat.jomon.process.execution.Drain.AsBuffering;
import org.echocat.jomon.runtime.generation.Generator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.Charset;

import static java.nio.charset.Charset.defaultCharset;
import static java.nio.charset.Charset.forName;
import static org.echocat.jomon.process.execution.Drain.AsBuffering.drainThatBuffers;
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

    @Nonnull
    private Charset _charset = defaultCharset();

    public BaseProcessExecuter(@Nonnull G processGenerator) {
        _processGenerator = processGenerator;
    }

    @Nonnull
    @Override
    public Response execute(@Nonnull R requirement) throws InterruptedException, IOException {
        return execute(requirement, null);
    }

    @Override
    @Nonnull
    public Response execute(@Nonnull R requirement, @Nullable Drains drains) throws InterruptedException, IOException {
        final Drain out = drains != null ? drains.stdout() : drainThatBuffers();
        final Drain err = drains != null ? drains.stderr() : drainThatBuffers();
        try (final P process = _processGenerator.generate(requirement)) {
            try (final OutputMonitor<E, ID> stdoutMonitor = new OutputMonitor<>(process, stdout, out)) {
                try (final OutputMonitor<E, ID> stderrMonitor = new OutputMonitor<>(process, stderr, err)) {
                    final int exitCode = process.waitFor();
                    stdoutMonitor.waitFor();
                    stderrMonitor.waitFor();
                    return new Response(asString(out), asString(err), exitCode);
                }
            }
        }
    }

    @Nonnull
    protected String asString(@Nonnull Drain drain) {
        final String result;
        if (drain instanceof AsBuffering) {
            result = ((AsBuffering) drain).toString(_charset);
        } else {
            result = null;
        }
        return result;
    }

    @Nonnull
    public T decodingWith(@Nonnull Charset charset) {
        _charset = charset;
        return thisObject();
    }

    @Nonnull
    public T decodingWith(@Nonnull String charset) {
        return decodingWith(forName(charset));
    }

    @Nonnull
    public Charset getCharset() {
        return _charset;
    }

    @Nonnull
    protected T thisObject() {
        // noinspection unchecked
        return (T) this;
    }
}

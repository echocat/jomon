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

import org.echocat.jomon.process.GeneratedProcess;
import org.echocat.jomon.runtime.concurrent.ThreadUtils;
import org.echocat.jomon.runtime.io.StreamType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Boolean.TRUE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.echocat.jomon.runtime.io.StreamType.stdout;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

public class OutputMonitor<E, ID> extends Thread implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(OutputMonitor.class);

    @Nonnull
    private final ThreadLocal<Boolean> _alreadyInClosing = new ThreadLocal<>();
    @Nonnull
    private final InputStream _stream;
    @Nonnull
    private final Drain _drain;

    @Nonnull
    private final Lock _lock = new ReentrantLock();
    @Nonnull
    private final Condition _condition = _lock.newCondition();

    private boolean _alive = true;


    public OutputMonitor(@Nonnull GeneratedProcess<E, ID> process, @Nonnull StreamType streamType, @Nonnull Drain drain) throws IOException {
        super("OutputMonitor:" + process.getId() + ":" + streamType);
        setDaemon(true);
        _stream = streamType == stdout ? process.getStdout() : process.getStderr();
        _drain = drain;
        start();
    }

    @Override
    public void run() {
        try {
            _lock.lockInterruptibly();
            try {
                try {
                    final byte[] buf = new byte[4096];
                    int read = _stream.read(buf);
                    while (!currentThread().isInterrupted() && read >= 0) {
                        _drain.drain(buf, 0, read);
                        read = _stream.read(buf);
                    }
                } catch (final InterruptedIOException ignored) {
                    currentThread().interrupt();
                } catch (final Exception e) {
                    // noinspection InstanceofCatchParameter
                    if (!(e instanceof IOException) || !"Stream closed".equals(e.getMessage())) {
                        LOG.warn("Could not read from " + _stream + ".", e);
                    }
                }
            } finally {
                try {
                    _alive = false;
                    _condition.signalAll();
                } finally {
                    _lock.unlock();
                }
            }
        } catch (final InterruptedException ignored) {
            currentThread().interrupt();
        } finally {
            closeQuietly(_stream);
        }
    }

    /**
     * @deprecated Use the {@link Drain#toString()} in the future.
     */
    @Nonnull
    @Deprecated
    public String getRecordedContent() {
        return _drain.toString();
    }

    public void waitFor() throws InterruptedException {
        _lock.lockInterruptibly();
        try {
            while (_alive) {
                // Prevent deadlock on some JVMs
                _condition.await(500, MILLISECONDS);
            }
        } finally {
            _lock.unlock();
        }
    }

    @Override
    public void close() {
        if (!TRUE.equals(_alreadyInClosing.get())) {
            _alreadyInClosing.set(TRUE);
            try {
                try {
                    closeQuietly(_stream, _drain);
                } finally {
                    ThreadUtils.stop(this);
                }
            } finally {
                _alreadyInClosing.remove();
            }
        }
    }
}

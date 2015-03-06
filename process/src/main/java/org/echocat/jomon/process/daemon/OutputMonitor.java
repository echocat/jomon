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

package org.echocat.jomon.process.daemon;

import org.echocat.jomon.process.GeneratedProcess;
import org.echocat.jomon.process.listeners.startup.StartupListener;
import org.echocat.jomon.process.listeners.stream.StreamListener;
import org.echocat.jomon.runtime.io.StreamType;
import org.echocat.jomon.runtime.util.ByteCount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;

public class OutputMonitor<E, ID, P extends GeneratedProcess<E, ID>, R extends ProcessDaemonRequirement<E, ID, P, ?>> implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(OutputMonitor.class);

    @Nonnull
    private final P _process;
    @Nonnull
    private final R _requirement;
    @Nonnull
    private final InputStream _stream;
    @Nonnull
    private final StreamType _streamType;
    @Nonnull
    private final ByteCount _sizeOfReadBuffer;

    public OutputMonitor(@Nonnull R requirement, @Nonnull P process, @Nonnull InputStream stream, @Nonnull StreamType streamType, @Nonnull ByteCount sizeOfReadBuffer) {
        _requirement = requirement;
        _process = process;
        _stream = stream;
        _streamType = streamType;
        _sizeOfReadBuffer = sizeOfReadBuffer;
    }

    @Override
    public void run() {
        final StreamListener<P> streamListener = _requirement.getStreamListener();
        final StartupListener<P> startupListener = _requirement.getStartupListener();
        try {
            final byte[] buffer = _sizeOfReadBuffer.allocate();
            int read = _stream.read(buffer);
            while (read >= 0) {
                startupListener.notifyOutput(_process, buffer, 0, read, _streamType);
                streamListener.notifyOutput(_process, buffer, 0, read, _streamType);
                read = _stream.read(buffer);
            }
        } catch (final InterruptedIOException ignored) {
            Thread.currentThread().interrupt();
        } catch (final Exception e) {
            // noinspection InstanceofCatchParameter
            if (!(e instanceof IOException) || !"Stream closed".equals(e.getMessage())) {
                LOG.warn("Could not read from " + _stream + ".", e);
            }
        }
    }

}

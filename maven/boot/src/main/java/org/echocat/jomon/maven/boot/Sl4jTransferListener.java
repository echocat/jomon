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

package org.echocat.jomon.maven.boot;

import org.echocat.jomon.runtime.util.ByteCount;
import org.echocat.jomon.runtime.util.Duration;
import org.slf4j.Logger;
import org.sonatype.aether.transfer.TransferCancelledException;
import org.sonatype.aether.transfer.TransferEvent;
import org.sonatype.aether.transfer.TransferListener;
import org.sonatype.aether.transfer.TransferResource;

import javax.annotation.Nonnull;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.sonatype.aether.transfer.TransferEvent.RequestType.PUT;

public class Sl4jTransferListener implements TransferListener {

    private final Logger _delegate;

    public Sl4jTransferListener(@Nonnull Logger delegate) {
        _delegate = delegate;
    }

    @Override
    public void transferInitiated(TransferEvent event) throws TransferCancelledException {
        final StringBuilder sb = new StringBuilder();
        final TransferResource resource = event.getResource();
        sb.append(event.getRequestType() == PUT ? "Uploading " : "Downloading ");
        sb.append(resource.getRepositoryUrl()).append(resource.getResourceName()).append("...");
        _delegate.info(sb.toString());
    }


    @Override
    public void transferCorrupted(TransferEvent event) throws TransferCancelledException {
        final StringBuilder sb = new StringBuilder();
        final TransferResource resource = event.getResource();
        sb.append("Failed to ");
        sb.append(event.getRequestType() == PUT ? "upload" : "download");
        sb.append(resource.getRepositoryUrl()).append(resource.getResourceName()).append(".");
        _delegate.error(sb.toString());
    }

    @Override
    public void transferSucceeded(TransferEvent event) {
        final TransferResource resource = event.getResource();
        final ByteCount contentLength = new ByteCount(event.getTransferredBytes());
        if (contentLength.hasContent()) {
            final StringBuilder sb = new StringBuilder();
            sb.append(event.getRequestType() == PUT ? "Uploaded " : "Downloaded ");
            sb.append(resource.getRepositoryUrl()).append(resource.getResourceName()).append(" (").append(contentLength);

            final Duration duration = new Duration(currentTimeMillis() - resource.getTransferStartTime());
            if (duration.hasContent()) {
                final ByteCount contentLengthPerSecond = contentLength.dividedBy(duration.in(SECONDS));
                sb.append(" at ").append(contentLengthPerSecond.toFormattedByteCount()).append("/s");
            }
            sb.append(").");
            _delegate.info(sb.toString());
        }
    }


    protected long toKB(long bytes) {
        return (bytes + 1023) / 1024;
    }

    @Override public void transferFailed(TransferEvent event) {}
    @Override public void transferProgressed(TransferEvent event) throws TransferCancelledException {}
    @Override public void transferStarted(TransferEvent event) throws TransferCancelledException {}
}

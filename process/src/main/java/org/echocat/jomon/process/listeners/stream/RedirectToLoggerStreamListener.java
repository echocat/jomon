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

package org.echocat.jomon.process.listeners.stream;

import org.echocat.jomon.process.GeneratedProcess;
import org.echocat.jomon.runtime.io.StreamType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Map;

import static org.echocat.jomon.runtime.io.StreamType.*;

public class RedirectToLoggerStreamListener<P extends GeneratedProcess<?, ?>> extends LineBasedAndStateEnabledStreamListenerSupport<P, RedirectToLoggerStreamListener<P>> {

    private static final Logger LOG = LoggerFactory.getLogger(RedirectToLoggerStreamListener.class);

    public RedirectToLoggerStreamListener() {
        whichFormatsMessagesWith("{message}");
    }

    @Override
    protected void write(@Nonnull P process, @Nonnull String content, @Nonnull StreamType streamType) {
        final String line = removeLastLineBreakFrom(content);
        if (streamType == stdout) {
            LOG.info(line);
        } else if (streamType == stderr) {
            LOG.error(line);
        } else if (streamType == system) {
            LOG.info(line);
        } else {
            throw new IllegalArgumentException("Could not handle streamType: " + streamType);
        }
    }

    @Nonnull
    protected String getDisplayFor(@Nonnull P reference) {
        final Object plain = reference.getId();
        return plain != null ? plain.toString() : "<unknown>";
    }

    public static class Provider extends StreamListenerProviderSupport {

        public Provider() {
            super(RedirectToLoggerStreamListener.class, "redirectToLogger");
        }

        @Nonnull
        @Override
        protected LineBasedStreamListenerSupport<?, ?> createInstanceBy(@Nonnull Class<? extends LineBasedStreamListenerSupport<?, ?>> type, @Nonnull Map<String, String> parameters) {
            return new RedirectToLoggerStreamListener();
        }

    }

}

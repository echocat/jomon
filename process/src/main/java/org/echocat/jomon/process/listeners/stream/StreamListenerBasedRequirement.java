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
import org.echocat.jomon.runtime.generation.Requirement;

import javax.annotation.Nonnull;

import static org.echocat.jomon.process.listeners.stream.StreamListeners.redirectToLogger;

public interface StreamListenerBasedRequirement<E, ID, P extends GeneratedProcess<E, ID>> extends Requirement {

    @Nonnull
    public StreamListener<P> getStreamListener();

    public abstract static class Base<E, ID, P extends GeneratedProcess<E, ID>, B extends Base<E, ID, P, B>> implements StreamListenerBasedRequirement<E, ID, P> {

        @Nonnull
        private StreamListener<P> _streamListener = redirectToLogger();

        @Nonnull
        public B withStreamListener(@Nonnull StreamListener<P> listener) {
            _streamListener = listener;
            return thisObject();
        }

        @Override
        @Nonnull
        public StreamListener<P> getStreamListener() {
            return _streamListener;
        }

        @Nonnull
        protected B thisObject() {
            //noinspection unchecked
            return (B) this;
        }
    }

}

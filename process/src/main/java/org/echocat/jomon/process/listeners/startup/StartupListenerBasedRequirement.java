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

package org.echocat.jomon.process.listeners.startup;

import org.echocat.jomon.process.GeneratedProcess;
import org.echocat.jomon.process.listeners.stream.StreamListenerBasedRequirement;

import javax.annotation.Nonnull;

import static org.echocat.jomon.process.listeners.startup.StartupListener.NoopStartupListener.noop;

public interface StartupListenerBasedRequirement<E, ID, P extends GeneratedProcess<E, ID>> extends StreamListenerBasedRequirement<E, ID, P> {

    @Nonnull
    public StartupListener<P> getStartupListener();

    public abstract static class Base<E, ID, P extends GeneratedProcess<E, ID>, B extends Base<E, ID, P, B>> extends StreamListenerBasedRequirement.Base<E, ID, P, B> implements StartupListenerBasedRequirement<E, ID, P> {

        @Nonnull
        private StartupListener<P> _startupListener = noop();

        @Nonnull
        public B withStartupListener(@Nonnull StartupListener<P> listener) {
            _startupListener = listener;
            return thisObject();
        }

        @Override
        @Nonnull
        public StartupListener<P> getStartupListener() {
            return _startupListener;
        }

    }

}

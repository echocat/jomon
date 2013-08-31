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

import org.echocat.jomon.process.daemon.listeners.startup.StartupListener;
import org.echocat.jomon.process.daemon.listeners.stream.StreamListener;
import org.echocat.jomon.process.daemon.listeners.stream.StreamListeners;
import org.echocat.jomon.runtime.generation.Requirement;

import javax.annotation.Nonnull;

import static org.echocat.jomon.process.daemon.listeners.startup.StartupListener.noop;

public interface ApplicationDaemonRequirement<T extends ApplicationDaemon<?>> extends Requirement {

    @Nonnull
    public Class<T> getType();

    @Nonnull
    public StartupListener getStartupListener();

    @Nonnull
    public StreamListener getStreamListener();

    public abstract static class Base<B extends Base<B, T>, T extends ApplicationDaemon<?>> implements ApplicationDaemonRequirement<T> {

        @Nonnull
        private final Class<T> _type;
        @Nonnull
        private StartupListener _startupListener = noop;
        @Nonnull
        private StreamListener _streamListener = StreamListeners.redirectToLogger;

        public Base(@Nonnull Class<T> type) {
            _type = type;
        }

        @Nonnull
        public B withStartupListener(@Nonnull StartupListener listener) {
            _startupListener = listener;
            return thisRequirement();
        }

        @Nonnull
        public B withStreamListener(@Nonnull StreamListener listener) {
            _streamListener = listener;
            return thisRequirement();
        }

        @Nonnull
        @Override
        public Class<T> getType() {
            return _type;
        }

        @Override
        @Nonnull
        public StartupListener getStartupListener() {
            return _startupListener;
        }

        @Override
        @Nonnull
        public StreamListener getStreamListener() {
            return _streamListener;
        }

        @Nonnull
        protected B thisRequirement() {
            //noinspection unchecked
            return (B) this;
        }
    }


    public static class Impl extends Base<Impl, ApplicationDaemon<?>> {

        @Nonnull
        public static Impl applicationDaemonOfType(@Nonnull Class<? extends ApplicationDaemon<?>> type) {
            // noinspection unchecked
            return new Impl((Class<ApplicationDaemon<?>>) type);
        }

        public Impl(@Nonnull Class<ApplicationDaemon<?>> type) {
            super(type);
        }

    }

}

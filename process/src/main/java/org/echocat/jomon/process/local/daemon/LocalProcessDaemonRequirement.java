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

package org.echocat.jomon.process.local.daemon;

import org.echocat.jomon.process.daemon.ProcessDaemonRequirement;
import org.echocat.jomon.process.local.LocalGeneratedProcess;

import javax.annotation.Nonnull;
import java.io.File;

public interface LocalProcessDaemonRequirement<T extends LocalProcessDaemon<?>> extends ProcessDaemonRequirement<File, Long, LocalGeneratedProcess, T> {

    public static class Base<D extends LocalProcessDaemon<?>, T extends Base<D, T>> extends ProcessDaemonRequirement.Base<File, Long, LocalGeneratedProcess, D, T> implements LocalProcessDaemonRequirement<D> {

        public Base(@Nonnull Class<D> type) {
            super(type);
        }

    }

    public static class Impl extends Base<LocalProcessDaemon<?>, Impl> {

        @Nonnull
        public static Impl localProcessDaemonOfType(@Nonnull Class<? extends LocalProcessDaemon<?>> type) {
            // noinspection unchecked
            return new Impl((Class<LocalProcessDaemon<?>>) type);
        }

        public Impl(@Nonnull Class<LocalProcessDaemon<?>> type) {
            super(type);
        }

    }

}

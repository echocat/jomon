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

import org.echocat.jomon.process.daemon.BaseProcessDaemonQuery;

import javax.annotation.Nonnull;
import java.io.File;

public class LocalProcessDaemonQuery extends BaseProcessDaemonQuery<File, Long, LocalProcessDaemonQuery, LocalProcessDaemon<?>> {

    @Nonnull
    public static LocalProcessDaemonQuery processDaemon() {
        return new LocalProcessDaemonQuery();
    }

}

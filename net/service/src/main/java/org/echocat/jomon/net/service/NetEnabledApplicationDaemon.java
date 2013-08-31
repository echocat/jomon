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

package org.echocat.jomon.net.service;

import org.echocat.jomon.net.FreeTcpPortDetector;
import org.echocat.jomon.process.ProcessRepository;
import org.echocat.jomon.process.daemon.ApplicationDaemon;
import org.echocat.jomon.process.daemon.ApplicationDaemonRequirement;
import org.echocat.jomon.process.daemon.CouldNotStartProcessException;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.InetAddress;
import java.net.UnknownHostException;

public abstract class NetEnabledApplicationDaemon<R extends ApplicationDaemonRequirement<?>> extends ApplicationDaemon<R> {

    protected NetEnabledApplicationDaemon(@Nullable R requirement) throws CouldNotStartProcessException {
        super(requirement);
    }

    protected NetEnabledApplicationDaemon(@Nonnull ProcessRepository processRepository, @Nullable R requirement) throws CouldNotStartProcessException {
        super(processRepository, requirement);
    }

    @Nonnegative
    protected int findFreePort() throws UnknownHostException {
        final InetAddress localhost = InetAddress.getByName("localhost");
        final FreeTcpPortDetector freeTcpPortDetector = new FreeTcpPortDetector(localhost, 10000, 45000);
        return freeTcpPortDetector.detect();
    }

}

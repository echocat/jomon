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

package org.echocat.jomon.net.ssh;

import javax.annotation.Nonnull;

import static java.util.ServiceLoader.load;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietlyIfAutoCloseable;

public abstract class Ssh {

    private static final Ssh INSTANCE = createInstance();

    @Nonnull
    public static Ssh getInstance() {
        return INSTANCE;
    }

    @Nonnull
    public static Ssh ssh() {
        return getInstance();
    }

    @Nonnull
    public static SshSessionGenerator sessionGenerator() {
        return ssh().getSessionGenerator();
    }

    @Nonnull
    public static SshProcessRepository processRepository() {
        return ssh().getProcessRepository();
    }

    @Nonnull
    public abstract SshSessionGenerator getSessionGenerator();

    @Nonnull
    public abstract SshProcessRepository getProcessRepository();

    public abstract boolean isAvailable();

    @Nonnull
    protected static Ssh createInstance() {
        Ssh processRepository = null;
        for (Ssh candidate : load(Ssh.class)) {
            if (candidate.isAvailable()) {
                processRepository = candidate;
                break;
            }
        }
        if (processRepository == null) {
            throw new IllegalStateException("Could not find any matching implementation of " + Ssh.class.getName() + " for this virtual machine.");
        }
        closeOnJvmShutdown(processRepository);
        return processRepository;
    }

    private static void closeOnJvmShutdown(@Nonnull final Ssh processRepository) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                closeQuietlyIfAutoCloseable(processRepository);
            }
        });
    }

}

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

package org.echocat.jomon.process.unix;

import org.echocat.jomon.process.ProcessPidResolver;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

import static com.sun.jna.Platform.*;

public class UnixProcessPidResolver extends ProcessPidResolver {

    protected static final Class<?> PROCESS_CLASS = findClass("java.lang.UNIXProcess");
    protected static final Field PROCESS_PID_FIELD = findFieldOf("pid", int.class, PROCESS_CLASS);

    @Override
    public long resolvePidOf(@Nonnull Process process) {
        final int pid;
        if (PROCESS_CLASS != null && PROCESS_PID_FIELD != null && PROCESS_CLASS.isInstance(process)) {
            pid = (Integer) getFieldValue(PROCESS_PID_FIELD, process);
        } else {
            throw new IllegalArgumentException("Could not handle: " + process);
        }
        return pid;
    }

    @Override
    protected boolean couldHandleThisVirtualMachine() {
        final boolean result;
        if (isFreeBSD() || isLinux() || isOpenBSD() || isSolaris() || isMac()) {
            result = PROCESS_PID_FIELD != null;
        } else {
            result = false;
        }
        return result;
    }

    @Override
    protected void init() {}



}

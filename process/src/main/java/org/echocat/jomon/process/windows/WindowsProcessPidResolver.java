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

package org.echocat.jomon.process.windows;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import org.echocat.jomon.process.local.ProcessPidResolver;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

import static com.sun.jna.Native.loadLibrary;
import static com.sun.jna.Platform.isWindows;
import static com.sun.jna.win32.W32APIOptions.DEFAULT_OPTIONS;

public class WindowsProcessPidResolver extends ProcessPidResolver {

    protected static final Class<?> WIN32_PROCESS_CLASS = findClass("java.lang.Win32Process");
    protected static final Class<?> PROCESS_IMPL_CLASS = findClass("java.lang.ProcessImpl");
    protected static final Field WIN32_PROCESS_HANDLE_FIELD = findFieldOf("handle", long.class, WIN32_PROCESS_CLASS);
    protected static final Field PROCESS_IMPL_HANDLE_FIELD = findFieldOf("handle", long.class, PROCESS_IMPL_CLASS);

    private WinNT _winNt;

    @Override
    public long resolvePidOf(@Nonnull Process process) {
        final long handleId;
        if (WIN32_PROCESS_CLASS != null && WIN32_PROCESS_HANDLE_FIELD != null && WIN32_PROCESS_CLASS.isInstance(process)) {
            handleId = (Long) getFieldValue(WIN32_PROCESS_HANDLE_FIELD, process);
        } else if (PROCESS_IMPL_CLASS != null && PROCESS_IMPL_HANDLE_FIELD != null && PROCESS_IMPL_CLASS.isInstance(process)) {
            handleId = (Long) getFieldValue(PROCESS_IMPL_HANDLE_FIELD, process);
        } else {
            throw new IllegalArgumentException("Could not handle: " + process);
        }
        return _winNt.GetProcessId(toHandle(handleId));
    }

    @Nonnull
    protected HANDLE toHandle(long handleId) {
        final HANDLE handle = new HANDLE();
        handle.setPointer(Pointer.createConstant(handleId));
        return handle;
    }

    @Override
    protected boolean couldHandleThisVirtualMachine() {
        final boolean result;
        if (isWindows()) {
            result = WIN32_PROCESS_HANDLE_FIELD != null || PROCESS_IMPL_HANDLE_FIELD != null;
        } else {
            result = false;
        }
        return result;
    }

    @Override
    protected void init() {
        _winNt = (WinNT) loadLibrary(WinNT.class, DEFAULT_OPTIONS);
    }



}

/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.process.sigar;

import org.hyperic.sigar.ProcExe;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.io.File;

@ThreadSafe
public class SigarProcess implements org.echocat.jomon.process.Process {

    private final long _pid;
    private final Sigar _sigar;

    private volatile boolean _execResolved;
    private volatile boolean _argsResolved;

    private volatile File _executable;
    private String[] _commandLine;

    public SigarProcess(long pid, @Nonnull Sigar sigar) {
        _pid = pid;
        _sigar = sigar;
    }

    @Override
    public long getId() {
        return _pid;
    }

    @Override
    @Nullable
    public File getExecutable() {
        resolveExecIfNeeded();
        return _executable;
    }

    @Override
    @Nullable
    public String[] getCommandLine() {
        resolveArgsIfNeeded();
        return _commandLine;
    }

    @Override
    public boolean isPathCaseSensitive() {
        return false;
    }

    protected void resolveExecIfNeeded() {
        if (!_execResolved) {
            try {
                final ProcExe procExe = _sigar.getProcExe(_pid);
                final String name = procExe.getName();
                final File file = new File(name);
                if (file.isFile()) {
                    _executable = file;
                }
            } catch (SigarException ignored) {}
            _execResolved = true;
        }
    }

    protected void resolveArgsIfNeeded() {
        if (!_argsResolved) {
            try {
                _commandLine = _sigar.getProcArgs(_pid);
            } catch (SigarException ignored) {}
            _argsResolved = true;
        }
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else if (!(o instanceof SigarProcess)) {
            result = false;
        } else {
            final SigarProcess that = (SigarProcess) o;
            result = _pid == that._pid;
        }
        return result;
    }

    @Override
    public int hashCode() {
        return (int) (_pid ^ (_pid >>> 32));
    }

    @Override
    public String toString() {
        return "Process #" + _pid;
    }
}

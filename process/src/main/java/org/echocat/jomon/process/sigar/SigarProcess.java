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

package org.echocat.jomon.process.sigar;

import org.echocat.jomon.process.local.LocalProcess;
import org.hyperic.sigar.ProcExe;
import org.hyperic.sigar.SigarException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.io.File;
import java.util.List;

import static org.echocat.jomon.runtime.CollectionUtils.asImmutableList;

@ThreadSafe
public class SigarProcess implements LocalProcess {

    private final long _pid;
    private final SigarFacade _sigar;

    private volatile boolean _execResolved;
    private volatile boolean _argsResolved;

    private volatile File _executable;
    private List<String> _commandLine;

    public SigarProcess(long pid, @Nonnull SigarFacade sigar) {
        _pid = pid;
        _sigar = sigar;
    }

    @Override
    public Long getId() {
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
    public List<String> getArguments() {
        resolveArgsIfNeeded();
        return _commandLine;
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
            } catch (final SigarException ignored) {}
            _execResolved = true;
        }
    }

    protected void resolveArgsIfNeeded() {
        if (!_argsResolved) {
            try {
                _commandLine = asImmutableList(_sigar.getProcArgs(_pid));
            } catch (final SigarException ignored) {}
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

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

package org.echocat.jomon.process.local;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class LocalGeneratedProcessImpl implements LocalGeneratedProcess {

    private final LocalProcess _placeHolder;
    private final Process _original;
    private final boolean _isDaemon;

    public LocalGeneratedProcessImpl(@Nonnull LocalProcess placeHolder, @Nonnull Process original, boolean isDaemon) {
        _placeHolder = placeHolder;
        _original = original;
        _isDaemon = isDaemon;
    }

    @Override
    public Long getId() {
        return _placeHolder.getId();
    }

    @Override
    @Nullable
    public File getExecutable() {
        return _placeHolder.getExecutable();
    }

    @Override
    @Nullable
    public List<String> getArguments() {
        return _placeHolder.getArguments();
    }

    @Nonnull
    @Override
    public OutputStream getStdin() {
        return _original.getOutputStream();
    }

    @Nonnull
    @Override
    public InputStream getStdout() {
        return _original.getInputStream();
    }

    @Nonnull
    @Override
    public InputStream getStderr() {
        return _original.getErrorStream();
    }

    @Override
    public int waitFor() throws InterruptedException {
        return _original.waitFor();
    }

    @Override
    public int exitValue() {
        try {
            return _original.exitValue();
        } catch (IllegalThreadStateException e) {
            throw new IllegalStateException("Process is still running.", e);
        }
    }

    @Override
    public boolean isAlive() {
        boolean result;
        try {
            result = _original.exitValue() >= 0;
        } catch (IllegalThreadStateException ignored) {
            result = false;
        }
        return result;
    }

    @Override
    public void close() {
        _original.destroy();
    }

    @Override
    public void kill() throws IOException {
        _original.destroy();
    }

    @Override
    public boolean isDaemon() {
        return _isDaemon;
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else if (!(o instanceof LocalGeneratedProcess)) {
            result = false;
        } else {
            final LocalGeneratedProcess that = (LocalGeneratedProcess) o;
            final Long id = getId();
            result = id != null ? id.equals(that.getId()) : that.getId() == null;
        }
        return result;
    }

    @Override
    public int hashCode() {
        final Long id = getId();
        return id != null ? (int) (id ^ (id >>> 32)) : 0;
    }

    @Override
    public String toString() {
        return "Process #" + getId();
    }
}

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

package org.echocat.jomon.process;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

@SuppressWarnings("UnnecessaryFullyQualifiedName")
public class GeneratedProcessImpl implements GeneratedProcess {

    private final Process _placeHolder;
    private final java.lang.Process _original;
    private final boolean _isDaemon;

    public GeneratedProcessImpl(@Nonnull Process placeHolder, @Nonnull java.lang.Process original, boolean isDaemon) {
        _placeHolder = placeHolder;
        _original = original;
        _isDaemon = isDaemon;
    }

    @Override
    public long getId() {
        return _placeHolder.getId();
    }

    @Override
    @Nullable
    public File getExecutable() {
        return _placeHolder.getExecutable();
    }

    @Override
    @Nullable
    public String[] getCommandLine() {
        return _placeHolder.getCommandLine();
    }

    @Override
    public boolean isPathCaseSensitive() {
        return _placeHolder.isPathCaseSensitive();
    }

    @Nonnull
    @Override
    public OutputStream getOutputStream() {
        return _original.getOutputStream();
    }

    @Nonnull
    @Override
    public InputStream getInputStream() {
        return _original.getInputStream();
    }

    @Nonnull
    @Override
    public InputStream getErrorStream() {
        return _original.getErrorStream();
    }

    @Override
    public int waitFor() throws InterruptedException {
        return _original.waitFor();
    }

    @Override
    public int exitValue() {
        return _original.exitValue();
    }

    @Override
    public void shutdown() {
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
        } else if (!(o instanceof GeneratedProcess)) {
            result = false;
        } else {
            final GeneratedProcess that = (GeneratedProcess) o;
            result = getId() == that.getId();
        }
        return result;
    }

    @Override
    public int hashCode() {
        final long id = getId();
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return "Process #" + getId();
    }
}

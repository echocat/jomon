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

package org.echocat.jomon.net.ssh.jsch;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.NotifyingIO.EventConsumer;
import org.echocat.jomon.net.ssh.SshGeneratedProcess;
import org.echocat.jomon.net.ssh.SshGeneratedProcessRequirement;
import org.echocat.jomon.net.ssh.SshProcessUtils;
import org.echocat.jomon.process.Pty;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.echocat.jomon.net.ssh.jsch.JschUtils.register;
import static org.echocat.jomon.runtime.util.Duration.sleep;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

public class JschSshGeneratedProcess implements SshGeneratedProcess {

    @Nonnull
    private final Lock _lock = new ReentrantLock();
    @Nonnull
    private final Condition _condition = _lock.newCondition();

    private boolean _closed;

    @Nonnull
    private final SshGeneratedProcessRequirement _requirement;
    @Nonnull
    private final ChannelExec _exec;
    private final boolean _closeContext;

    @Nonnull
    private final Id _id;

    public JschSshGeneratedProcess(@Nonnull SshGeneratedProcessRequirement requirement, @Nonnull ChannelExec exec, @Nonnull Id id, boolean closeContext) throws IOException {
        _requirement = requirement;
        _exec = exec;
        _closeContext = closeContext;
        _id = id;
        register(new EventConsumerImpl(), exec);
    }

    @Nonnull
    @Override
    public String getExecutable() {
        return _requirement.getExecutable();
    }

    @Nonnull
    @Override
    public List<String> getArguments() {
        return _requirement.getArguments();
    }

    @Override
    @Nonnegative
    public int waitFor() throws InterruptedException, IllegalStateException {
        _lock.lockInterruptibly();
        try {
            if (!_closed) {
                while (_exec.isConnected()) {
                    _condition.await(100, MILLISECONDS);
                }
            }
        } finally {
            _lock.unlock();
        }
        while (_exec.isConnected()) {
            _exec.disconnect();
            sleep("10ms");
        }
        return exitValue();
    }

    @Override
    public int exitValue() throws IllegalStateException {
        if (_exec.isConnected()) {
            throw new IllegalStateException();
        }
        return _exec.getExitStatus();
    }

    @Override
    public boolean isAlive() {
        return _exec.isConnected();
    }

    @Nullable
    @Override
    public Pty getPty() {
        return _requirement.getPty();
    }

    @Override
    public boolean isDaemon() {
        return _requirement.isDaemon();
    }

    @Nonnull
    @Override
    public Id getId() {
        return _id;
    }

    @Nonnull
    @Override
    public OutputStream getStdin() throws IOException {
        return _exec.getOutputStream();
    }

    @Nonnull
    @Override
    public InputStream getStdout() throws IOException {
        return _exec.getInputStream();
    }

    @Nonnull
    @Override
    public InputStream getStderr() throws IOException {
        return _exec.getErrStream();
    }

    @Override
    public void close() throws IOException {
        try {
            if (_requirement.getPty() != null) {
                final OutputStream out = _exec.getOutputStream();
                out.write(3);
                out.flush();
                waitFor();
            }
        } catch (Exception ignored) {
        } finally {
            kill();
        }
    }

    @Override
    public void kill() throws IOException {
        try {
            try {
                _exec.disconnect();
            } catch (Exception ignored) {
            }
        } finally {
            if (_closeContext) {
                closeQuietly(_requirement.getContext());
            }
        }
    }

    @Override
    public String toString() {
        return SshProcessUtils.getCompleteCommandLineArguments(getExecutable(), getArguments());
    }

    protected class EventConsumerImpl implements EventConsumer {

        @Override
        public void onClose() {
            _lock.lock();
            try {
                if (!_closed) {
                    _closed = true;
                    _condition.signalAll();
                }
            } finally {
                _lock.unlock();
            }
        }

    }

}

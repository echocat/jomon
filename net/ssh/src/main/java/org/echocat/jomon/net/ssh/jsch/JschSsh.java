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

import org.echocat.jomon.net.ssh.Ssh;
import org.echocat.jomon.net.ssh.SshProcessRepository;
import org.echocat.jomon.net.ssh.SshSessionGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.echocat.jomon.runtime.exceptions.ExceptionUtils.containsClassNotFoundException;

public class JschSsh extends Ssh {

    private static final Logger LOG = LoggerFactory.getLogger(JschSsh.class);

    @Nullable
    private final JschSshSessionGenerator _sessionGenerator;
    @Nullable
    private final JschSshProcessRepository _processRepository;

    public JschSsh(@Nonnull JschSshSessionGenerator sessionGenerator, @Nonnull JschSshProcessRepository processRepository) {
        _sessionGenerator = sessionGenerator;
        _processRepository = processRepository;
    }

    public JschSsh() {
        _sessionGenerator = tryCreateSessionGenerator();
        _processRepository = _sessionGenerator != null ? tryCreateProcessGenerator(_sessionGenerator) : null;
    }

    @Nullable
    protected JschSshSessionGenerator tryCreateSessionGenerator() {
        JschSshSessionGenerator result;
        try {
            result = new JschSshSessionGenerator();
        } catch (Exception e) {
            result = null;
            checkException(e, SshSessionGenerator.class);
        }
        return result;
    }

    @Nullable
    protected JschSshProcessRepository tryCreateProcessGenerator(@Nonnull JschSshSessionGenerator sessionGenerator) {
        JschSshProcessRepository result;
        try {
            result = new JschSshProcessRepository(sessionGenerator);
        } catch (Exception e) {
            result = null;
            checkException(e, SshProcessRepository.class);
        }
        return result;
    }

    protected void checkException(@Nonnull Exception e, @Nonnull Class<?> resultType) {
        if (containsClassNotFoundException(e, "com.jcraft.jsch.")) {
            LOG.info("The JSch implementation of " + resultType.getSimpleName() + " is not available. Could not find the JSch implementation in the classpath present. If you use Maven add the org.fusesource:sigar:1.6.4+ dependency. You can ignore this message if you do not want to use the JSch SSH implementation.");
        } else {
            throw new RuntimeException("Could not create a " + resultType.getSimpleName() + ".", e);
        }
    }

    @Nonnull
    @Override
    public SshSessionGenerator getSessionGenerator() {
        return returnCheckedInstance(_sessionGenerator, SshSessionGenerator.class);
    }

    @Nonnull
    @Override
    public SshProcessRepository getProcessRepository() {
        return returnCheckedInstance(_processRepository, SshProcessRepository.class);
    }

    @Nonnull
    protected <T> T returnCheckedInstance(@Nullable T instance, @Nonnull Class<T> type) {
        if (instance == null) {
            throw new UnsupportedOperationException("This implemenation of " + Ssh.class.getName() + " is not able to provide a " + type.getSimpleName() + " because there is no JSch available for this JVM. See previous message for more information.");
        }
        return instance;
    }

    @Override
    public boolean isAvailable() { return _sessionGenerator != null; }

}

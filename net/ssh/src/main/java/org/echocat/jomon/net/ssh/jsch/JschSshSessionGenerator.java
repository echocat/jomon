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

import com.jcraft.jsch.IdentityRepository;
import com.jcraft.jsch.agentproxy.AgentProxyException;
import com.jcraft.jsch.agentproxy.Connector;
import com.jcraft.jsch.agentproxy.RemoteIdentityRepository;
import org.echocat.jomon.net.ssh.SshSessionRequirement;
import org.echocat.jomon.net.ssh.SshSystemException;

import javax.annotation.Nonnull;

import static com.jcraft.jsch.agentproxy.ConnectorFactory.getDefault;

public class JschSshSessionGenerator extends JschSshSessionGeneratorSupport {

    @Nonnull
    @Override
    protected IdentityRepository createIdentityRepository(@Nonnull SshSessionRequirement requirement) {
        final Connector connector = createConnection();
        return new RemoteIdentityRepository(connector);
    }

    @Nonnull
    protected Connector createConnection() throws SshSystemException {
        try {
            return getDefault().createConnector();
        } catch (final AgentProxyException e) {
            throw new SshSystemException("Could not create connector for ssh session.", e);
        }
    }

}

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

package org.echocat.jomon.net.http.client;

import org.apache.http.conn.ClientConnectionManager;
import org.springframework.beans.factory.FactoryBean;

public class ClientConnectionManagerFactoryBean extends ClientConnectionManagerFactory implements FactoryBean<ClientConnectionManager> {

    @Override
    public ClientConnectionManager getObject() throws Exception {
        return create();
    }

    @Override
    public Class<?> getObjectType() {
        return ClientConnectionManager.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}

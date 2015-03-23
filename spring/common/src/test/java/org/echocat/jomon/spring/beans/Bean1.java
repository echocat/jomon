/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012-2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.spring.beans;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

public class Bean1 implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(Bean1.class);

    private String _aString;

    public String getAString() {
        return _aString;
    }

    public void setAString(String aString) {
        _aString = aString;
    }

    @PostConstruct
    public void init() throws Exception {
        LOG.info("Hi!");
    }

    @Override
    public void close() throws Exception {
        LOG.info("Bye!");
    }
}

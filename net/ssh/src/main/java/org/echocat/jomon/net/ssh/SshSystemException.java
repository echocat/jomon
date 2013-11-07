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

package org.echocat.jomon.net.ssh;

public class SshSystemException extends SshException {

    public SshSystemException() {}

    public SshSystemException(String message) {
        super(message);
    }

    public SshSystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public SshSystemException(Throwable cause) {
        super(cause);
    }

}
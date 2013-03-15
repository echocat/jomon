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

package org.echocat.jomon.net.mail;

import org.echocat.jomon.net.Sender;

import javax.annotation.Nonnull;

public interface MailSender extends Sender<Mail> {

    @Override
    public void send(@Nonnull Mail mail) throws MailSenderException;

    public static class MailSenderException extends RuntimeException {

        public MailSenderException() {}

        public MailSenderException(String message) {
            super(message);
        }

        public MailSenderException(String message, Throwable cause) {
            super(message, cause);
        }

        public MailSenderException(Throwable cause) {
            super(cause);
        }
    }

}

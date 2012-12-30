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

package org.echocat.jomon.runtime.mail;

import org.echocat.jomon.runtime.ImmutableMimeType;
import org.echocat.jomon.runtime.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Properties;

import static javax.mail.Message.RecipientType.*;

public class SmtpSender implements MailSender {

    private InetSocketAddress _server;
    private Authentication _authentication;
    private String _defaultSenderAddress;
    private String _defaultPersonal;

    public void setServer(@Nonnull InetSocketAddress server) {
        _server = server;
    }

    public void setServerUri(@Nonnull URI server) {
        if (!"smtp".equals(server.getScheme())) {
            throw new IllegalArgumentException("Illegal scheme of: " + server);
        }
        final String host = server.getHost();
        final int port = server.getPort();
        _server = new InetSocketAddress(host, port >= 0 ? port : 25);
    }

    public void setAuthentication(@Nonnull Authentication authentication) {
        _authentication = authentication;
    }

    public void setUsernameAndPassword(@Nonnull String authenticationString) {
        final String[] args = StringUtils.split(authenticationString, ":", 2);
        if (args.length != 2) {
            throw new IllegalArgumentException("<username>:<password> required but got: " + authenticationString);
        }
        _authentication = new Authentication(args[0], args[1]);
    }

    public void setDefaultSenderAddress(@Nonnull String defaultSenderAddress) {
        _defaultSenderAddress = defaultSenderAddress;
    }

    public void setDefaultPersonal(@Nonnull String defaultPersonal) {
        _defaultPersonal = defaultPersonal;
    }

    @Override
    public void send(@Nonnull Mail mail) throws MailSenderException {
        checkMail(mail);
        final Session session = Session.getDefaultInstance(createProperties(), _authentication);
        final Message message = toMessage(mail, session);
        try {
            Transport.send(message);
        } catch (MessagingException e) {
            throw new MailSenderException("Could not send message, please check contained exception for details.", e);
        }
    }

    private void checkMail(@Nonnull Mail mail) {
        if (!mail.hasSender()) {
            if (_defaultSenderAddress == null) {
                throw new IllegalArgumentException("This sender requires a sender's address.");
            } else {
                mail.withSender(_defaultSenderAddress, _defaultPersonal);
            }
        }
    }

    @Nonnull
    private Properties createProperties() {
        final Properties properties = new Properties();
        properties.setProperty("mail.smtp.host", _server.getHostName());
        properties.setProperty("mail.smtp.port", Integer.toString(_server.getPort()));
        properties.setProperty("mail.smtp.auth", Boolean.toString(_authentication != null));
        return properties;
    }

    @Nonnull
    private Message toMessage(@Nonnull Mail mail, @Nonnull Session session) {
        final Message message = new MimeMessage(session);
        final String body = getBodyFrom(mail);
        try {
            message.setFrom(mail.getSender());
            setRecipients(mail, message);
            message.setSubject(mail.getSubject());
            message.setText(body);
        } catch (MessagingException e) {
            throw new MailSenderException("Could not send message, see enclosed exception.", e);
        }
        return message;
    }

    @Nonnull
    private String getBodyFrom(@Nonnull Mail mail) {
        String body = mail.getBody().get(ImmutableMimeType.TEXT_PLAIN);
        if (body == null) {
            body = mail.getBody().get(ImmutableMimeType.TEXT_HTML);
            if (body == null) {
                body = mail.getBody().get(ImmutableMimeType.APPLICATION_OCTET_STREAM);
            }
        }
        if (body == null) {
            body = "";
        }
        return body;
    }

    private void setRecipients(@Nonnull Mail mail, @Nonnull Message message) throws MessagingException {
        message.setRecipients(TO, convertToAddressArray(mail.getRecipients().get(TO)));
        message.setRecipients(CC, convertToAddressArray(mail.getRecipients().get(CC)));
        message.setRecipients(BCC, convertToAddressArray(mail.getRecipients().get(BCC)));
    }

    @Nullable
    private Address[] convertToAddressArray(@Nullable List<InternetAddress> addresses) {
        final Address [] addressArray;
        if (addresses == null) {
            addressArray = null;
        } else {
            addressArray = new Address [addresses.size()];
            for (int i = 0; i < addressArray.length; i++) {
                addressArray[i] = addresses.get(i);
            }
        }
        return addressArray;
    }

    public class Authentication extends Authenticator {

        private final String _user;
        private final String _password;

        public Authentication(@Nonnull String user, @Nonnull String password) {
            _user = user;
            _password = password;
        }

        @Override
        @Nonnull
        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(_user, _password);
        }
    }
}

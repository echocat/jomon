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

package org.echocat.jomon.net.mail;

import org.echocat.jomon.runtime.ImmutableMimeType;
import org.echocat.jomon.runtime.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.mail.Message.RecipientType;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static java.util.Collections.*;
import static javax.mail.Message.RecipientType.*;
import static org.echocat.jomon.runtime.ImmutableMimeType.TEXT_HTML;
import static org.echocat.jomon.runtime.ImmutableMimeType.TEXT_PLAIN;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;

public class Mail {

    @Nonnull
    public static Mail mail() {
        return new Mail();
    }

    private InternetAddress _sender;
    private final Map<RecipientType, List<InternetAddress>> _recipients = new HashMap<>();

    private final Map<ImmutableMimeType, String> _body = new HashMap<>();
    private String _subject;

    @Nonnull
    public Mail withRecipients(@Nonnull RecipientType type, @Nullable InternetAddress... recipients) {
        if (!isEmpty(recipients)) {
            List<InternetAddress> addresses = _recipients.get(type);
            if (addresses == null) {
                addresses = new ArrayList<>();
                _recipients.put(type, addresses);
            }
            addAll(addresses, recipients);
        }
        return thisInstance();
    }

    @Nonnull
    public Mail withRecipients(@Nullable InternetAddress... recipients) {
        return withRecipients(TO, recipients);
    }

    @Nonnull
    public Mail withCcs(@Nullable InternetAddress... recipients) {
        return withRecipients(CC, recipients);
    }

    @Nonnull
    public Mail withBccs(@Nullable InternetAddress... recipients) {
        return withRecipients(BCC, recipients);
    }

    @Nonnull
    public Mail withRecipients(@Nonnull RecipientType type, @Nullable String... recipients) {
        if (!isEmpty(recipients)) {
            final List<InternetAddress> recipientsAsAddress = new ArrayList<>(recipients.length);
            for (int i = 0; i < recipients.length; i++) {
                try {
                    addAll(recipientsAsAddress, InternetAddress.parse(recipients[i], true));
                } catch (AddressException e) {
                    throw new IllegalArgumentException("Illegal recipient: " + recipients[i], e);
                }
            }
            withRecipients(type, recipientsAsAddress.toArray(new InternetAddress[recipientsAsAddress.size()]));
        }
        return thisInstance();
    }

    @Nonnull
    public Mail withRecipients(@Nullable String... recipients) {
        return withRecipients(TO, recipients);
    }

    @Nonnull
    public Mail withCcs(@Nullable String... recipients) {
        return withRecipients(CC, recipients);
    }

    @Nonnull
    public Mail withBccs(@Nullable String... recipients) {
        return withRecipients(BCC, recipients);
    }

    @Nonnull
    public Mail withRecipient(@Nonnull RecipientType type, @Nonnull String address, @Nullable String personal) {
        return withRecipients(type, toInternetAddress(address, personal));
    }

    @Nonnull
    public Mail withRecipient(@Nonnull String address, @Nullable String personal) {
        return withRecipient(TO, address, personal);
    }

    @Nonnull
    public Mail withRecipient(@Nonnull InternetAddress internetAddress) {
        return withRecipient(TO, internetAddress.getAddress(), internetAddress.getPersonal());
    }

    @Nonnull
    public Mail withCc(@Nonnull String address, @Nullable String personal) {
        return withRecipient(CC, address, personal);
    }

    @Nonnull
    public Mail withBcc(@Nonnull String address, @Nullable String personal) {
        return withRecipient(BCC, address, personal);
    }

    @Nonnull
    public Mail withBody(@Nonnull ImmutableMimeType type, @Nonnull String body) {
        _body.put(type, body);
        return thisInstance();
    }

    @Nonnull
    public Mail withTextBody(@Nonnull String textBody) {
        return withBody(TEXT_PLAIN, textBody);
    }

    @Nonnull
    public Mail withHtmlBody(@Nonnull String textBody) {
        return withBody(TEXT_HTML, textBody);
    }

    @Nonnull
    public Mail withSubject(@Nonnull String subject) {
        _subject = subject;
        return thisInstance();
    }

    @Nonnull
    public Mail withSender(@Nonnull InternetAddress sender) {
        _sender = sender;
        return thisInstance();
    }

    @Nonnull
    public Mail withSender(@Nonnull String address, @Nullable String personal) {
        return withSender(toInternetAddress(address, personal));
    }

    @Nonnull
    protected InternetAddress toInternetAddress(@Nonnull String address, @Nullable String personal) {
        try {
            return personal != null ? new InternetAddress(address, personal) : new InternetAddress(address, true);
        } catch (UnsupportedEncodingException | AddressException e) {
            throw new IllegalArgumentException("Could not parse: " + address, e);
        }
    }

    @Nonnull
    public Mail withSender(@Nonnull String address) {
        return withSender(address, null);
    }

    @Nullable
    public InternetAddress getSender() {
        return _sender;
    }

    public boolean hasSender() {
        return getSender() != null;
    }

    @Nonnull
    public Map<RecipientType, List<InternetAddress>> getRecipients() {
        return unmodifiableMap(_recipients);
    }

    @Nonnull
    public List<InternetAddress> getRecipients(@Nonnull RecipientType type) {
        final List<InternetAddress> addresses = _recipients.get(type);
        return addresses != null ? unmodifiableList(addresses) : Collections.<InternetAddress>emptyList();
    }

    @Nonnull
    public List<InternetAddress> getAllRecipients() {
        final List<InternetAddress> addresses = new ArrayList<>();
        for (List<InternetAddress> that : _recipients.values()) {
            addresses.addAll(that);
        }
        return unmodifiableList(addresses);
    }

    public boolean hasRecipients() {
        boolean result = false;
        for (List<InternetAddress> that : _recipients.values()) {
            if (!that.isEmpty()) {
                result = true;
                break;
            }
        }
        return result;
    }

    @Nonnull
    public List<InternetAddress> getRecipientsTo() {
        return getRecipients(TO);
    }

    public boolean hasRecipientsTo() {
        return !getRecipientsTo().isEmpty();
    }

    @Nonnull
    public List<InternetAddress> getRecipientsCc() {
        return getRecipients(CC);
    }

    public boolean hasRecipientsCc() {
        return !getRecipientsCc().isEmpty();
    }

    @Nonnull
    public List<InternetAddress> getRecipientsBcc() {
        return getRecipients(BCC);
    }

    public boolean hasRecipientsBcc() {
        return !getRecipientsBcc().isEmpty();
    }

    @Nonnull
    public Map<ImmutableMimeType, String> getBody() {
        return unmodifiableMap(_body);
    }

    public boolean hasBody() {
        boolean result = false;
        for (String body : _body.values()) {
            if (!StringUtils.isEmpty(body)) {
                result = true;
                break;
            }
        }
        return result;
    }

    @Nullable
    public String getBody(@Nonnull ImmutableMimeType mimeType) {
        return _body.get(mimeType);
    }

    public boolean hasBody(@Nonnull ImmutableMimeType mimeType) {
        return !StringUtils.isEmpty(getBody(mimeType));
    }

    @Nullable
    public String getSubject() {
        return _subject;
    }

    public boolean hasSubject() {
        return !StringUtils.isEmpty(_subject);
    }

    @Nonnull
    protected Mail thisInstance() {
        return this;
    }
}

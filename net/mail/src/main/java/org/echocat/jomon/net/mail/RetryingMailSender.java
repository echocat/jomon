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

import org.echocat.jomon.runtime.concurrent.RetryForSpecifiedCountStrategy;
import org.echocat.jomon.runtime.concurrent.RetryingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.echocat.jomon.runtime.concurrent.Retryer.executeWithRetry;

public class RetryingMailSender implements MailSender {

    private static final Logger LOG = LoggerFactory.getLogger(RetryingMailSender.class);

    private static final RetryingStrategy<Void> DEFAULT_RETRYING_STRATEGY = RetryForSpecifiedCountStrategy.<Void>retryForSpecifiedCountOf(5).withExceptionsThatForceRetry(MailSenderException.class);

    private final MailSender _delegate;
    private final MailSender _fallback;

    private RetryingStrategy<Void> _retryingStrategy = DEFAULT_RETRYING_STRATEGY;
    private boolean _logWarnIfFallbackIsUsed;

    public RetryingMailSender(@Nonnull MailSender delegate) {
        this(delegate, null);
    }

    public RetryingMailSender(@Nonnull MailSender delegate, @Nullable MailSender fallback) {
        _delegate = delegate;
        _fallback = fallback;
    }

    public void setRetryingStrategy(@Nonnull RetryingStrategy<Void> retryingStrategy) {
        _retryingStrategy = retryingStrategy;
    }

    public void setLogWarnIfFallbackIsUsed(boolean logWarnIfFallbackIsUsed) {
        _logWarnIfFallbackIsUsed = logWarnIfFallbackIsUsed;
    }

    @Override
    public void send(@Nonnull final Mail mail) throws MailSenderException {
        try {
            executeWithRetry(new Runnable() {

                @Override
                public void run() {
                    _delegate.send(mail);
                }

            }, _retryingStrategy, MailSenderException.class);
        } catch (final MailSenderException e) {
            if (_fallback != null) {
                if (_logWarnIfFallbackIsUsed) {
                    LOG.warn("Could not send " + mail + " with " + _delegate + " after several retries (based on " + _retryingStrategy + "). Please check the following exception. Will now fallback to " + _fallback + ".", e);
                }
                _fallback.send(mail);
            } else {
                throw e;
            }
        }
    }
}

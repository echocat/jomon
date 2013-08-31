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

package org.echocat.jomon.maven.boot;

import org.codehaus.plexus.logging.Logger;

import javax.annotation.Nonnull;

public class PlexusToSl4jLogger implements Logger {

    private final org.slf4j.Logger _delegate;

    public PlexusToSl4jLogger(@Nonnull org.slf4j.Logger delegate) {
        _delegate = delegate;
    }

    @Override
    public void debug(String message) {
        _delegate.debug(message);
    }

    @Override
    public void debug(String message, Throwable throwable) {
        _delegate.debug(message, throwable);
    }

    @Override
    public boolean isDebugEnabled() {
        return _delegate.isDebugEnabled();
    }

    @Override
    public void info(String message) {
        _delegate.info(message);
    }

    @Override
    public void info(String message, Throwable throwable) {
        _delegate.info(message, throwable);
    }

    @Override
    public boolean isInfoEnabled() {
        return _delegate.isInfoEnabled();
    }

    @Override
    public void warn(String message) {
        _delegate.warn(message);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        _delegate.warn(message, throwable);
    }

    @Override
    public boolean isWarnEnabled() {
        return _delegate.isWarnEnabled();
    }

    @Override
    public void error(String message) {
        _delegate.error(message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        _delegate.error(message, throwable);
    }

    @Override
    public boolean isErrorEnabled() {
        return _delegate.isErrorEnabled();
    }

    @Override
    public void fatalError(String message) {
        _delegate.error(message);
    }

    @Override
    public void fatalError(String message, Throwable throwable) {
        _delegate.error(message, throwable);
    }

    @Override
    public boolean isFatalErrorEnabled() {
        return _delegate.isErrorEnabled();
    }

    @Override
    public int getThreshold() {
        final int threshold;
        if (!_delegate.isErrorEnabled()) {
            threshold = Logger.LEVEL_DISABLED;
        } else if (!_delegate.isWarnEnabled()) {
            threshold = Logger.LEVEL_ERROR;
        } else if (!_delegate.isInfoEnabled()) {
            threshold = Logger.LEVEL_WARN;
        } else if (!_delegate.isDebugEnabled()) {
            threshold = Logger.LEVEL_INFO;
        } else {
            threshold = Logger.LEVEL_DEBUG;
        }
        return threshold;
    }

    @Override
    public void setThreshold(int threshold) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Logger getChildLogger(String name) {
        return this;
    }

    @Override
    public String getName() {
        return _delegate.getName();
    }
}

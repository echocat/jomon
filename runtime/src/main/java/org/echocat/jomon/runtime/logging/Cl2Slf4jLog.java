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

package org.echocat.jomon.runtime.logging;

import org.apache.commons.logging.Log;
import org.slf4j.Logger;

import javax.annotation.Nonnull;

public class Cl2Slf4jLog implements Log {

    @Nonnull
    private final Logger _logger;

    public Cl2Slf4jLog(@Nonnull Logger logger) {
        _logger = logger;
    }

    @Override
    public boolean isDebugEnabled() {
        return _logger.isDebugEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return _logger.isErrorEnabled();
    }

    @Override
    public boolean isFatalEnabled() {
        return _logger.isErrorEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return _logger.isInfoEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return _logger.isTraceEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return _logger.isWarnEnabled();
    }

    @Override
    public void trace(Object message) {
        _logger.trace(String.valueOf(message));
    }

    @Override
    public void trace(Object message, Throwable t) {
        _logger.trace(String.valueOf(message), t);
    }

    @Override
    public void debug(Object message) {
        _logger.debug(String.valueOf(message));
    }

    @Override
    public void debug(Object message, Throwable t) {
        _logger.debug(String.valueOf(message), t);
    }

    @Override
    public void info(Object message) {
        _logger.info(String.valueOf(message));
    }

    @Override
    public void info(Object message, Throwable t) {
        _logger.info(String.valueOf(message), t);
    }

    @Override
    public void warn(Object message) {
        _logger.warn(String.valueOf(message));
    }

    @Override
    public void warn(Object message, Throwable t) {
        _logger.warn(String.valueOf(message), t);
    }

    @Override
    public void error(Object message) {
        _logger.error(String.valueOf(message));
    }

    @Override
    public void error(Object message, Throwable t) {
        _logger.error(String.valueOf(message), t);
    }

    @Override
    public void fatal(Object message) {
        _logger.error(String.valueOf(message));
    }

    @Override
    public void fatal(Object message, Throwable t) {
        _logger.error(String.valueOf(message), t);
    }

}
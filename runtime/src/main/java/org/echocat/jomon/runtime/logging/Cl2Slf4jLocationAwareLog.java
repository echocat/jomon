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

import org.slf4j.spi.LocationAwareLogger;

import javax.annotation.Nonnull;

import static java.lang.String.valueOf;

public class Cl2Slf4jLocationAwareLog extends Cl2Slf4jLog {

    private static final String FQCN = Cl2Slf4jLocationAwareLog.class.getName();

    @Nonnull
    private final LocationAwareLogger _logger;

    public Cl2Slf4jLocationAwareLog(@Nonnull LocationAwareLogger logger) {
        super(logger);
        _logger = logger;
    }

    @Override
    public void trace(Object message) {
        _logger.log(null, FQCN, LocationAwareLogger.TRACE_INT, valueOf(message), null, null);
    }

    @Override
    public void trace(Object message, Throwable t) {
        _logger.log(null, FQCN, LocationAwareLogger.TRACE_INT, valueOf(message), null, t);
    }

    @Override
    public void debug(Object message) {
        _logger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, valueOf(message), null, null);
    }

    @Override
    public void debug(Object message, Throwable t) {
        _logger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, valueOf(message), null, t);
    }

    @Override
    public void info(Object message) {
        _logger.log(null, FQCN, LocationAwareLogger.INFO_INT, valueOf(message), null, null);
    }

    @Override
    public void info(Object message, Throwable t) {
        _logger.log(null, FQCN, LocationAwareLogger.INFO_INT, valueOf(message), null, t);
    }

    @Override
    public void warn(Object message) {
        _logger.log(null, FQCN, LocationAwareLogger.WARN_INT, valueOf(message), null, null);
    }

    @Override
    public void warn(Object message, Throwable t) {
        _logger.log(null, FQCN, LocationAwareLogger.WARN_INT, valueOf(message), null, t);
    }

    @Override
    public void error(Object message) {
        _logger.log(null, FQCN, LocationAwareLogger.ERROR_INT, valueOf(message), null, null);
    }

    @Override
    public void error(Object message, Throwable t) {
        _logger.log(null, FQCN, LocationAwareLogger.ERROR_INT, valueOf(message), null, t);
    }

    @Override
    public void fatal(Object message) {
        _logger.log(null, FQCN, LocationAwareLogger.ERROR_INT, valueOf(message), null, null);
    }

    @Override
    public void fatal(Object message, Throwable t) {
        _logger.log(null, FQCN, LocationAwareLogger.ERROR_INT, valueOf(message), null, t);
    }

}
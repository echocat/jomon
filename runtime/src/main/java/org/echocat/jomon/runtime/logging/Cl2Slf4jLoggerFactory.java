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
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class Cl2Slf4jLoggerFactory extends LogFactory {

    public static final String LOG_PROPERTY = "org.apache.commons.logging.Log";

    @Nonnull
    private final ILoggerFactory _loggerFactory;
    @Nonnull
    private final Map<String, Log> _loggerMap = new HashMap<>();
    @Nonnull
    private final Map<String, Object> _attributes = new HashMap<>();

    public Cl2Slf4jLoggerFactory(@Nullable ILoggerFactory loggerFactory) {
        _loggerFactory = loggerFactory != null ? loggerFactory : LoggerFactory.getILoggerFactory();
    }

    @Override
    public Log getInstance(@SuppressWarnings("rawtypes") Class clazz) throws LogConfigurationException {
        return getInstance(clazz.getName());
    }

    @Override
    public Log getInstance(String name) throws LogConfigurationException {
        Log instance;
        synchronized (_loggerMap) {
            instance = _loggerMap.get(name);
            if (instance == null) {
                final Logger logger = _loggerFactory.getLogger(name);
                if(logger instanceof LocationAwareLogger) {
                    instance = new Cl2Slf4jLocationAwareLog((LocationAwareLogger) logger);
                } else {
                    instance = new Cl2Slf4jLog(logger);
                }
                _loggerMap.put(name, instance);
            }
        }
        return instance;
    }

    @Override
    public void release() {}

    @Override
    public void removeAttribute(String name) {
        _attributes.remove(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        if (value == null) {
            _attributes.remove(name);
        } else {
            _attributes.put(name, value);
        }
    }

    @Override
    public Object getAttribute(String name) {
        return _attributes.get(name);
    }

    @Override
    public String[] getAttributeNames() {
        final Set<String> keys = _attributes.keySet();
        return keys.toArray(new String[keys.size()]);
    }


}

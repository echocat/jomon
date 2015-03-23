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

package org.slf4j;

import org.slf4j.helpers.NOPLoggerFactory;
import org.slf4j.helpers.SubstituteLoggerFactory;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import static org.slf4j.LoggerFactory.NOP_FALLBACK_INITIALIZATION;

public class LoggerFactoryRegistry {

    private LoggerFactoryRegistry() {}

    @Nonnull
    public static Registration register(@Nonnull final ILoggerFactory loggerFactory) {
        synchronized (LoggerFactoryRegistry.class) {
            final Registration registration = new ActivationRegistration();
            LoggerFactory.INITIALIZATION_STATE = NOP_FALLBACK_INITIALIZATION;
            LoggerFactory.NOP_FALLBACK_FACTORY = new NOPLoggerFactory() {
                @Override
                public Logger getLogger(String name) {
                    return loggerFactory.getLogger(name);
                }
            };
            return registration;

        }

    }

    public static interface Registration extends AutoCloseable {}

    private static class ActivationRegistration implements Registration {

        @Nonnegative
        private final int _originalInitializationState;
        @Nonnull
        private final SubstituteLoggerFactory _originalTempFactory;
        @Nonnull
        private final NOPLoggerFactory _originalNopFallbackFactory;

        private boolean _alreadyClosed;

        private ActivationRegistration() {
            synchronized (LoggerFactoryRegistry.class) {
                _originalInitializationState = LoggerFactory.INITIALIZATION_STATE;
                _originalTempFactory = LoggerFactory.TEMP_FACTORY;
                _originalNopFallbackFactory = LoggerFactory.NOP_FALLBACK_FACTORY;
            }
        }

        @Override
        public void close() throws Exception {
            synchronized (LoggerFactoryRegistry.class) {
                if (!_alreadyClosed) {
                    _alreadyClosed = true;
                    LoggerFactory.INITIALIZATION_STATE = _originalInitializationState;
                    LoggerFactory.TEMP_FACTORY = _originalTempFactory;
                    LoggerFactory.NOP_FALLBACK_FACTORY = _originalNopFallbackFactory;
                }
            }
        }

    }

}

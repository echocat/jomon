/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012-2014 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.resources.optimizing.yui;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import java.util.logging.Logger;

public class ErrorReporterImpl implements ErrorReporter {

    private static final Logger LOG = Logger.getLogger(ErrorReporterImpl.class.getName());

    @Override
    public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
        // noinspection ThrowableResultOfMethodCallIgnored
        LOG.warning(runtimeError(message, sourceName, line, lineSource, lineOffset).getMessage());
    }

    @Override
    public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
        throw runtimeError(message, sourceName, line, lineSource, lineOffset);
    }

    @Override
    public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
        return new EvaluatorException(message, sourceName, line, lineSource, lineOffset);
    }

}

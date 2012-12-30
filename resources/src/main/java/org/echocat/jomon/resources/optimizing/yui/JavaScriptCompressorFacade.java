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

package org.echocat.jomon.resources.optimizing.yui;

import javax.annotation.Nonnull;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class JavaScriptCompressorFacade extends FacadeSupport {

    protected static final Class<?> COMPRESSOR_CLASS = loadClass("com.yahoo.platform.yui.compressor.JavaScriptCompressor", CLASS_LOADER);
    protected static final Class<?> ERROR_REPORTER_CLASS = loadClass("org.mozilla.javascript.ErrorReporter", CLASS_LOADER);
    protected static final Class<?> ERROR_REPORTER_IMPL_CLASS = loadClass("org.echocat.jomon.resources.optimizing.yui.ErrorReporterImpl", CLASS_LOADER);

    protected static final Constructor<?> COMPRESSOR_CONSTRUCTOR = loadConstructor(COMPRESSOR_CLASS, Reader.class, ERROR_REPORTER_CLASS);
    protected static final Constructor<?> ERROR_REPORTER_IMPL_CONSTRUCTOR = loadConstructor(ERROR_REPORTER_IMPL_CLASS);

    protected static final Method COMPRESS_METHOD = loadMethod(COMPRESSOR_CLASS, "compress", Writer.class, int.class, boolean.class, boolean.class, boolean.class, boolean.class);

    private final Object _instance;

    public JavaScriptCompressorFacade(@Nonnull Reader in) throws Exception {
        final Object errorReporter = ERROR_REPORTER_IMPL_CONSTRUCTOR.newInstance();
        _instance = COMPRESSOR_CONSTRUCTOR.newInstance(in, errorReporter);
    }

    public void compress(@Nonnull Writer out, int linebreak, boolean munge, boolean verbose, boolean preserveAllSemiColons, boolean disableOptimizations) throws Exception {
        try {
            COMPRESS_METHOD.invoke(_instance, out, linebreak, munge, verbose, preserveAllSemiColons, disableOptimizations);
        } catch (InvocationTargetException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                // noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
                throw (Exception)cause;
            } else if (cause instanceof Error) {
                // noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
                throw (Error)cause;
            } else {
                throw e;
            }
        }
    }
}

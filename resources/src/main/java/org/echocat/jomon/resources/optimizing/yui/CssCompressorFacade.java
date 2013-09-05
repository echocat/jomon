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

package org.echocat.jomon.resources.optimizing.yui;

import javax.annotation.Nonnull;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CssCompressorFacade extends FacadeSupport {

    protected static final Class<?> COMPRESSOR_CLASS = loadClass("com.yahoo.platform.yui.compressor.CssCompressor", CLASS_LOADER);
    protected static final Constructor<?> COMPRESSOR_CONSTRUCTOR = loadConstructor(COMPRESSOR_CLASS, Reader.class);
    protected static final Method COMPRESS_METHOD = loadMethod(COMPRESSOR_CLASS, "compress", Writer.class, int.class);

    private final Object _instance;

    public CssCompressorFacade(@Nonnull Reader in) throws Exception {
        _instance = COMPRESSOR_CONSTRUCTOR.newInstance(in);
    }

    public void compress(@Nonnull Writer out, int linebreakpos) throws Exception {
        try {
            COMPRESS_METHOD.invoke(_instance, out, linebreakpos);
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

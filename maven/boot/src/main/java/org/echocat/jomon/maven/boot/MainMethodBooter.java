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

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MainMethodBooter {

    private static final String MAIN_METHOD_NAME = "main";

    public void execute(@Nonnull Class<?> mainClass, @Nonnull String[] arguments) throws Exception {
        final Method method;
        try {
            method = mainClass.getMethod(MAIN_METHOD_NAME, String[].class);
        } catch (final NoSuchMethodException e) {
            //noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
            throw new IllegalStateException("Could not found the method '" + MAIN_METHOD_NAME + "' at the mainClass '" + mainClass.getName() + "'.");
        }
        invokeMethod(arguments, method);
    }

    private void invokeMethod(@Nonnull String[] arguments, @Nonnull Method method) throws Exception {
        try {
            method.invoke(null, new Object[]{arguments});
        } catch (final InvocationTargetException e) {
            final Throwable target = e.getTargetException();
            if (target instanceof Error) {
                //noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
                throw (Error) target;
            } else if (target instanceof RuntimeException) {
                //noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
                throw (RuntimeException) target;
            } else if (target instanceof Exception) {
                //noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
                throw (Exception) target;
            } else {
                throw e;
            }
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException("Could not access the method " + method + ".", e);
        }
    }


}

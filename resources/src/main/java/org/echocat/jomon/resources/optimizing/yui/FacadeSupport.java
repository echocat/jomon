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
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public abstract class FacadeSupport {

    protected static final ClassLoader CLASS_LOADER = YuiClassLoaderFactory.getClassLoader();

    @Nonnull
    protected static Class<?> loadClass(@Nonnull String className, @Nonnull ClassLoader from) {
        try {
            return from.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Nonnull
    protected static Constructor<?> loadConstructor(@Nonnull Class<?> type, Class<?>... arguments) {
        try {
            return type.getConstructor(arguments);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Nonnull
    protected static Method loadMethod(@Nonnull Class<?> type, @Nonnull String name, Class<?>... arguments) {
        try {
            return type.getMethod(name, arguments);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}

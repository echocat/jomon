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

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.echocat.jomon.resources.optimizing.yui.YuiClassLoaderFactory.yuiClassLoader;

public abstract class FacadeSupport {

    @Nonnull
    protected static final ClassLoader CLASS_LOADER = yuiClassLoader();

    @Nonnull
    protected static Class<?> loadYuiClass(@Nonnull String className) {
        try {
            return CLASS_LOADER.loadClass(className);
        } catch (final ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Nonnull
    protected static Constructor<?> getYuiConstructor(@Nonnull Class<?> type, Class<?>... arguments) {
        try {
            return type.getConstructor(arguments);
        } catch (final NoSuchMethodException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Nonnull
    protected static Method getYuiMethod(@Nonnull Class<?> type, @Nonnull String name, Class<?>... arguments) {
        try {
            return type.getMethod(name, arguments);
        } catch (final NoSuchMethodException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}

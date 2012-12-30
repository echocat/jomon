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

package org.echocat.jomon.runtime.valuemodule;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;

import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;

public class ValueModuleUtils {

    public static final String ALL_NAME = "all";

    @Nonnull
    public static <T extends Enum<T>> T[] valuesOf(@Nonnull Class<T> type) {
        final Method method;
        try {
            method = type.getMethod("values");
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(type.getName() + " is not a valid enum.", e);
        }
        checkValuesMethod(method);
        try {
            // noinspection unchecked
            return (T[]) method.invoke(null);
        } catch (Exception e) {
            throw new RuntimeException("Could not get values of " + type.getName() + ".", e);
        }
    }

    private static void checkValuesMethod(@Nonnull Method method) {
        final int modifiers = method.getModifiers();
        final Class<?> returnType = method.getReturnType();
        if (!isStatic(modifiers) || !isPublic(modifiers) || !returnType.isArray() || !method.getDeclaringClass().equals(returnType.getComponentType())) {
            throw new IllegalArgumentException(returnType.getDeclaringClass().getName() + " is not a valid enum.");
        }
    }


    private ValueModuleUtils() {}
}

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

package org.echocat.jomon.runtime.codec;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class HashFunctionUtils {

    public static final char[] HEX_CHARACTERS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    @Nonnull
    public static <T extends HashFunction> Constructor<? extends T> selectConstructorFor(@Nonnull Class<T> requiredType, @Nonnull Class<? extends T> defaultImplementation) {
        final String typePropertyName = requiredType.getName() + ".implementation";
        final Class<? extends T> type = retrieveDefaultType(typePropertyName, requiredType, defaultImplementation);
        final Constructor<? extends T> constructor = retrieveDefaultConstructorFor(typePropertyName, type);
        return constructor;
    }

    @Nonnull
    public static <T extends HashFunction> T newInstanceOf(@Nonnull Constructor<? extends T> constructor) {
        try {
            return constructor.newInstance();
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
            final Throwable cause = e instanceof InvocationTargetException ? e.getCause() : null;
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else if (cause instanceof Error) {
                throw (Error) cause;
            } else {
                throw new RuntimeException("Could not create an instance of '" + constructor + "'.", e);
            }
        }
    }

    @Nonnull
    private static <T extends HashFunction> Class<? extends T> retrieveDefaultType(@Nonnull String propertyName, @Nonnull Class<T> hashFunctionType, @Nonnull Class<? extends T> defaultImplementatio) {
        final String typeName = System.getProperty(propertyName, defaultImplementatio.getName());
        final Class<?> plainType;
        try {
            plainType = HashFunctionUtils.class.getClassLoader().loadClass(typeName.trim());
        } catch (final ClassNotFoundException e) {
            throw new IllegalArgumentException("Illegal value of '" + propertyName + "'. Could not find class for name: " + typeName, e);
        }
        if (!hashFunctionType.isAssignableFrom(plainType)) {
            throw new IllegalArgumentException("Illegal value of '" + propertyName + "'. Class '" + typeName + "' is not type of '" + hashFunctionType.getName() + "'.");
        }
        // noinspection unchecked
        return (Class<? extends T>) plainType;
    }

    @Nonnull
    private static <T extends HashFunction> Constructor<? extends T> retrieveDefaultConstructorFor(@Nonnull String propertyName, @Nonnull Class<? extends T> hashFunctionType) {
        final Constructor<? extends T> constructor;
        try {
            constructor = hashFunctionType.getConstructor();
        } catch (final NoSuchMethodException e) {
            throw new IllegalArgumentException("Illegal value of '" + propertyName + "'. Class '" + hashFunctionType.getName() + "' has no default constructor.", e);
        }
        constructor.setAccessible(true);
        return constructor;
    }

    @Nonnull
    private static char[] asHexCharacters(@Nonnull byte[] bytes) {
        final int length = bytes.length;
        final char[] out = new char[length << 1];
        int j = 0;
        for (int i = 0; i < length; i++) {
            final byte b = bytes[i];
            out[j++] = HEX_CHARACTERS[(240 & b) >>> 4];
            out[j++] = HEX_CHARACTERS[15 & b];
        }
        return out;
    }

    @Nonnull
    public static String asHexString(@Nonnull byte[] bytes) {
        return new String(asHexCharacters(bytes));
    }

    private HashFunctionUtils() {}

}

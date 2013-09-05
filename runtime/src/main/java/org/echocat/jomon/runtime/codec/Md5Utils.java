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

package org.echocat.jomon.runtime.codec;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.WillNotClose;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;

public class Md5Utils {

    public static final String TYPE_PROPERTY_NAME = Md5Utils.class.getPackage().getName() + ".type";
    public static final Class<? extends Md5> DEFAULT_TYPE = retrieveDefaultType();
    public static final Constructor<? extends Md5> DEFAULT_CONSTRUCTOR = retrieveDefaultConstructorFor(DEFAULT_TYPE);

    @Nonnull
    public static Md5 md5() {
        try {
            return DEFAULT_CONSTRUCTOR.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Could not create an instance of '" + DEFAULT_CONSTRUCTOR + "'.", e);
        }
    }

    @Nonnull
    public static Md5 md5Of(@Nullable String with) {
        return md5().update(with);
    }

    @Nonnull
    public static Md5 md5Of(@Nullable String with, @Nonnull Charset charset) {
        return md5().update(with, charset);
    }

    @Nonnull
    public static Md5 md5Of(byte with) {
        return md5().update(with);
    }

    @Nonnull
    public static Md5 md5Of(@Nullable byte[] with) {
        return md5().update(with);
    }

    @Nonnull
    public static Md5 md5Of(@Nullable byte[] with, @Nonnegative int offset, @Nonnegative int length) {
        return md5().update(with, offset, length);
    }

    @Nonnull
    public static Md5 md5Of(@Nullable @WillNotClose InputStream is) throws IOException {
        return md5().update(is);
    }

    @Nonnull
    public static Md5 md5Of(@Nullable File file) throws IOException {
        return md5().update(file);
    }

    @Nonnull
    private static Class<? extends Md5> retrieveDefaultType() {
        final String typeName = System.getProperty(TYPE_PROPERTY_NAME, DefaultMd5.class.getName());
        final Class<?> plainType;
        try {
            plainType = Md5Utils.class.getClassLoader().loadClass(typeName.trim());
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Illegal value of '" + TYPE_PROPERTY_NAME + "'. Could not find class for name: " + typeName, e);
        }
        if (!Md5.class.isAssignableFrom(plainType)) {
            throw new IllegalArgumentException("Illegal value of '" + TYPE_PROPERTY_NAME + "'. Class '" + typeName + "' is not type of '" + Md5.class.getName() + "'.");
        }
        // noinspection unchecked
        return (Class<? extends Md5>) plainType;
    }

    @Nonnull
    private static Constructor<? extends Md5> retrieveDefaultConstructorFor(@Nonnull Class<? extends Md5> type) {
        final Constructor<? extends Md5> constructor;
        try {
            constructor = type.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Illegal value of '" + TYPE_PROPERTY_NAME + "'. Class '" + type.getName() + "' has no default constructor.", e);
        }
        constructor.setAccessible(true);
        return constructor;
    }

    private Md5Utils() {}

}

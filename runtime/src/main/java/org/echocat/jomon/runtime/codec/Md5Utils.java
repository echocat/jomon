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
import java.nio.charset.Charset;

import static org.echocat.jomon.runtime.codec.HashFunctionUtils.newInstanceOf;
import static org.echocat.jomon.runtime.codec.HashFunctionUtils.selectConstructorFor;

public class Md5Utils {

    protected static final Constructor<? extends Md5> DEFAULT_CONSTRUCTOR = selectConstructorFor(Md5.class, DefaultMd5.class);

    @Nonnull
    public static Md5 md5() {
        return newInstanceOf(DEFAULT_CONSTRUCTOR);
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

    private Md5Utils() {}

}

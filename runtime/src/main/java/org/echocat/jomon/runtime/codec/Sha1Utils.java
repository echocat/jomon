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

public class Sha1Utils {

    protected static final Constructor<? extends Sha1> DEFAULT_CONSTRUCTOR = selectConstructorFor(Sha1.class, DefaultSha1.class);

    @Nonnull
    public static Sha1 sha1() {
        return newInstanceOf(DEFAULT_CONSTRUCTOR);
    }

    @Nonnull
    public static Sha1 sha1Of(@Nullable String with) {
        return sha1().update(with);
    }

    @Nonnull
    public static Sha1 sha1Of(@Nullable String with, @Nonnull Charset charset) {
        return sha1().update(with, charset);
    }

    @Nonnull
    public static Sha1 sha1Of(byte with) {
        return sha1().update(with);
    }

    @Nonnull
    public static Sha1 sha1Of(@Nullable byte[] with) {
        return sha1().update(with);
    }

    @Nonnull
    public static Sha1 sha1Of(@Nullable byte[] with, @Nonnegative int offset, @Nonnegative int length) {
        return sha1().update(with, offset, length);
    }

    @Nonnull
    public static Sha1 sha1Of(@Nullable @WillNotClose InputStream is) throws IOException {
        return sha1().update(is);
    }

    @Nonnull
    public static Sha1 sha1Of(@Nullable File file) throws IOException {
        return sha1().update(file);
    }

    private Sha1Utils() {}

}

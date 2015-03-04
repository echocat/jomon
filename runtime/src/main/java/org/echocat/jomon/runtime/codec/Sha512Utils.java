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

public class Sha512Utils {

    protected static final Constructor<? extends Sha512> DEFAULT_CONSTRUCTOR = selectConstructorFor(Sha512.class, DefaultSha512.class);

    @Nonnull
    public static Sha512 sha512() {
        return newInstanceOf(DEFAULT_CONSTRUCTOR);
    }

    @Nonnull
    public static Sha512 sha512Of(@Nullable String with) {
        return sha512().update(with);
    }

    @Nonnull
    public static Sha512 sha512Of(@Nullable String with, @Nonnull Charset charset) {
        return sha512().update(with, charset);
    }

    @Nonnull
    public static Sha512 sha512Of(byte with) {
        return sha512().update(with);
    }

    @Nonnull
    public static Sha512 sha512Of(@Nullable byte[] with) {
        return sha512().update(with);
    }

    @Nonnull
    public static Sha512 sha512Of(@Nullable byte[] with, @Nonnegative int offset, @Nonnegative int length) {
        return sha512().update(with, offset, length);
    }

    @Nonnull
    public static Sha512 sha512Of(@Nullable @WillNotClose InputStream is) throws IOException {
        return sha512().update(is);
    }

    @Nonnull
    public static Sha512 sha512Of(@Nullable File file) throws IOException {
        return sha512().update(file);
    }

    private Sha512Utils() {}

}

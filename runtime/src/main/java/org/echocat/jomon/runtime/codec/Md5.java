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
import java.nio.charset.Charset;

public interface Md5 {

    @Nonnull
    public Md5 update(@Nullable String with);

    @Nonnull
    public Md5 update(@Nullable String with, @Nonnull Charset charset);

    @Nonnull
    public Md5 update(byte with);

    @Nonnull
    public Md5 update(@Nullable byte[] with);

    @Nonnull
    public Md5 update(@Nullable byte[] with, @Nonnegative int offset, @Nonnegative int length);

    @Nonnull
    public Md5 update(@Nullable @WillNotClose InputStream is) throws IOException;

    @Nonnull
    public Md5 update(@Nullable File file) throws IOException;

    @Nonnull
    public byte[] asBytes();

    @Nonnull
    public char[] asHexCharacters();

    @Nonnull
    public String asHexString();

}

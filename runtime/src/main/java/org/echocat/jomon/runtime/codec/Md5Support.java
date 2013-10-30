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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.WillNotClose;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import static java.nio.charset.Charset.forName;

public abstract class Md5Support implements Md5 {

    public static final Charset DEFAULT_CHARSET = forName("UTF-8");

    @Override
    @Nonnull
    public Md5 update(@Nullable String with) {
        update(with, DEFAULT_CHARSET);
        return this;
    }

    @Override
    @Nonnull
    public Md5Support update(@Nullable String with, @Nonnull Charset charset) {
        if (with != null) {
            final byte[] bytes = with.getBytes(charset);
            update(bytes, 0, bytes.length);
        }
        return this;
    }

    @Override
    @Nonnull
    public Md5 update(byte with) {
        update(new byte[]{with}, 0, 1);
        return this;
    }

    @Override
    @Nonnull
    public Md5 update(@Nullable byte[] with) {
        if (with != null) {
            update(with, 0, with.length);
        }
        return this;
    }

    @Override
    @Nonnull
    public Md5 update(@Nullable @WillNotClose InputStream is) throws IOException {
        if (is != null) {
            final byte[] buffer = new byte[4096];
            int read = is.read(buffer);
            while (read > -1) {
                update(buffer, 0, read);
                read = is.read(buffer);
            }
        }
        return this;
    }

    @Nonnull
    @Override
    public Md5 update(@Nullable File file) throws IOException {
        if (file != null) {
            try (final InputStream is = new FileInputStream(file)) {
                update(is);
            }
        }
        return this;
    }

    @Nonnull
    @Override
    public char[] asHexCharacters() {
        return Md5Utils.asHexCharacters(asBytes());
    }

    @Nonnull
    @Override
    public String asHexString() {
        return Md5Utils.asHexString(asBytes());
    }

}

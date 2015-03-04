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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import static java.nio.charset.Charset.forName;
import static org.apache.commons.codec.binary.Base64.encodeBase64;

public abstract class HashFunctionSupport<T extends HashFunctionSupport<T>> implements HashFunction {

    public static final Charset DEFAULT_CHARSET = forName("UTF-8");

    @Nonnull
    @Override
    public abstract T update(@Nullable byte[] with, @Nonnegative int offset, @Nonnegative int length);

    @Override
    @Nonnull
    public T update(@Nullable String with) {
        update(with, DEFAULT_CHARSET);
        return thisObject();
    }

    @Override
    @Nonnull
    public T update(@Nullable String with, @Nonnull Charset charset) {
        if (with != null) {
            final byte[] bytes = with.getBytes(charset);
            update(bytes, 0, bytes.length);
        }
        return thisObject();
    }

    @Override
    @Nonnull
    public T update(byte with) {
        update(new byte[]{with}, 0, 1);
        return thisObject();
    }

    @Override
    @Nonnull
    public T update(@Nullable byte[] with) {
        if (with != null) {
            update(with, 0, with.length);
        }
        return thisObject();
    }

    @Override
    @Nonnull
    public T update(@Nullable @WillNotClose InputStream is) throws IOException {
        if (is != null) {
            final byte[] buffer = new byte[4096];
            int read = is.read(buffer);
            while (read > -1) {
                update(buffer, 0, read);
                read = is.read(buffer);
            }
        }
        return thisObject();
    }

    @Nonnull
    @Override
    public T update(@Nullable File file) throws IOException {
        if (file != null) {
            try (final InputStream is = new FileInputStream(file)) {
                update(is);
            }
        }
        return thisObject();
    }

    @Nonnull
    @Override
    public String asHexString() {
        return HashFunctionUtils.asHexString(asBytes());
    }

    @Override
    @Nonnull
    public byte[] asBase64() {
        return encodeBase64(asBytes());
    }

    @Override
    @Nonnull
    public String asBase64String() {
        return new String(asBase64(), DEFAULT_CHARSET);
    }

    @Nonnull
    protected T thisObject() {
        //noinspection unchecked
        return (T) this;
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else if (o == null || getClass() != o.getClass()) {
            result = false;
        } else {
            final HashFunctionSupport<?> that = (HashFunctionSupport) o;
            result = Arrays.equals(asBytes(), that.asBytes());
        }
        return result;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(asBytes());
    }

}

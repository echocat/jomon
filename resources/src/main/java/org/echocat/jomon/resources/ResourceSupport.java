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

package org.echocat.jomon.resources;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static org.echocat.jomon.runtime.codec.Md5Utils.md5Of;
import static org.apache.commons.codec.binary.Hex.encodeHexString;

public abstract class ResourceSupport implements Resource {

    private volatile byte[] _md5;
    private volatile Long _size;

    @Override
    public boolean equals(Object o) {
        try {
            final boolean result;
            if (this == o) {
                result = true;
            } else if (o == null || !Resource.class.isInstance(o)) {
                result = false;
            } else {
                final Resource that = (Resource) o;
                result = Arrays.equals(getMd5(), that.getMd5()) && getSize() == getSize();
            }
            return result;
        } catch (final Exception e) {
            throw new RuntimeException("Could not check equality.", e);
        }
    }

    @Override
    public int hashCode() {
        try {
            int result = Arrays.hashCode(getMd5());
            result = 37 * result + (int)getSize();
            return result;
        } catch (final Exception e) {
            throw new RuntimeException("Could not build hashCode.", e);
        }
    }

    @Override
    public String toString() {
        String result;
        try {
            result = encodeHexString(getMd5()) + "_" + getSize() + "." + getType();
        } catch (final Exception ignored) {
            result = super.toString();
        }
        return result;
    }

    @Nonnull
    @Override
    public byte[] getMd5() throws IOException {
        if (_md5 == null) {
            try (final InputStream is = openInputStream()) {
                _md5 = md5Of(is).asBytes();
            }
        }
        return _md5;
    }

    @Override
    public long getSize() throws IOException {
        if (_size == null) {
            try (final InputStream is = openInputStream()) {
                final byte[] buffer = new byte[4096];
                int read = is.read(buffer);
                long size = 0;
                while (read >= 0) {
                    size += read;
                    read = is.read(buffer);
                }
                _size = size;
            }
        }
        return _size;
    }
    @Override public void touch() throws IOException {}

    @Override public void release() throws IOException {}
}

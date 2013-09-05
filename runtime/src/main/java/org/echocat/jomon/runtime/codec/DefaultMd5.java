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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DefaultMd5 extends Md5Support {

    private final MessageDigest _delegate;

    public DefaultMd5() {
        try {
            _delegate = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException("Could not create an instance of md5 because it is not supported by JVM.", e);
        }
    }

    @Override
    @Nonnull
    public Md5 update(byte with) {
        _delegate.update(with);
        return this;
    }

    @Override
    @Nonnull
    public Md5 update(@Nullable byte[] with) {
        if (with != null) {
            _delegate.update(with);
        }
        return this;
    }

    @Override
    @Nonnull
    public Md5 update(@Nullable byte[] with, @Nonnegative int offset, @Nonnegative int length) {
        if (with != null) {
            _delegate.update(with, offset, length);
        }
        return this;
    }

    @Nonnull
    @Override
    public byte[] asBytes() {
        return _delegate.digest();
    }

}

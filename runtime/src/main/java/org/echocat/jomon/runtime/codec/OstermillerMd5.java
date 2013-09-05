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

import com.Ostermiller.util.MD5;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OstermillerMd5 extends Md5Support {

    private final MD5 _delegate = new MD5();

    @Override
    public Md5 update(@Nullable byte[] with, @Nonnegative int offset, @Nonnegative int length) {
        if (with != null) {
            _delegate.update(with, offset, length);
        }
        return this;
    }

    @Nonnull
    @Override
    public byte[] asBytes() {
        return _delegate.getHash();
    }

}

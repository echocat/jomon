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
import java.nio.charset.Charset;

public interface Sha384 extends HashFunction {

    @Nonnull
    @Override
    public Sha384 update(@Nullable String with);

    @Nonnull
    @Override
    public Sha384 update(@Nullable String with, @Nonnull Charset charset);

    @Nonnull
    @Override
    public Sha384 update(byte with);

    @Nonnull
    @Override
    public Sha384 update(@Nullable byte[] with);

    @Nonnull
    @Override
    public Sha384 update(@Nullable byte[] with, @Nonnegative int offset, @Nonnegative int length);

    @Nonnull
    @Override
    public Sha384 update(@Nullable @WillNotClose InputStream is) throws IOException;

    @Nonnull
    @Override
    public Sha384 update(@Nullable File file) throws IOException;

}

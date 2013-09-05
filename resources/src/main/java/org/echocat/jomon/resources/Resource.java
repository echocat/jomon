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

package org.echocat.jomon.resources;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public interface Resource {

    @Nonnull
    public InputStream openInputStream() throws IOException;

    @Nonnull
    public ResourceType getType() throws IOException;

    @Nonnull
    public byte[] getMd5() throws IOException;

    @Nonnegative
    public long getSize() throws IOException;

    @Nullable
    public Date getLastModified() throws IOException;

    public boolean isExisting() throws IOException;

    public void release() throws IOException;

    public boolean isGenerated() throws IOException;

    public void touch() throws IOException;
}

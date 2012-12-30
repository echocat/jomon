/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.resources;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.Date;

public class MemoryResource extends ResourceSupport {

    private final byte[] _content;
    private final byte[] _md5;
    private final ResourceType _type;
    private final boolean _generated;
    private final Date _lastModified = new Date();

    public MemoryResource(@Nonnull byte[] content, @Nonnull byte[] md5, @Nonnull ResourceType type, boolean generated) {
        _content = content;
        _md5 = md5;
        _type = type;
        _generated = generated;
    }

    @Nonnull
    @Override
    public InputStream openInputStream() throws IOException {
        return new ByteArrayInputStream(_content);
    }

    @Nonnull
    @Override
    public ResourceType getType() {
        return _type;
    }

    @Nonnull
    @Override
    public byte[] getMd5() {
        return _md5;
    }

    @Override
    public long getSize() {
        return _content.length;
    }

    @Override
    public Date getLastModified() {
        return _lastModified;
    }

    @Override
    public boolean isExisting() throws IOException {
        return true;
    }

    @Override
    public boolean isGenerated() throws IOException {
        return _generated;
    }
}

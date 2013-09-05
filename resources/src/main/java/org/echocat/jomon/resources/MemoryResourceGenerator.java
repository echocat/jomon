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

import com.Ostermiller.util.MD5OutputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.apache.commons.io.IOUtils.closeQuietly;

@NotThreadSafe
public class MemoryResourceGenerator extends ResourceGenerator {

    private final ByteArrayOutputStream _outputStream;
    private final MD5OutputStream _md5OutputStream;

    public MemoryResourceGenerator(@Nonnull Resource originalResource) throws IOException {
        this(originalResource, null);
    }

    public MemoryResourceGenerator(@Nonnull Resource originalResource, @Nullable String name) throws IOException {
        this(originalResource.getType(), originalResource, name);
    }

    public MemoryResourceGenerator(@Nonnull ResourceType type) throws IOException {
        this(type, null);
    }

    public MemoryResourceGenerator(@Nonnull ResourceType type, @Nullable String name) throws IOException {
        this(type, null, name);
    }

    private MemoryResourceGenerator(@Nonnull ResourceType type, @Nullable Resource originalResource, @Nullable String name) throws IOException {
        super(type, originalResource, name);
        boolean success = false;
        _outputStream = new ByteArrayOutputStream();
        try {
            _md5OutputStream = new MD5OutputStream(_outputStream);
            success = true;
        } finally {
            if (!success) {
                closeQuietly(_outputStream);
            }
        }
    }

    @Override
    protected MemoryResource generateResource() throws IOException {
        final byte[] md5 = _md5OutputStream.getHash();
        final MemoryResource result;
        if (getName() != null) {
            result = new NameEnabledMemoryResource(_outputStream.toByteArray(), md5, getType(), getName(), true);
        } else if (getOriginalResource() instanceof NameEnabledResource) {
            result = new NameEnabledMemoryResource(_outputStream.toByteArray(), md5, getType(), ((NameEnabledResource) getOriginalResource()).getName(), true);
        } else if (getOriginalResource() instanceof UriEnabledResource) {
            result = new NameEnabledMemoryResource(_outputStream.toByteArray(), md5, getType(), ((UriEnabledResource) getOriginalResource()).getUri(), true);
        } else {
            result = new MemoryResource(_outputStream.toByteArray(), md5, getType(), true);
        }
        return result;
    }

    @Override
    protected OutputStream getOutputStream() throws IOException {
        return _md5OutputStream;
    }

    @Override
    public void close() throws IOException {
        try {
            closeQuietly(_md5OutputStream);
        } finally {
            closeQuietly(_outputStream);
        }
    }
}

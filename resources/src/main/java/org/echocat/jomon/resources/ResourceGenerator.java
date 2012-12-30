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
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.io.OutputStream;

import static org.apache.commons.io.IOUtils.closeQuietly;

@NotThreadSafe
public abstract class ResourceGenerator extends OutputStream {

    private final Resource _originalResource;
    private final ResourceType _type;
    private final String _name;

    private Resource _resultResource;
    private boolean _dropped;

    protected ResourceGenerator(@Nonnull Resource originalResource) throws IOException {
        this(originalResource, null);
    }

    protected ResourceGenerator(@Nonnull Resource originalResource, @Nullable String name) throws IOException {
        this(originalResource.getType(), originalResource, name);
    }

    protected ResourceGenerator(@Nonnull ResourceType type) throws IOException {
        this(type, null);
    }

    protected ResourceGenerator(@Nonnull ResourceType type, @Nullable String name) throws IOException {
        this(type, null, name);
    }

    protected ResourceGenerator(@Nonnull ResourceType type, @Nullable Resource originalResource, @Nullable String name) throws IOException {
        _type = type;
        _originalResource = originalResource;
        _name = name;
    }

    @Nonnull
    public Resource getTemporaryBufferedResource() throws IOException {
        if (_dropped) {
            throw new IOException("This resources was dropped.");
        }
        if (_resultResource == null) {
            close();
            _resultResource = generateResource();
        }
        return _resultResource;
    }
    
    protected abstract Resource generateResource() throws IOException;

    protected abstract OutputStream getOutputStream() throws IOException;

    @Nullable
    protected Resource getOriginalResource() {
        return _originalResource;
    }

    @Nonnull
    protected ResourceType getType() {
        return _type;
    }

    @Nullable
    protected String getName() {
        return _name;
    }

    @Override
    public void write(int b) throws IOException {
        getOutputStream().write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        getOutputStream().write(b, off, len);
    }

    @Override
    public void write(byte[] b) throws IOException {
        getOutputStream().write(b);
    }

    @Override
    public void flush() throws IOException {
        getOutputStream().flush();
    }

    @Override
    public void close() throws IOException {}

    @Override
    protected void finalize() throws Throwable {
        try {
            closeQuietly(this);
        } finally {
            super.finalize();
        }
    }
    
    public void drop() throws IOException {
        _dropped = true;
        close();
    }
}

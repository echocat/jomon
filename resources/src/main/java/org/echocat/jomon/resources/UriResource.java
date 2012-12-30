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
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import static java.nio.file.Files.*;

public class UriResource extends ResourceSupport implements UriEnabledResource, PrivateUrlEnabledResource {

    private final URI _uri;
    private final Path _path;
    private final ResourceType _resourceType;
    private final boolean _generated;

    public UriResource(@Nonnull URI uri, @Nonnull ResourceType resourceType, boolean generated) {
        _uri = uri;
        _resourceType = resourceType;
        _generated = generated;
        _path = Paths.get(uri);
    }

    @Nonnull
    @Override
    public InputStream openInputStream() throws IOException {
        return newInputStream(_path);
    }

    @Nonnull
    @Override
    public ResourceType getType() throws IOException {
        return _resourceType;
    }

    @Override
    public long getSize() throws IOException {
        return size(_path);
    }

    @Override
    public Date getLastModified() throws IOException {
        return new Date(getLastModifiedTime(_path).toMillis());
    }

    @Override
    public boolean isExisting() throws IOException {
        return exists(_path);
    }

    @Override
    public boolean isGenerated() throws IOException {
        return _generated;
    }

    @Nonnull
    @Override
    public String getUri() {
        return _uri.toString();
    }

    @Nonnull
    @Override
    public URL getPrivateUrl() throws IOException {
        return _uri.toURL();
    }
}

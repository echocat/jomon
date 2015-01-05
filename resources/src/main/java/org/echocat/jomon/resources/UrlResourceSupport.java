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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

public abstract class UrlResourceSupport extends ResourceSupport implements UriEnabledResource {

    private final URL _url;
    private final ResourceType _resourceType;
    private final boolean _generated;

    private volatile Long _size;
    private volatile Boolean _exists;
    private volatile Date _lastModified;

    public UrlResourceSupport(@Nonnull URL url, @Nonnull ResourceType resourceType, boolean generated) {
        _url = url;
        _resourceType = resourceType;
        _generated = generated;
    }

    @Nonnull
    public URL getUrl() {
        return _url;
    }

    @Nonnull
    @Override
    public InputStream openInputStream() throws IOException {
        return _url.openStream();
    }

    @Nonnull
    @Override
    public ResourceType getType() throws IOException {
        return _resourceType;
    }

    @Override
    public long getSize() throws IOException {
        loadLazyValues();
        return _size;
    }

    @Override
    public Date getLastModified() throws IOException {
        loadLazyValues();
        return _lastModified;
    }

    @Override
    public boolean isExisting() throws IOException {
        loadLazyValues();
        return _exists;
    }

    @Override
    public boolean isGenerated() throws IOException {
        return _generated;
    }

    @Nonnull
    @Override
    public String getUri() {
        return _url.toString();
    }

    protected void loadLazyValues() throws IOException {
        if (_size == null || _exists == null) {
            try {
                final URLConnection urlConnection = _url.openConnection();
                try {
                    _size = urlConnection.getContentLengthLong();
                    _exists = true;
                    final long lastModified = urlConnection.getLastModified();
                    if (lastModified > 0) {
                        _lastModified = new Date(lastModified);
                    } else {
                        _lastModified = null;
                    }
                } finally {
                    if (urlConnection instanceof HttpURLConnection) {
                        ((HttpURLConnection) urlConnection).disconnect();
                    }
                }
            } catch (final FileNotFoundException ignored) {
                _exists = false;
                _size = 0L;
            }
        }
    }

}

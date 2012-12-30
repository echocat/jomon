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

import com.google.common.io.CountingInputStream;
import org.echocat.jomon.runtime.codec.Md5Utils;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

@ThreadSafe
public class ClassPathResource extends ResourceSupport implements NameEnabledResource, UriEnabledResource, PrivateUrlEnabledResource {

    private final String _path;
    private final ResourceType _type;
    private final ClassLoader _classLoader;
    private final Date _lastModified = new Date();
    private final String _classPath;

    private volatile byte[] _md5;
    private volatile Long _size;
    private volatile Boolean _existing;

    protected ClassPathResource(@Nonnull String path, @Nonnull ResourceType type, @Nonnull ClassLoader classLoader) {
        _path = path;
        _type = type;
        _classLoader = classLoader;
        _classPath = path.substring(1);
    }

    @Nonnull
    @Override
    public InputStream openInputStream() throws IOException {
        final InputStream inputStream = _classLoader.getResourceAsStream(_classPath);
        if (inputStream == null) {
            throw new FileNotFoundException("Could not find resource: " + _path);
        }
        return inputStream;
    }

    @Nonnull
    @Override
    public ResourceType getType() {
        return _type;
    }

    @Nonnull
    @Override
    public byte[] getMd5() throws IOException {
        if (_md5 == null) {
            initMd5AndSize();
        }
        return _md5;
    }

    @Override
    public long getSize() throws IOException {
        if (_size == null) {
            initMd5AndSize();
        }
        return _size;
    }

    @Override
    public Date getLastModified() throws IOException {
        return _lastModified;
    }

    @Override
    public boolean isExisting() throws IOException {
        if (_existing == null) {
            _existing = _classLoader.getResource(_path.substring(1)) != null;
        }
        return _existing;
    }

    protected void initMd5AndSize() throws IOException {
        try (final InputStream inputStream = openInputStream()) {
            try (final CountingInputStream countingInputStream = new CountingInputStream(inputStream)) {
                _md5 = Md5Utils.md5Of(countingInputStream).asBytes();
                _size = countingInputStream.getCount();
            }
        }
    }

    @Nonnull
    @Override
    public String getName() {
        return FilenameUtils.getName(_path);
    }

    @Nonnull
    @Override
    public String getUri() {
        return _path;
    }

    @Override
    public boolean isGenerated() throws IOException {
        return false;
    }

    @Nonnull
    @Override
    public URL getPrivateUrl() throws IOException {
        return new URL("classpath:" + _path);
    }

    @Nonnull
    public ClassLoader getClassLoader() {
        return _classLoader;
    }
}

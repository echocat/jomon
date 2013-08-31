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

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static java.io.File.separatorChar;
import static java.lang.System.getProperty;
import static java.util.UUID.randomUUID;
import static org.apache.commons.codec.binary.Hex.encodeHexString;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.moveFile;
import static org.apache.commons.io.IOUtils.closeQuietly;

@NotThreadSafe
public class FileResourceGenerator extends ResourceGenerator {
    
    private static final File TEMPORARY_BASE_DIRECTORY = new File(getProperty("java.io.tmpdir", "." + File.separator + "tmp"), FileResourceGenerator.class.getPackage().getName().replace('.', separatorChar));

    private final File _temporaryFile;

    private final OutputStream _outputStream;
    private final MD5OutputStream _md5OutputStream;

    public FileResourceGenerator(@Nonnull Resource originalResource) throws IOException {
        this(originalResource, null);
    }

    public FileResourceGenerator(@Nonnull Resource originalResource, @Nullable String name) throws IOException {
        this(originalResource.getType(), originalResource, name);
    }

    public FileResourceGenerator(@Nonnull ResourceType type) throws IOException {
        this(type, null);
    }

    public FileResourceGenerator(@Nonnull ResourceType type, @Nullable String name) throws IOException {
        this(type, null, name);
    }

    private FileResourceGenerator(@Nonnull ResourceType type, @Nullable Resource originalResource, @Nullable String name) throws IOException {
        super(type, originalResource, name);
        _temporaryFile = new File(TEMPORARY_BASE_DIRECTORY, randomUUID().toString());
        _temporaryFile.getParentFile().mkdirs();
        boolean success = false;
        _outputStream = new FileOutputStream(_temporaryFile);
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
    protected FileResource generateResource() throws IOException {
        final byte[] md5 = _md5OutputStream.getHash();
        final File targetFile = createTargetFileFor(md5, _temporaryFile.length(), getType());
        if (!targetFile.isFile() || targetFile.length() != _temporaryFile.length()) {
            deleteQuietly(targetFile);
            moveFile(_temporaryFile, targetFile);
        } else {
            deleteQuietly(_temporaryFile);
        }
        final FileResource result;
        if (getName() != null) {
            result = new NameEnabledFileResource(targetFile, md5, getType(), getName(), true);
        } else if (getOriginalResource() instanceof NameEnabledResource) {
            result = new NameEnabledFileResource(targetFile, md5, getType(), ((NameEnabledResource) getOriginalResource()).getName(), true);
        } else if (getOriginalResource() instanceof UriEnabledResource) {
            result = new NameEnabledFileResource(targetFile, md5, getType(), ((UriEnabledResource) getOriginalResource()).getUri(), true);
        } else {
            result = new FileResource(targetFile, md5, getType(), true);
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

    @Override
    public void drop() throws IOException {
        try {
            super.drop();
        } finally {
            _temporaryFile.delete();
        }
    }

    @Nonnull
    protected File createTargetFileFor(@Nonnull byte[] md5, @Nonnegative long size, @Nonnull ResourceType type) {
        final String md5AsHex = encodeHexString(md5);
        return new File(TEMPORARY_BASE_DIRECTORY, md5AsHex + "_" + size + "." + type);
    }
}

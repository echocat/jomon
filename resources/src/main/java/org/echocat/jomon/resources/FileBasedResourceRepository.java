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

import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;

import static java.io.File.separatorChar;
import static java.lang.System.getProperty;
import static org.apache.commons.codec.binary.Hex.encodeHexString;
import static org.apache.commons.io.FileUtils.forceMkdir;

public class FileBasedResourceRepository implements ResourceRepository<PropertiesEnabledResource<String>> {

    private static final File DEFAULT_BASE_DIRECTORY = new File(getProperty("java.io.tmpdir", "." + File.separator + "tmp"), FileBasedResourceRepository.class.getPackage().getName().replace('.', separatorChar));

    private volatile File _baseDirectory = DEFAULT_BASE_DIRECTORY;

    @Nonnull
    public File getBaseDirectory() {
        return _baseDirectory;
    }

    public void setBaseDirectory(@Nonnull File baseDirectory) {
        _baseDirectory = baseDirectory;
    }

    @Override
    public void save(@Nonnull Resource resource) throws Exception {
        final File targetFile = createTargetFileFor(resource);
        if (!targetFileIsTheSame(resource, targetFile) && (!targetFile.isFile() || targetFile.length() != resource.getSize())) {
            forceMkdir(targetFile.getParentFile());
            try (final InputStream inputStream = resource.openInputStream()) {
                try (final OutputStream outputStream = new FileOutputStream(targetFile)) {
                    IOUtils.copy(inputStream, outputStream);
                }
            }
            resource.release();
        }
    }

    protected boolean targetFileIsTheSame(@Nonnull Resource resource, @Nonnull File targetFile) throws IOException {
        final boolean result;
        if (resource instanceof FileResource) {
            result = ((FileResource) resource).getFile().getCanonicalFile().equals(targetFile.getCanonicalFile());
        } else {
            result = false;
        }
        return result;
    }

    @Override
    @Nullable
    public PropertiesEnabledResource<String> findResourceBy(@Nonnull byte[] md5, @Nonnegative long size, @Nonnull ResourceType type) {
        final FileResource result;
        final File targetFile = createTargetFileFor(md5, size, type);
        if (targetFile.isFile()) {
            result = new FileResource(targetFile, md5, type, false);
        } else {
            result = null;
        }
        return result;
    }

    @Nonnull
    protected File createTargetFileFor(@Nonnull Resource resource) {
        try {
            return createTargetFileFor(resource.getMd5(), resource.getSize(), resource.getType());
        } catch (IOException e) {
            throw new RuntimeException("Could not get target file of " + resource + ".", e);
        }
    }

    @Nonnull
    protected File createTargetFileFor(@Nonnull byte[] md5, @Nonnegative long size, @Nonnull ResourceType type) {
        final String md5AsHex = encodeHexString(md5);
        final StringBuilder sb = new StringBuilder();
        sb.append(md5AsHex.charAt(0)).append(File.separatorChar);
        sb.append(md5AsHex.charAt(1)).append(File.separatorChar);
        sb.append(md5AsHex.charAt(2)).append(File.separatorChar);
        sb.append(md5AsHex.charAt(3)).append(File.separatorChar);
        sb.append(md5AsHex).append('_').append(size).append('.').append(type);
        return new File(_baseDirectory, sb.toString());
    }
}

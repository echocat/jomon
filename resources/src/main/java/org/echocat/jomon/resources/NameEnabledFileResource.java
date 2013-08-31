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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.io.*;

import static org.apache.commons.io.FileUtils.forceMkdir;

@ThreadSafe
public class NameEnabledFileResource extends FileResource implements NameEnabledResource {

    private String _name;
    private boolean _nameLoaded;

    public NameEnabledFileResource(@Nonnull File file, @Nonnull byte[] md5, @Nonnull ResourceType type, boolean generated) {
        super(file, md5, type, generated);
    }

    public NameEnabledFileResource(@Nonnull File file, @Nonnull byte[] md5, @Nonnull ResourceType type, @Nonnull String name, boolean generated) throws IOException {
        this(file, md5, type, generated);
        setName(name);
    }

    @Nonnull
    @Override
    public String getName() throws IOException {
        synchronized (this) {
            if (!_nameLoaded) {
                final File nameFile = getNameFile();
                if (nameFile.isFile()) {
                    try (final InputStream is = new FileInputStream(nameFile)) {
                        try (final Reader reader = new InputStreamReader(is, "UTF-8")) {
                            _name = IOUtils.toString(reader);
                        }
                    }
                } else {
                    _name = null;
                }
                _nameLoaded = true;
            }
            return _name;
        }
    }

    public void setName(String name) throws IOException {
        synchronized (this) {
            _name = name;
            _nameLoaded = true;
            final File nameFile = getNameFile();
            if (name == null) {
                if (nameFile.exists() && !nameFile.delete()) {
                    throw new IOException("Could not delete the old and not longer needed name file: " + nameFile);
                }
            } else {
                forceMkdir(nameFile.getParentFile());
                try (final OutputStream os = new FileOutputStream(nameFile)) {
                    try (final Writer writer = new OutputStreamWriter(os, "UTF-8")) {
                        writer.write(name);
                    }
                }
            }
        }
    }

    @Override
    public void release() throws IOException {
        try {
            super.release();
        } finally {
            getNameFile().delete();
        }
    }

    @Nonnull
    public File getNameFile() {
        return new File(getFile().getPath() + ".name");
    }
}

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

import org.echocat.jomon.runtime.iterators.CloseableIterator;
import org.echocat.jomon.runtime.iterators.ConvertingIterator;
import org.echocat.jomon.runtime.util.Entry;
import org.echocat.jomon.runtime.util.Entry.Impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.System.currentTimeMillis;
import static org.echocat.jomon.runtime.codec.Md5Utils.md5Of;
import static org.echocat.jomon.runtime.iterators.IteratorUtils.emptyCloseableIterator;
import static org.apache.commons.io.FileUtils.forceMkdir;
import static org.apache.commons.io.IOUtils.closeQuietly;

@ThreadSafe
public class FileResource extends ResourceSupport implements PropertiesEnabledResource<String>, LoggingEnabledResource, FileEnabledResource, PrivateUrlEnabledResource {

    private final File _file;
    private final ResourceType _type;
    private final boolean _generated;

    private volatile byte[] _md5;
    private volatile Long _size;
    private Properties _properties;

    public FileResource(@Nonnull File file, @Nonnull ResourceType type) {
        this(file, type, false);
    }

    public FileResource(@Nonnull File file, @Nullable byte[] md5, @Nonnull ResourceType type) {
        this(file, md5, type, false);
    }

    public FileResource(@Nonnull File file, @Nonnull ResourceType type, boolean generated) {
        this(file, null, type, generated);
    }

    public FileResource(@Nonnull File file, @Nullable byte[] md5, @Nonnull ResourceType type, boolean generated) {
        _file = file;
        _md5 = md5;
        _type = type;
        _generated = generated;
    }

    @Nonnull
    @Override
    public InputStream openInputStream() throws IOException {
        return new FileInputStream(_file);
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
            _md5 = md5Of(_file).asBytes();
        }
        return _md5;
    }

    @Override
    public long getSize() {
        if (_size == null) {
            _size = _file.length();
        }
        return _size;
    }

    @Override
    public Date getLastModified() {
        return new Date(_file.lastModified());
    }

    @Override
    public boolean isExisting() throws IOException {
        return _file.isFile();
    }

    @Override
    public void release() throws IOException {
        if (_generated) {
            _file.delete();
            getPropertiesFile().delete();
        }
    }

    @Nonnull
    public File getPropertiesFile() {
        return new File(_file.getPath() + ".properties");
    }

    @Nonnull
    protected Properties getPropertiesInternal() throws IOException {
        if (_properties == null) {
            final Properties properties = new Properties();
            final File propertiesFile = getPropertiesFile();
            if (propertiesFile.isFile()) {
                try (final InputStream is = new FileInputStream(propertiesFile)) {
                    try (final Reader reader = new InputStreamReader(is, "UTF-8")) {
                        properties.load(reader);
                    }
                }
            }
            _properties = properties;
        }
        return _properties;
    }

    protected void saveProperties(@Nonnull Properties properties) throws IOException {
        final File propertiesFile = getPropertiesFile();
        if (properties.isEmpty()) {
            if (propertiesFile.exists() && !propertiesFile.delete()) {
                throw new IOException("Could not delete the old and not longer needed properties file: " + propertiesFile);
            }
        } else {
            forceMkdir(propertiesFile.getParentFile());
            try (final OutputStream os = new FileOutputStream(propertiesFile)) {
                try (final Writer writer = new OutputStreamWriter(os, "UTF-8")) {
                    properties.store(writer, null);
                }
            }
        }
    }

    @Nonnull
    @Override
    public Iterable<Entry<String, String>> getProperties() throws IOException {
        final Map<String, String> copy;
        synchronized (this) {
            // noinspection unchecked, RedundantCast
            copy = new HashMap<>((Map<String, String>)(Object)getPropertiesInternal());
        }
        return new Iterable<Entry<String, String>>() { @Override public Iterator<Entry<String, String>> iterator() {
            return new ConvertingIterator<Map.Entry<String, String>, Entry<String, String>>(copy.entrySet().iterator()) {
                private final AtomicReference<String> _currentKey = new AtomicReference<>();

                @Override protected Entry<String, String> convert(Map.Entry<String, String> input) {
                    _currentKey.set(input.getKey());
                   return new Impl<>(input.getKey(), input.getValue());
                }

                @Override public void remove() {
                    final String key = _currentKey.get();
                    if (key != null) {
                        try {
                            removeProperty(key);
                        } catch (final IOException e) {
                            throw new RuntimeException("Could not remove " + key + ".", e);
                        }
                    }
                }
            };
        }};
    }

    @Override
    public String getProperty(@Nonnull String name) throws IOException {
        synchronized (this) {
            return getPropertiesInternal().getProperty(name);
        }
    }

    @Override
    public void setProperty(@Nonnull String name, @Nullable Object value) throws IOException {
        synchronized (this) {
            final Properties properties = getPropertiesInternal();
            if (value != null) {
                properties.put(name, value);
            } else {
                properties.remove(name);
            }
            saveProperties(properties);
        }
    }

    @Override
    public void removeProperty(@Nonnull String name) throws IOException {
        synchronized (this) {
            final Properties properties = getPropertiesInternal();
            properties.remove(name);
            saveProperties(properties);
        }
    }

    @Nonnull
    @Override
    public Class<String> getPropertyValueType() {
        return String.class;
    }

    @Override
    @Nonnull
    public File getFile() {
        return _file;
    }

    @Nonnull
    public File getLoggingFile() {
        return new File(_file.getPath() + ".log");
    }

    @Override
    public void logMessage(@Nonnull String message) throws IOException {
        try (final OutputStream os = new FileOutputStream(getLoggingFile(), true)) {
            try (final Writer writer = new OutputStreamWriter(os, "UTF-8")) {
                writer.write(message.replace("\\", "\\\\").replace("\n", "\\n"));
                writer.write('\n');
                writer.flush();
            }
        }
    }

    @Nonnull
    @Override
    public CloseableIterator<String> logMessageIterator() throws IOException {
        final CloseableIterator<String> result;
        if (getLoggingFile().isFile()) {
            boolean success = false;
            final InputStream is = new FileInputStream(getLoggingFile());
            try {
                final Reader reader = new InputStreamReader(is, "UTF-8");
                try {
                    final BufferedReader bufferedReader = new BufferedReader(reader);
                    try {
                        success = true;
                        final String firstLine = readNextMessageOf(bufferedReader);

                        result = new CloseableIterator<String>() {

                            private String _line = firstLine;

                            @Override
                            public void close() {
                                try {
                                    closeQuietly(bufferedReader);
                                } finally {
                                    try {
                                        closeQuietly(reader);
                                    } finally {
                                        closeQuietly(is);
                                    }
                                }
                            }

                            @Override
                            public boolean hasNext() {
                                return _line != null;
                            }

                            @Override
                            public String next() {
                                final String result = _line;
                                if (result == null) {
                                    throw new NoSuchElementException();
                                }
                                _line = readNextMessageOf(bufferedReader);
                                return result;
                            }

                            @Override public void remove() { throw new UnsupportedOperationException(); }
                        };
                    } finally {
                        if (!success) {
                            closeQuietly(bufferedReader);
                        }
                    }
                } finally {
                    if (!success) {
                        closeQuietly(reader);
                    }
                }
            } finally {
                if (!success) {
                    closeQuietly(is);
                }
            }
        } else {
            result = emptyCloseableIterator();
        }
        return result;
    }

    @Nullable
    protected String readNextMessageOf(@Nonnull BufferedReader bufferedReader) {
        try {
            final String line = bufferedReader.readLine();
            return line != null ? line.replace("\\n", "\n").replace("\\\\", "\\") : null;
        } catch (final IOException e) {
            throw new RuntimeException("Could not read next line.", e);
        }
    }

    @Override
    public boolean isGenerated() throws IOException {
        return _generated;
    }

    @Nonnull
    @Override
    public URL getPrivateUrl() throws IOException {
        return _file.toURI().toURL();
    }

    @Override
    public void touch() throws IOException {
        _file.setLastModified(currentTimeMillis());
    }
}

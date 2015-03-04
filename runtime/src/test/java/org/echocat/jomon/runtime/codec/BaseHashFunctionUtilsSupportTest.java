/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012-2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.runtime.codec;

import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.Charset.forName;

public abstract class BaseHashFunctionUtilsSupportTest {

    @Nonnull
    protected static final Charset CHARSET = forName("UTF-8");
    protected static final String TEST_CONTENT_1 = "This is a test!";
    protected static final String TEST_CONTENT_2 = "This is also a test!";
    protected static final byte[] TEST_BYTES_1 = TEST_CONTENT_1.getBytes(CHARSET);
    protected static final byte[] TEST_BYTES_2 = TEST_CONTENT_2.getBytes(CHARSET);

    @Rule
    public final TemporaryFolder _folder = new TemporaryFolder();

    @Nonnull
    protected InputStream streamOf(@Nonnull String fileName) throws IOException {
        final InputStream result = BaseHashFunctionUtilsSupportTest.class.getResourceAsStream(fileName);
        if (result == null) {
            throw new FileNotFoundException("Could not find file '" + fileName + "' in classpath.");
        }
        return result;
    }

    @Nonnull
    protected File testFileOf(@Nonnull String fileName) throws IOException {
        final File result = _folder.newFile(fileName);
        try (final InputStream is = streamOf(fileName)) {
            try (final OutputStream os = new FileOutputStream(result)) {
                IOUtils.copy(is, os);
            }
        }
        return result;
    }

    protected static class HashFunctionImpl extends HashFunctionSupport<HashFunctionImpl> {

        @Nonnull
        private final byte[] _hash;
        @Nonnull
        private final List<String> _updates = new ArrayList<>();

        public HashFunctionImpl(@Nonnull byte[] hash) {
            _hash = hash;
        }

        public HashFunctionImpl() {
            this(TEST_BYTES_1);
        }

        @Nonnull
        @Override
        public HashFunctionImpl update(@Nullable byte[] with, @Nonnegative int offset, @Nonnegative int length) {
            _updates.add(new String(with, offset, length, CHARSET));
            return this;
        }

       @Nonnull
        @Override
        public byte[] asBytes() {
            return _hash;
        }

        @Nonnull
        public List<String> getContents() {
            return _updates;
        }

    }

}
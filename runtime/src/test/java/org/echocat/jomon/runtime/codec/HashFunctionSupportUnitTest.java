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

import org.junit.Test;

import java.io.InputStream;
import java.util.Arrays;

import static org.apache.commons.codec.binary.Base64.encodeBase64;
import static org.echocat.jomon.runtime.CollectionUtils.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class HashFunctionSupportUnitTest extends BaseHashFunctionUtilsSupportTest {

    @Test
    public void updateByte() throws Exception {
        final HashFunctionImpl function = new HashFunctionImpl();
        function.update((byte)'X');
        function.update((byte)'Y');
        assertThat(function.getContents(), is(asList("X", "Y")));
    }

    @Test
    public void updateBytes() throws Exception {
        final HashFunctionImpl function = new HashFunctionImpl();
        function.update(TEST_BYTES_1);
        function.update(TEST_BYTES_2);
        assertThat(function.getContents(), is(asList(TEST_CONTENT_1, TEST_CONTENT_2)));
    }

    @Test
    public void updateByteRanges() throws Exception {
        final HashFunctionImpl function = new HashFunctionImpl();
        function.update(TEST_BYTES_1, 1, 4);
        function.update(TEST_BYTES_2, 0, TEST_BYTES_2.length);
        assertThat(function.getContents(), is(asList(TEST_CONTENT_1.substring(1, 5), TEST_CONTENT_2)));
    }

    @Test
    public void updateString() throws Exception {
        final HashFunctionImpl function = new HashFunctionImpl();
        function.update(TEST_CONTENT_1);
        function.update(TEST_CONTENT_2);
        assertThat(function.getContents(), is(asList(TEST_CONTENT_1, TEST_CONTENT_2)));
    }

    @Test
    public void updateStringWithCharset() throws Exception {
        final HashFunctionImpl function = new HashFunctionImpl();
        function.update(TEST_CONTENT_1, CHARSET);
        function.update(TEST_CONTENT_2, CHARSET);
        assertThat(function.getContents(), is(asList(TEST_CONTENT_1, TEST_CONTENT_2)));
    }

    @Test
    public void updateInputStream() throws Exception {
        final HashFunctionImpl function = new HashFunctionImpl();
        try (final InputStream is = streamOf("testFile1.txt")) {
            function.update(is);
        }
        try (final InputStream is = streamOf("testFile2.txt")) {
            function.update(is);
        }
        assertThat(function.getContents(), is(asList(TEST_CONTENT_1, TEST_CONTENT_2)));
    }

    @Test
    public void updateFile() throws Exception {
        final HashFunctionImpl function = new HashFunctionImpl();
        function.update(testFileOf("testFile1.txt"));
        function.update(testFileOf("testFile2.txt"));
        assertThat(function.getContents(), is(asList(TEST_CONTENT_1, TEST_CONTENT_2)));
    }

    @Test
    public void asBase64() throws Exception {
        final HashFunctionImpl function = new HashFunctionImpl();
        assertThat(Arrays.equals(function.asBase64(), encodeBase64(TEST_BYTES_1)), is(true));
        assertThat(Arrays.equals(function.asBase64(), "VGhpcyBpcyBhIHRlc3Qh".getBytes(CHARSET)), is(true));
    }

    @Test
    public void asHexString() throws Exception {
        final HashFunctionImpl function = new HashFunctionImpl();
        assertThat(function.asHexString(), is(HashFunctionUtils.asHexString(TEST_BYTES_1)));
        assertThat(function.asHexString(), is("546869732069732061207465737421"));
    }

    @Test
    public void asBase64String() throws Exception {
        final HashFunctionImpl function = new HashFunctionImpl();
        assertThat(function.asBase64String(), is(new String(encodeBase64(TEST_BYTES_1), CHARSET)));
        assertThat(function.asBase64String(), is("VGhpcyBpcyBhIHRlc3Qh"));
    }

}
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

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.Charset;

import static org.echocat.jomon.runtime.codec.HashFunctionUtils.newInstanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public abstract class HashFunctionUtilsTestSupport extends BaseHashFunctionUtilsSupportTest {

    @Nonnull
    private final Class<?> _utilsType;
    @Nonnull
    private final Constructor<? extends HashFunction> _constructor;
    @Nonnull
    private final String _methodName;

    protected HashFunctionUtilsTestSupport(@Nonnull Class<?> utilsType, @Nonnull Class<? extends HashFunction> hashFunctionType) {
        _utilsType = utilsType;
        try {
            _constructor = hashFunctionType.getConstructor();
        } catch (final NoSuchMethodException e) {
            throw new RuntimeException("Could not find required constructor of " + hashFunctionType.getName() + ".", e);
        }
        final String simpleName = _utilsType.getSimpleName();
        final String simpleNameWithoutUtilsInIt = simpleName.replace("Utils", "");
        _methodName = StringUtils.uncapitalize(simpleNameWithoutUtilsInIt) + "Of";
    }

    @Test
    public void updateByte() throws Exception {
        final Method method = _utilsType.getDeclaredMethod(_methodName, byte.class);
        final HashFunction function = newInstanceOf(_constructor);
        assertThat(method.invoke(null, (byte) 'X'), is((Object) function.update((byte) 'X')));
    }

    @Test
    public void updateBytes() throws Exception {
        final Method method = _utilsType.getDeclaredMethod(_methodName, byte[].class);
        final HashFunction function = newInstanceOf(_constructor);
        assertThat(method.invoke(null, TEST_BYTES_1), is((Object) function.update(TEST_BYTES_1)));
    }

    @Test
    public void updateByteRanges() throws Exception {
        final Method method = _utilsType.getDeclaredMethod(_methodName, byte[].class, int.class, int.class);
        final HashFunction function = newInstanceOf(_constructor);
        assertThat(method.invoke(null, TEST_BYTES_1, 1, 4), is((Object) function.update(TEST_BYTES_1, 1, 4)));
    }

    @Test
    public void updateString() throws Exception {
        final Method method = _utilsType.getDeclaredMethod(_methodName, String.class);
        final HashFunction function = newInstanceOf(_constructor);
        assertThat(method.invoke(null, TEST_CONTENT_1), is((Object) function.update(TEST_CONTENT_1)));
    }

    @Test
    public void updateStringWithCharset() throws Exception {
        final Method method = _utilsType.getDeclaredMethod(_methodName, String.class, Charset.class);
        final HashFunction function = newInstanceOf(_constructor);
        assertThat(method.invoke(null, TEST_CONTENT_1, CHARSET), is((Object) function.update(TEST_CONTENT_1, CHARSET)));
    }

    @Test
    public void updateInputStream() throws Exception {
        final Method method = _utilsType.getDeclaredMethod(_methodName, InputStream.class);
        final HashFunction function = newInstanceOf(_constructor);
        try (final InputStream is1 = streamOf("testFile1.txt")) {
            try (final InputStream is2 = streamOf("testFile1.txt")) {
                assertThat(method.invoke(null, is1), is((Object) function.update(is2)));
            }
        }
    }

    @Test
    public void updateFile() throws Exception {
        final Method method = _utilsType.getDeclaredMethod(_methodName, File.class);
        final HashFunction function = newInstanceOf(_constructor);
        final File file = testFileOf("testFile1.txt");
        assertThat(method.invoke(null, file), is((Object) function.update(file)));
    }

}
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

import javax.annotation.Nonnull;
import java.security.Provider;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JvmMessageDigestBasedHashFunctionSupportUnitTest {

    protected static final String TEST_STRING = "Test";
    protected static final String TEST_MD5_HEX = "0cbc6611f5540bd0809a388dc95a615b";

    @Test
    public void initialization() throws  Exception {
        assertThat(new HashFunctionImpl("MD5").update(TEST_STRING).asHexString(), is(TEST_MD5_HEX));
    }

    protected static class HashFunctionImpl extends JvmMessageDigestBasedHashFunctionSupport<HashFunctionImpl> {

        public HashFunctionImpl(@Nonnull String algorithm) {
            super(algorithm);
        }

        public HashFunctionImpl(@Nonnull String algorithm, @Nonnull String provider) {
            super(algorithm, provider);
        }

        public HashFunctionImpl(@Nonnull String algorithm, @Nonnull Provider provider) {
            super(algorithm, provider);
        }

    }

}
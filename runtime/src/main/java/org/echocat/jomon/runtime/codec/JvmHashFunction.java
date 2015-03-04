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

import javax.annotation.Nonnull;
import java.security.Provider;

public class JvmHashFunction extends JvmMessageDigestBasedHashFunctionSupport<JvmHashFunction> {

    public JvmHashFunction(@Nonnull String algorithm) {
        super(algorithm);
    }

    public JvmHashFunction(@Nonnull String algorithm, @Nonnull String provider) {
        super(algorithm, provider);
    }

    public JvmHashFunction(@Nonnull String algorithm, @Nonnull Provider provider) {
        super(algorithm, provider);
    }

}

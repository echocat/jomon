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

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.util.NoSuchElementException;

public abstract class JvmMessageDigestBasedHashFunctionSupport<T extends JvmMessageDigestBasedHashFunctionSupport<T>> extends HashFunctionSupport<T> {

    private final MessageDigest _delegate;

    protected JvmMessageDigestBasedHashFunctionSupport(@Nonnull String algorithm) {
        try {
            _delegate = MessageDigest.getInstance(algorithm);
        } catch (final NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException("Could not create an instance of " + algorithm + " because it is not supported by JVM.", e);
        }
    }

    protected JvmMessageDigestBasedHashFunctionSupport(@Nonnull String algorithm, @Nonnull String provider) {
        try {
            _delegate = MessageDigest.getInstance(algorithm, provider);
        } catch (final NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException("Could not create an instance of " + algorithm + " because it is not supported by JVM.", e);
        } catch (final NoSuchProviderException e) {
            final NoSuchElementException exception = new NoSuchElementException("Could not create an instance of " + algorithm + " because provider " + provider + " could not be found.");
            exception.initCause(e);
            throw exception;
        }
    }

    protected JvmMessageDigestBasedHashFunctionSupport(@Nonnull String algorithm, @Nonnull Provider provider) {
        try {
            _delegate = MessageDigest.getInstance(algorithm, provider);
        } catch (final NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException("Could not create an instance of " + algorithm + " because it is not supported by JVM.", e);
        }
    }

    @Override
    @Nonnull
    public T update(@Nullable byte[] with, @Nonnegative int offset, @Nonnegative int length) {
        if (with != null) {
            _delegate.update(with, offset, length);
        }
        return thisObject();
    }

    @Nonnull
    @Override
    public byte[] asBytes() {
        return _delegate.digest();
    }

}

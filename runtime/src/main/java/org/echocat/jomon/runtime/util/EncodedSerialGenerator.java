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

package org.echocat.jomon.runtime.util;

import org.apache.commons.codec.binary.Base64;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

import static javax.crypto.Cipher.DECRYPT_MODE;
import static javax.crypto.Cipher.ENCRYPT_MODE;

public class EncodedSerialGenerator implements SerialGenerator<String> {

    private static final String ALGORITHM = "AES";

    private final SerialGenerator<Long> _longBasedSerialGenerator;

    private String _password = "gk9s4(03dfj21S/F";
    private int _creationRetries = 10;

    private Cipher _encryptCipher;
    private Cipher _decryptCipher;

    public EncodedSerialGenerator(@Nonnull SerialGenerator<Long> longBasedSerialGenerator) throws Exception {
        _longBasedSerialGenerator = longBasedSerialGenerator;
        init();
    }
    
    @PostConstruct
    public void init() throws Exception {
       final Key key = new SecretKeySpec(_password.getBytes("UTF-8"), ALGORITHM);
        _encryptCipher = Cipher.getInstance(ALGORITHM);
        _encryptCipher.init(ENCRYPT_MODE, key);
        _decryptCipher = Cipher.getInstance(ALGORITHM);
        _decryptCipher.init(DECRYPT_MODE, key);
    }

    @Nonnull
    public String getPassword() {
        return _password;
    }

    public void setPassword(@Nonnull String password) {
        _password = password;
    }

    @Nonnegative
    public int getCreationRetries() {
        return _creationRetries;
    }

    public void setCreationRetries(@Nonnegative int creationRetries) {
        _creationRetries = creationRetries;
    }

    @Nonnull
    @Override
    public String next() {
        String encodedSerial;
        int currentTry = 0;
        do {
            try {
                encodedSerial = nextInternal();
            } catch (RuntimeException e) {
                encodedSerial = null;
                if (currentTry++ >= _creationRetries) {
                    throw e;
                }
            }
        } while (encodedSerial == null);
        return encodedSerial;
    }

    @Nonnull
    protected String nextInternal() {
        final Long serial = _longBasedSerialGenerator.next();
        final String encodedSerial = encode(serial);
        final long recoveredSerial = decode(encodedSerial);
        if (recoveredSerial != serial) {
            throw new IllegalStateException("The encryption/decryption of the serial fails.");
        }
        return encodedSerial;
    }
    
    @Nonnull
    public String encode(long serial) {
        final byte[] serialAsBytes = asBytes(serial);
        final byte[] encrypted= encrypt(serialAsBytes);
        return base64Encode(encrypted);
    }

    @Nonnull
    protected String base64Encode(@Nonnull byte[] toEncode) {
        return createBase64().encodeToString(toEncode);
    }

    @Nonnull
    protected byte[] encrypt(@Nonnull byte[] toEncrypt) {
        final byte[] encoded;
        try {
            encoded = _encryptCipher.doFinal(toEncrypt);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException("Could not create serial.", e);
        }
        return encoded;
    }

    @Nonnull
    protected byte[] asBytes(long val) {
        final byte[] b = new byte[8];
        b[7] = (byte) (val       );
        b[6] = (byte) (val >>>  8);
        b[5] = (byte) (val >>> 16);
        b[4] = (byte) (val >>> 24);
        b[3] = (byte) (val >>> 32);
        b[2] = (byte) (val >>> 40);
        b[1] = (byte) (val >>> 48);
        b[0] = (byte) (val >>> 56);
        return b;
    }
    
    public long decode(@Nonnull String encodedSerial) {
        final byte[] encrypted = base64decode(encodedSerial);
        final byte[] serialAsBytes = decrypt(encrypted);
        return asLong(serialAsBytes);
    }

    @Nonnull
    protected byte[] base64decode(@Nonnull String toDecode) {
        return createBase64().decode(toDecode);
    }

    @Nonnull
    protected byte[] decrypt(@Nonnull byte[] toDecrypt) {
        final byte[] encoded;
        try {
            encoded = _decryptCipher.doFinal(toDecrypt);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException("Could not create serial.", e);
        }
        return encoded;
    }

    @Nonnull
    protected long asLong(byte[] b) {
        return ((b[7] & 0xFFL)      ) +
               ((b[6] & 0xFFL) <<  8) +
               ((b[5] & 0xFFL) << 16) +
               ((b[4] & 0xFFL) << 24) +
               ((b[3] & 0xFFL) << 32) +
               ((b[2] & 0xFFL) << 40) +
               ((b[1] & 0xFFL) << 48) +
               (((long) b[0])  << 56);
    }

    @Nonnull
    @Override
    public Class<String> getGeneratedType() {
        return String.class;
    }

    @Nonnull
    protected Base64 createBase64() {
        return new Base64(0, null, true);
    }
}

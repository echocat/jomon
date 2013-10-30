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

package com.jcraft.jsch;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

@SuppressWarnings({"ParameterHidesMemberVariable", "DuplicateThrows"})
public class NotifyingIO extends IO {

    @Nonnull
    private final IO _original;
    @Nullable
    private volatile EventConsumer _eventConsumer;

    public NotifyingIO(@Nonnull IO original) {
        _original = original;
    }

    @Override
    public void setOutputStream(OutputStream out) {_original.setOutputStream(out);}

    @Override
    public void setOutputStream(OutputStream out, boolean dontclose) {_original.setOutputStream(out, dontclose);}

    @Override
    public void setExtOutputStream(OutputStream out) {_original.setExtOutputStream(out);}

    @Override
    public void setExtOutputStream(OutputStream out, boolean dontclose) {_original.setExtOutputStream(out, dontclose);}

    @Override
    public void setInputStream(InputStream in) {_original.setInputStream(in);}

    @Override
    public void setInputStream(InputStream in, boolean dontclose) {_original.setInputStream(in, dontclose);}

    @Override
    public void put(Packet p) throws IOException, SocketException {_original.put(p);}

    @Override
    public void put(byte[] array, int begin, int length) throws IOException {_original.put(array, begin, length);}

    @Override
    public void put_ext(byte[] array, int begin, int length) throws IOException {_original.put_ext(array, begin, length);}

    @Override
    public int getByte() throws IOException {return _original.getByte();}

    @Override
    public void getByte(byte[] array) throws IOException {_original.getByte(array);}

    @Override
    public void getByte(byte[] array, int begin, int length) throws IOException {_original.getByte(array, begin, length);}

    @Override
    void out_close() {
        try {
            _original.out_close();
        } finally {
            final EventConsumer consumer = _eventConsumer;
            if (consumer != null) {
                consumer.onClose();
            }
        }
    }

    @Override
    public void close() {
        try {
            _original.close();
        } finally {
            final EventConsumer consumer = _eventConsumer;
            if (consumer != null) {
                consumer.onClose();
            }
        }
    }

    public void setEventConsumer(@Nullable EventConsumer eventConsumer) {
        _eventConsumer = eventConsumer;
    }

    public static interface EventConsumer {

        public void onClose();

    }

}

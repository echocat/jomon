/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.net.cluster.channel;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public class ByteUtils {

    private ByteUtils() {}

    public static void putShort(@Nonnull byte[] b, @Nonnegative int off, short val) {
        b[off + 1] = (byte) (val      );
        b[off    ] = (byte) (val >>> 8);
    }

    public static short getShort(@Nonnull byte[] b, @Nonnegative int off) {
        return (short) ((b[off + 1] & 0xFF) +
                        (b[off] << 8));
    }

    public static int getInt(@Nonnull byte[] b, @Nonnegative int off) {
        return ((b[off + 3] & 0xFF)      ) +
               ((b[off + 2] & 0xFF) <<  8) +
               ((b[off + 1] & 0xFF) << 16) +
               ((b[off    ]       ) << 24);
    }

    public static void putInt(@Nonnull byte[] b, @Nonnegative int off, int val) {
        b[off + 3] = (byte) (val       );
        b[off + 2] = (byte) (val >>>  8);
        b[off + 1] = (byte) (val >>> 16);
        b[off    ] = (byte) (val >>> 24);
    }

    public static long getLong(@Nonnull byte[] b, @Nonnegative int off) {
        return ((b[off + 7] & 0xFFL)      ) +
               ((b[off + 6] & 0xFFL) <<  8) +
               ((b[off + 5] & 0xFFL) << 16) +
               ((b[off + 4] & 0xFFL) << 24) +
               ((b[off + 3] & 0xFFL) << 32) +
               ((b[off + 2] & 0xFFL) << 40) +
               ((b[off + 1] & 0xFFL) << 48) +
               (((long) b[off])      << 56);
    }

    public static void putLong(@Nonnull byte[] b, @Nonnegative int off, long val) {
        b[off + 7] = (byte) (val       );
        b[off + 6] = (byte) (val >>>  8);
        b[off + 5] = (byte) (val >>> 16);
        b[off + 4] = (byte) (val >>> 24);
        b[off + 3] = (byte) (val >>> 32);
        b[off + 2] = (byte) (val >>> 40);
        b[off + 1] = (byte) (val >>> 48);
        b[off    ] = (byte) (val >>> 56);
    }
}

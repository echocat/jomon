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

package org.echocat.jomon.net.dns;

import org.xbill.DNS.*;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;

import static org.echocat.jomon.net.dns.RecordUtils.ns;
import static org.echocat.jomon.net.dns.RecordUtils.soa;

public class ZoneUtils {

    @Nonnull
    public static Zone zone(@Nonnull String name, @Nonnegative long serial, @Nullable Record... records) {
        return zone(name, soa(name, name, serial), ns(name, "ns1." + name), records);
    }

    @Nonnull
    public static Zone zone(@Nonnull String name, @Nonnull SOARecord soa, @Nonnull NSRecord nsRecord, @Nullable Record... records) {
        final Record[] target = new Record[(records != null ? records.length : 0) + 2];
        target[0] = soa;
        target[1] = nsRecord;
        if (records != null) {
            for (int i = 0; i < records.length; i++) {
                target[i + 2] = records[i];
            }
        }
        return zone(Name.fromConstantString(name), target);
    }

    @Nonnull
    public static Zone zone(@Nonnull Name name, @Nullable Record... records) {
        try {
            return new Zone(name, records != null ? records : new Record[0]);
        } catch (final IOException e) {
            throw new IllegalArgumentException("Could not create a zone from " + name + " and " + Arrays.toString(records) + ".", e);
        }
    }

    private ZoneUtils() {}

}

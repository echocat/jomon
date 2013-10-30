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

package org.echocat.jomon.net;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.NetworkInterface;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

public enum NetworkInterfaceType {
    loopBack("lo"),
    other(null);

    private final Pattern _namePattern;

    NetworkInterfaceType(@Nullable String namePattern) {
        _namePattern = namePattern != null ? compile(namePattern) : null;
    }

    @Nonnull
    public static NetworkInterfaceType typeOf(@Nonnull NetworkInterface what) {
        final String name = what.getName();
        NetworkInterfaceType result = other;
        for (NetworkInterfaceType current : values()) {
            final Pattern pattern = current._namePattern;
            if (pattern != null && pattern.matcher(name).matches()) {
                result = current;
                break;
            }
        }
        return result;
    }

}

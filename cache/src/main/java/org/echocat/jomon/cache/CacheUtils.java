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

package org.echocat.jomon.cache;

import javax.annotation.Nullable;

import static java.lang.Character.isLetterOrDigit;

public class CacheUtils {

    public static boolean isValidCacheId(@Nullable String id) {
        boolean result;
        if (id == null || id.isEmpty()) {
            result = false;
        } else {
            result = true;
            for (char c : id.toCharArray()) {
                if (!isLetterOrDigit(c) && c != '.' && c != '-' && c != '_' && c != ':') {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    public static void assertValidCacheId(@Nullable String id) {
        if (!isValidCacheId(id)) {
            throw new IllegalArgumentException("The provided it is not usable as cache id: " + id);
        }
    }

    private CacheUtils() {}
}

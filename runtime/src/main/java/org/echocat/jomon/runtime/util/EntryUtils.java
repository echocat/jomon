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

package org.echocat.jomon.runtime.util;

import org.echocat.jomon.runtime.util.Entry.Impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntryUtils {

    @Nonnull
    public static <K, V> Entry<K, V> entry(@Nullable K key, @Nullable V value) {
        return new Impl<>(key, value);
    }
    
    private EntryUtils() {}
}

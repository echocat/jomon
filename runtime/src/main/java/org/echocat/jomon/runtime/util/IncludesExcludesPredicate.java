/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012-2014 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.runtime.util;

import javax.annotation.Nonnull;

public class IncludesExcludesPredicate<V> extends BaseIncludesExcludesPredicate<V, V, IncludesExcludesPredicate<V>> {

    @Nonnull
    public static <V> IncludesExcludesPredicate<V> includesExcludesPredicate() {
        return new IncludesExcludesPredicate<>();
    }

    @Nonnull
    public static <V> IncludesExcludesPredicate<V> filteringPredicate() {
        return includesExcludesPredicate();
    }

    @Nonnull
    public static <V> IncludesExcludesPredicate<V> predicate() {
        return includesExcludesPredicate();
    }

}

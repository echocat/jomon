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

package org.echocat.jomon.cache.management;

import org.echocat.jomon.cache.Cache;

import javax.annotation.Nonnull;

public abstract class CacheCreatorSupport implements CacheCreator {

    public boolean canHandleType(@Nonnull Class<? extends Cache<?, ?>> type) throws Exception {
        return false;
    }

    @Override
    public boolean canHandleType(@Nonnull CacheDefinition<?, ?, ?> by) throws Exception {
        return canHandleType(by.getRequiredType());
    }
}

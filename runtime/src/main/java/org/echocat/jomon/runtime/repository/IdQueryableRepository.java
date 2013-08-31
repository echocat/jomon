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

package org.echocat.jomon.runtime.repository;

import org.echocat.jomon.runtime.iterators.CloseableIterator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public interface IdQueryableRepository<Q extends Query, ID> extends Repository {

    @Nonnull
    public CloseableIterator<ID> findIdsBy(@Nonnull Q query);

    @Nonnegative
    public long countBy(@Nonnull Q query);
}

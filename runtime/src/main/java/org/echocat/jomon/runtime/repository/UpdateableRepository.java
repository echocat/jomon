/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012-2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.runtime.repository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface UpdateableRepository<Q extends Query, ID, U extends Update> extends Repository {

    /**
     * @return {@code true} if the element could be updated.
     */
    @Nullable
    public boolean update(@Nonnull U what, @Nonnull ID byId);

    /**
     * @return the number of updated elements.
     */
    @Nullable
    public long update(@Nonnull U what, @Nonnull Q byQuery);

}

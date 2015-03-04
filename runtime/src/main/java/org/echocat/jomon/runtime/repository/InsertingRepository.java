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

import javax.annotation.Nonnull;
import org.echocat.jomon.runtime.generation.*;

/**
 * @deprecated This way of inserting elements in a persistence layer is unsafe and potentially does not performing good enough.
 * Use {@link Generator} in combination with an {@link Requirement} instead.
 */
@Deprecated
public interface InsertingRepository<T> extends Repository {

    /**
     * @deprecated This way of inserting elements in a persistence layer is unsafe and potentially does not performing good enough.
     * Use {@link Generator#generate} instead.
    */
    @Deprecated
    public void insert(@Nonnull T toInsert);

}

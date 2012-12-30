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

package org.echocat.jomon.runtime.repository;

import org.echocat.jomon.runtime.valuemodule.ValueModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ConcurrentModificationException;

public interface ModularizedSafeUpdatingRepository<V, VM extends ValueModule, E extends ConcurrentModificationException> extends Repository {

    public void updateSafe(@Nonnull V value, @Nullable VM... modules) throws E;

}

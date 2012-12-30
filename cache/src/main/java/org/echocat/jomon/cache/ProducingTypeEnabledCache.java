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

package org.echocat.jomon.cache;

import org.echocat.jomon.runtime.util.ProducingType;

import javax.annotation.Nonnull;

public interface ProducingTypeEnabledCache<K, V> extends Cache<K, V> {

    public void setProducingType(@Nonnull ProducingType producingType);

    @Nonnull
    public ProducingType getProducingType();

}

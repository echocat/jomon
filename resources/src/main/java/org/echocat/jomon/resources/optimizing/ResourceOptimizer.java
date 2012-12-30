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

package org.echocat.jomon.resources.optimizing;

import org.echocat.jomon.resources.Resource;
import org.echocat.jomon.resources.ResourceTypeSupporting;

import javax.annotation.Nonnull;

public interface ResourceOptimizer extends ResourceTypeSupporting {

    @Nonnull
    public Resource optimize(@Nonnull Resource input, @Nonnull OptimizationContext context) throws Exception;
}

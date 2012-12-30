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

package org.echocat.jomon.runtime.valuemodule.access;

import javax.annotation.Nonnull;

public interface ValueModuleToAssignmentStackFactory {

    @Nonnull
    public <B> ValueModuleToAssignmentStack<?, B> getStack(@Nonnull Class<? extends B>... valueTypes);

}

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

package org.echocat.jomon.runtime.valuemodule.fetching;

import org.echocat.jomon.runtime.valuemodule.SourcePath;
import org.echocat.jomon.runtime.valuemodule.ValueModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public interface ValueFetcher<VM extends ValueModule, ID, B> {

    public boolean isResponsibleFor(@Nonnull Class<?> type);

    @Nonnull
    public B fetchWholeObjectFor(@Nullable ID id, @Nullable Set<? extends VM> valueModules, @Nullable SourcePath sourcePath) throws Exception;

    @Nullable
    public Object fetchValueOf(@Nullable B bean, @Nonnull VM valueModule, @Nullable SourcePath sourcePath) throws Exception;
}

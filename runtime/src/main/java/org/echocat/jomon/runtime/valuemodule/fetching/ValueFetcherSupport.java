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

import org.echocat.jomon.runtime.id.IdProvider;
import org.echocat.jomon.runtime.valuemodule.SourcePath;
import org.echocat.jomon.runtime.valuemodule.ValueModule;
import org.echocat.jomon.runtime.valuemodule.access.ValueModuleAccessor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static java.util.Collections.singleton;

public abstract class ValueFetcherSupport<VM extends ValueModule, ID, B> implements ValueFetcher<VM, ID, B> {

    private final IdProvider<ID, B> _idProvider;
    private final ValueModuleAccessor<VM, B> _accessor;

    public ValueFetcherSupport(@Nonnull IdProvider<ID, B> idProvider, @Nonnull ValueModuleAccessor<VM, B> accessor) {
        _idProvider = idProvider;
        _accessor = accessor;
    }

    @Override
    public boolean isResponsibleFor(@Nonnull Class<?> type) {
        return _accessor.isResponsibleFor(type);
    }

    @Override
    @Nullable
    public Object fetchValueOf(@Nullable B bean, @Nonnull VM valueModule, @Nullable SourcePath sourcePath) throws Exception {
        final ID id = _idProvider.provideIdOf(bean);
        final B fetchedBean = fetchWholeObjectFor(id, singleton(valueModule), sourcePath);
        return _accessor.getValueOf(fetchedBean, valueModule, sourcePath);
    }

}

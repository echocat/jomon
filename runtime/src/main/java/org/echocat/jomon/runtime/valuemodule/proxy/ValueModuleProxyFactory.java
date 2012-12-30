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

package org.echocat.jomon.runtime.valuemodule.proxy;

import org.echocat.jomon.runtime.valuemodule.InitialSource;
import org.echocat.jomon.runtime.valuemodule.ValueModule;
import org.echocat.jomon.runtime.valuemodule.ValueModuleRegistry;
import org.echocat.jomon.runtime.valuemodule.fetching.ValueFetcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ValueModuleProxyFactory {

    @Nonnull
    public <VM extends ValueModule, ID, B> B createFor(@Nonnull ValueModuleRegistry<VM, B> registry, @Nonnull ValueFetcher<VM, ID, B> fetcher, @Nonnull B baseBean, @Nullable InitialSource initialSource) throws IllegalArgumentException;

    @Nonnull
    public <B> B getDelegate(@Nonnull B base);
}

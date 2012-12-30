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

package org.echocat.jomon.runtime.valuemodule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public interface ValueModuleProvider<T extends ValueModule> {

    @Nonnull
    public T provideBy(@Nonnull String id) throws IllegalArgumentException;

    @Nonnull
    public Set<? extends T> provideAllBy(@Nullable String commaSeparatedIds) throws IllegalArgumentException;

    @Nonnull
    public String toCommaSeparatedIds(@Nullable Set<? extends T> valueModules);

    @Nonnull
    public Iterable<? extends T> getAll();

}

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

import org.echocat.jomon.runtime.valuemodule.SourcePath;
import org.echocat.jomon.runtime.valuemodule.ValueModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.beans.PropertyDescriptor;

public interface ValueModuleAccessor<VM extends ValueModule, B> {

    @Nullable
    public Object getValueOf(@Nonnull B baseBean, @Nonnull VM module);

    @Nullable
    public Object getValueOf(@Nonnull B baseBean, @Nonnull VM module, @Nullable SourcePath sourcePath);

    public void setValueOf(@Nonnull B baseBean, @Nonnull VM module, @Nullable Object value);

    public void setValueOf(@Nonnull B baseBean, @Nonnull VM module, @Nullable Object value, @Nullable SourcePath sourcePath);

    @Nullable
    public VM findModuleFor(@Nonnull PropertyDescriptor descriptor);

    public boolean isResponsibleFor(@Nonnull Class<?> type);
}

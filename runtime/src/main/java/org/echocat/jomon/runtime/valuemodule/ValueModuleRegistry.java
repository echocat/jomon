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
import java.beans.PropertyDescriptor;
import java.util.Set;

public interface ValueModuleRegistry<VM extends ValueModule, B> {

    @Nonnull
    public Class<? extends B> getTypeResponsibleFor();

    @Nullable
    public VM findModuleFor(@Nonnull PropertyDescriptor descriptor);

    public void notifyPropertyIsNowKnown(@Nonnull PropertyDescriptor ofDescriptor);

    public void notifyValueIsNowKnown(@Nonnull VM valueModule);

    public boolean isLoadOfValueRequiredFor(@Nonnull PropertyDescriptor descriptor);

    @Nonnull
    public Set<VM> getAllKnown();

    public void addListener(@Nonnull Listener<VM> listener);

    public void removeListener(@Nonnull Listener<VM> listener);

    public interface Listener<VM extends ValueModule> {

        public void notifyValueIsNowKnown(@Nonnull VM valueModule);

    }
}

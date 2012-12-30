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

import org.echocat.jomon.runtime.valuemodule.access.ValueModuleAccessor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.synchronizedSet;

public class DefaultValueModuleRegistry<VM extends ValueModule, B> implements ValueModuleRegistry<VM, B> {

    private final Class<? extends B> _type;
    private final ValueModuleAccessor<VM, B> _accessor;
    private final Set<VM> _known = synchronizedSet(new HashSet<VM>());
    private final Set<Listener<VM>> _listeners = synchronizedSet(new HashSet<Listener<VM>>());

    public DefaultValueModuleRegistry(@Nonnull Class<? extends B> type, @Nonnull ValueModuleAccessor<VM, B> accessor) {
        _type = type;
        _accessor = accessor;
    }

    @Nonnull
    @Override
    public Class<? extends B> getTypeResponsibleFor() {
        return _type;
    }

    @Override
    @Nullable
    public VM findModuleFor(@Nonnull PropertyDescriptor descriptor) {
        return _accessor.findModuleFor(descriptor);
    }

    @Override
    public void notifyPropertyIsNowKnown(@Nonnull PropertyDescriptor ofDescriptor) {
        final VM valueModule = findModuleFor(ofDescriptor);
        if (valueModule != null) {
            notifyValueIsNowKnown(valueModule);
        }
    }

    @Override
    public void notifyValueIsNowKnown(@Nonnull VM valueModule) {
        _known.add(valueModule);
        for (Listener<VM> listener : getAllListeners()) {
            listener.notifyValueIsNowKnown(valueModule);
        }
    }

    @Nonnull
    protected Set<Listener<VM>> getAllListeners() {
        synchronized (_listeners) {
            return new HashSet<>(_listeners);
        }
    }

    @Override
    public boolean isLoadOfValueRequiredFor(@Nonnull PropertyDescriptor descriptor) {
        final VM valueModule = findModuleFor(descriptor);
        return valueModule != null && !_known.contains(valueModule);
    }

    @Nonnull
    @Override
    public Set<VM> getAllKnown() {
        synchronized (_known) {
            return new HashSet<>(_known);
        }
    }

    @Override
    public void addListener(@Nonnull Listener<VM> listener) {
        _listeners.add(listener);
    }

    @Override
    public void removeListener(@Nonnull Listener<VM> listener) {
        _listeners.remove(listener);
    }
}

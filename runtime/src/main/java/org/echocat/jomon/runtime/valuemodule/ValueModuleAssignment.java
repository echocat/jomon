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
import java.beans.PropertyDescriptor;
import java.util.Collection;

public class ValueModuleAssignment<VM extends ValueModule, B> {

    private final VM _module;
    private final Class<? extends B> _bound;
    private final PropertyDescriptor _descriptor;
    private final Collection<ValueModuleAssignment<? extends VM, ? extends B>> _children;

    public ValueModuleAssignment(@Nonnull VM module, @Nonnull Class<? extends B> bound, @Nonnull PropertyDescriptor descriptor, @Nonnull Collection<ValueModuleAssignment<? extends VM, ? extends B>> children) {
        _module = module;
        _bound = bound;
        _descriptor = descriptor;
        _children = children;
    }

    @Nonnull
    public VM getModule() {
        return _module;
    }

    public Class<? extends B> getBound() {
        return _bound;
    }

    @Nonnull
    public PropertyDescriptor getDescriptor() {
        return _descriptor;
    }

    @Nonnull
    public Collection<ValueModuleAssignment<? extends VM, ? extends B>> getChildren() {
        return _children;
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else if (!(o instanceof ValueModuleAssignment)) {
            result = false;
        } else {
            final ValueModuleAssignment<?, ?> that = (ValueModuleAssignment) o;
            result = _module.equals(that._module) && _descriptor.equals(that._descriptor);
        }
        return result;
    }

    @Override
    public int hashCode() {
        int result = _module.hashCode();
        result = 31 * result + _descriptor.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{module=" + _module + ", property=" + _descriptor.getName() + ", children=" + _children.size() + "x}";
    }
}

/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012-2013 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.runtime.format;

import javax.annotation.Nonnull;

public abstract class FormatSupport {

    private final String _name;

    public FormatSupport(@Nonnull String name) {
        _name = name;
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else if (!isOfRequiredType(o)) {
            result = false;
        } else {
            result = getName().equals(getNameOf(o));
        }
        return result;
    }

    protected abstract boolean isOfRequiredType(@Nonnull Object o);

    protected abstract String getNameOf(@Nonnull Object o);

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Nonnull
    public String getName() {
        return _name;
    }

    @Override
    public String toString() {
        return getName();
    }
}
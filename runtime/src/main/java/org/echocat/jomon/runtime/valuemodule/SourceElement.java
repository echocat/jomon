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

import javax.annotation.Nullable;

public class SourceElement {

    private final Object _element;

    public SourceElement(@Nullable Object element) {
        _element = element;
    }

    @Nullable
    public Object getElement() {
        return _element;
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else if (!(o instanceof SourceElement)) {
            result = false;
        } else {
            final SourceElement that = (SourceElement) o;
            result = _element != null ? _element.equals(that._element) : that._element == null;
        }
        return result;
    }

    @Override
    public int hashCode() {
        return _element != null ? _element.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "[" + _element.toString() + "]";
    }
}

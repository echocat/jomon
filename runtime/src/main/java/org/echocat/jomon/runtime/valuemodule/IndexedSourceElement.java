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

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;

public class IndexedSourceElement extends SourceElement {

    private final int _index;

    public IndexedSourceElement(@Nullable Object element, @Nonnegative int index) {
        super(element);
        _index = index;
    }

    @Nonnegative
    public int getIndex() {
        return _index;
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else if (!(o instanceof IndexedSourceElement) || !super.equals(o)) {
            result = false;
        } else {
            final IndexedSourceElement that = (IndexedSourceElement) o;
            result = _index == that._index;
        }
        return result;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + _index;
        return result;
    }

    @Override
    public String toString() {
        return "[" + getElement() + "#" + _index + "]";
    }
}

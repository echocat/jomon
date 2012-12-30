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
import java.util.Iterator;

public abstract class SourcePath implements Iterable<SourceElement> {

    private final InitialSource _initialSource;

    protected SourcePath() {
        this(null);
    }

    protected SourcePath(@Nullable InitialSource initialSource) {
        _initialSource = initialSource;
    }

    public InitialSource getInitialSource() {
        return _initialSource;
    }

    @Override
    public boolean equals(Object other) {
        boolean result;
        if (this == other) {
            result = true;
        } else if (!(other instanceof SourcePath)) {
            result = false;
        } else {
            final SourcePath that = (SourcePath)other;
            result = true;
            final Iterator<? extends SourceElement> thisI = iterator();
            final Iterator<? extends SourceElement> thatI = that.iterator();
            while (result && thatI.hasNext() && thatI.hasNext()) {
                final SourceElement thisO = thisI.next();
                final SourceElement thatO = thatI.next();
                result = thisO != null ? thisO.equals(thatO) : thatO == null;
            }
            if (result) {
                result = !thisI.hasNext() && !thatI.hasNext();
            }
        }
        return result;
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        for (SourceElement element : this) {
            hashCode = 31 * hashCode + (element != null ? element.hashCode() : 0);
        }
        return hashCode;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (Object element : this) {
            if (sb.length() > 0) {
                sb.append("->");
            }
            sb.append(element);
        }
        return sb.toString();
    }
}

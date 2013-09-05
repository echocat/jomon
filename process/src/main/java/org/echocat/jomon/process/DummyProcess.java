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

package org.echocat.jomon.process;

import java.io.File;

public class DummyProcess implements Process {

    private final long _id;

    public DummyProcess(long id) {
        _id = id;
    }

    @Override
    public long getId() {
        return _id;
    }

    @Override
    public File getExecutable() {
        return null;
    }

    @Override
    public String[] getCommandLine() {
        return new String[0];
    }

    @Override
    public boolean isPathCaseSensitive() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else if (!(o instanceof DummyProcess)) {
            result = false;
        } else {
            final DummyProcess that = (DummyProcess) o;
            result = _id == that._id;
        }
        return result;
    }

    @Override
    public int hashCode() {
        return (int) (_id ^ (_id >>> 32));
    }

    @Override
    public String toString() {
        return "Process #" + _id;
    }

}

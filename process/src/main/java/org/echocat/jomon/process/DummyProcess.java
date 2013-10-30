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

import org.echocat.jomon.process.local.LocalProcess;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;

import static java.util.Collections.emptyList;

public class DummyProcess<E, ID> implements Process<E, ID> {

    @Nullable
    private final ID _id;

    public DummyProcess(@Nullable ID id) {
        _id = id;
    }

    @Override
    public ID getId() {
        return _id;
    }

    @Override
    public E getExecutable() {
        return null;
    }

    @Override
    public List<String> getArguments() {
        return emptyList();
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else if (!(o instanceof DummyProcess)) {
            result = false;
        } else {
            final DummyProcess<?, ?> that = (DummyProcess) o;
            final ID id = getId();
            result = id != null ? id.equals(that.getId()) : that.getId() == null;
        }
        return result;
    }

    @Override
    public int hashCode() {
        return (_id != null ? _id.hashCode() : 0);
    }

    @Override
    public String toString() {
        return "Process #" + _id;
    }

    public static class LocalDummyProcess extends DummyProcess<File, Long> implements LocalProcess {

        public LocalDummyProcess(@Nullable Long aLong) {
            super(aLong);
        }



    }

}

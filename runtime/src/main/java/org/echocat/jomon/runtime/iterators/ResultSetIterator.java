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

package org.echocat.jomon.runtime.iterators;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;

import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

public abstract class ResultSetIterator<R> implements CloseableIterator<R> {

    private final ResultSet _resultSet;

    private Boolean _hasNext;

    protected ResultSetIterator(@Nonnull ResultSet resultSet) {
        _resultSet = resultSet;
    }

    public abstract R convert(@Nonnull ResultSet row) throws SQLException;

    @Override
    public boolean hasNext() {
        if (_hasNext == null) {
            try {
                _hasNext = _resultSet.next();
            } catch (SQLException e) {
                throw new RuntimeException("Could not retrieve next row of " + _resultSet + ".", e);
            }
        }
        return _hasNext;
    }

    @Override
    public R next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        final R result;
        try {
            result = convert(_resultSet);
        } catch (SQLException e) {
            throw new RuntimeException("Could not convert current row of " + _resultSet + ".", e);
        }
        _hasNext = null;
        return result;
    }

    @Override public void close() { closeQuietly(_resultSet); }
    @Override public void remove() { throw new UnsupportedOperationException(); }
}

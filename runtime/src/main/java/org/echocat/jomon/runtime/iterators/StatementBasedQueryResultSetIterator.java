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

package org.echocat.jomon.runtime.iterators;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static java.lang.Integer.MIN_VALUE;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

public abstract class StatementBasedQueryResultSetIterator<R> extends ResultSetIterator<R> {

    private final Statement _statement;
    private final String _querySql;

    protected StatementBasedQueryResultSetIterator(@Nonnull Statement statement, @Nonnull String querySql) throws SQLException {
        this(statement, querySql, MIN_VALUE);
    }

    protected StatementBasedQueryResultSetIterator(@Nonnull Statement statement, @Nonnull String querySql, @Nullable Integer fetchSize) throws SQLException {
        super(createResultSetFor(statement, querySql));
        _statement = statement;
        _querySql = querySql;
        if (fetchSize != null) {
            try {
                statement.setFetchSize(fetchSize);
            } catch (final SQLException e) {
                throw new RuntimeException("Could not set fetchSize to " + fetchSize + " at " + statement + ".", e);
            }
        }
    }

    @Nonnull
    protected static ResultSet createResultSetFor(@Nonnull Statement statement, @Nonnull String sql) throws SQLException {
        boolean success = false;
        try {
            final ResultSet resultSet = statement.executeQuery(sql);
            success = true;
            return resultSet;
        } finally {
            if (!success) {
                closeQuietly(statement);
            }
        }
    }

    @Override
    public void close() {
        try {
            closeQuietly(_statement);
        } finally {
            super.close();
        }
    }

    @Override
    public String toString() {
        return "Iterator for '" + _querySql + "'";
    }
}

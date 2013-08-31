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
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static java.lang.Integer.MIN_VALUE;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

public abstract class ConnectionBasedQueryResultSetIterator<R> extends StatementBasedQueryResultSetIterator<R> {

    private final Connection _connection;

    protected ConnectionBasedQueryResultSetIterator(@Nonnull Connection connection, @Nonnull String sql) throws SQLException {
        this(connection, sql, MIN_VALUE);
    }

    protected ConnectionBasedQueryResultSetIterator(@Nonnull Connection connection, @Nonnull String sql, @Nullable Integer fetchSize) throws SQLException {
        super(createStatementFor(connection), sql, fetchSize);
        _connection = connection;
    }

    @Nonnull
    protected static Statement createStatementFor(@Nonnull Connection connection) throws SQLException {
        boolean success = false;
        try {
            final Statement statement = connection.createStatement();
            success = true;
            return statement;
        } finally {
            if (!success) {
                closeQuietly(connection);
            }
        }
    }

    @Override
    public void close() {
        try {
            closeQuietly(_connection);
        } finally {
            super.close();
        }
    }

}

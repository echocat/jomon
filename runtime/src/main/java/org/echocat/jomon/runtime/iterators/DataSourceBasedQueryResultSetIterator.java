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
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static java.lang.Integer.MIN_VALUE;

public abstract class DataSourceBasedQueryResultSetIterator<R> extends ConnectionBasedQueryResultSetIterator<R> {

    protected DataSourceBasedQueryResultSetIterator(@Nonnull DataSource dataSource, @Nonnull String sql) throws SQLException {
        this(dataSource, sql, MIN_VALUE);
    }

    protected DataSourceBasedQueryResultSetIterator(@Nonnull DataSource dataSource, @Nonnull String sql, @Nullable Integer fetchSize) throws SQLException {
        super(createConnectionFor(dataSource), sql, fetchSize);
    }

    @Nonnull
    protected static Connection createConnectionFor(@Nonnull DataSource dataSource) throws SQLException {
        return dataSource.getConnection();
    }
}

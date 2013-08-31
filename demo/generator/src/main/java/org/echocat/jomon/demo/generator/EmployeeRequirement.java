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

package org.echocat.jomon.demo.generator;

import org.echocat.jomon.runtime.annotations.Excluding;
import org.echocat.jomon.runtime.annotations.Including;
import org.echocat.jomon.runtime.date.DateRangeRequirement;
import org.echocat.jomon.runtime.date.DateRequirement;
import org.echocat.jomon.runtime.date.ExactDateRequirement;
import org.echocat.jomon.runtime.generation.Requirement;
import org.echocat.jomon.runtime.generation.StringRequirement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Date;

import static org.echocat.jomon.runtime.generation.StringRequirement.UNIQUE_VALUE_PLACE_HOLDER;

public class EmployeeRequirement implements Requirement {

    @Nonnull
    public static EmployeeRequirement employee() {
        return new EmployeeRequirement();
    }

    private StringRequirement _name = new StringRequirement("employee" + UNIQUE_VALUE_PLACE_HOLDER);
    private DateRequirement _joined = new ExactDateRequirement(new Date());

    @Nonnull
    public EmployeeRequirement withName(@Nonnull String name) {
        _name = new StringRequirement(name);
        return this;
    }

    @Nonnull
    public EmployeeRequirement withNamePrefixedBy(@Nonnull String prefixedBy) {
        return withName(prefixedBy + UNIQUE_VALUE_PLACE_HOLDER);
    }

    @Nonnull
    public EmployeeRequirement joinedBetween(@Nullable @Including Date from, @Nullable @Excluding Date to) {
        _joined = new DateRangeRequirement(from, to);
        return this;
    }

    @Nonnull
    public EmployeeRequirement joinedBefore(@Nonnull @Excluding Date before) {
        return joinedBetween(null, before);
    }

    @Nonnull
    public EmployeeRequirement joinedAfter(@Nonnull @Including Date after) {
        return joinedBetween(after, null);
    }

    @Nonnull
    public EmployeeRequirement joined(@Nonnull Date at) {
        _joined = new ExactDateRequirement(at);
        return this;
    }

    @Nonnull
    public StringRequirement getName() {
        return _name;
    }

    @Nonnull
    public DateRequirement getJoined() {
        return _joined;
    }
}

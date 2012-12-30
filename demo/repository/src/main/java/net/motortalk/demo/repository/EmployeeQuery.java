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

package net.motortalk.demo.repository;

import com.google.common.base.Predicate;
import org.echocat.jomon.runtime.annotations.Excluding;
import org.echocat.jomon.runtime.annotations.Including;
import org.echocat.jomon.runtime.date.DateRange;
import org.echocat.jomon.runtime.repository.Query;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Date;
import java.util.Set;
import java.util.regex.Pattern;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.regex.Pattern.compile;
import static java.util.regex.Pattern.quote;
import static org.apache.commons.lang3.ArrayUtils.toObject;

public class EmployeeQuery implements Query, Predicate<Employee> {

    @Nonnull
    public static EmployeeQuery employee() {
        return new EmployeeQuery();
    }

    private Set<Long> _ids;
    private Pattern _name;
    private DateRange _joinedBetween;

    @Nonnull
    public EmployeeQuery withId(@Nonnegative long id) {
        return withIds(id);
    }

    @Nonnull
    public EmployeeQuery withIds(@Nullable long... ids) {
        if (_ids != null) {
            throw new IllegalStateException("Ids already set.");
        }
        _ids = newHashSet(toObject(ids));
        return this;
    }

    @Nonnull
    public EmployeeQuery withName(@Nonnull String name) {
        if (_name != null) {
            throw new IllegalStateException("Name already set.");
        }
        _name = compile(quote(name));
        return this;
    }

    @Nonnull
    public EmployeeQuery withNamePrefixedBy(@Nonnull String prefix) {
        if (_name != null) {
            throw new IllegalStateException("Name already set.");
        }
        _name = compile(quote(prefix) + ".*");
        return this;
    }

    @Nonnull
    public EmployeeQuery joinedBetween(@Nullable @Including Date from, @Nullable @Excluding Date to) {
        if (_joinedBetween != null) {
            throw new IllegalStateException("JoinedBetween already set.");
        }
        _joinedBetween = new DateRange(from, to);
        return this;
    }

    @Override
    public boolean apply(@Nullable Employee input) {
        return input != null
            && applyId(input)
            && applyName(input)
            && applyCreated(input)
            ;
    }

    protected boolean applyId(@Nonnull Employee input) {
        return _ids == null || _ids.contains(input.getId());
    }

    protected boolean applyName(@Nonnull Employee input) {
        final boolean result;
        if (_name != null) {
            final String nameOfEmployee = input.getName();
            if (nameOfEmployee != null) {
                result = _name.matcher(nameOfEmployee).matches();
            } else {
                result = false;
            }
        } else {
            result = true;
        }
        return result;
    }

    protected boolean applyCreated(@Nonnull Employee input) {
        final boolean result;
        if (_joinedBetween != null) {
            final Date joinedOfEmployee = input.getJoined();
            if (joinedOfEmployee != null) {
                result = _joinedBetween.apply(joinedOfEmployee);
            } else {
                result = false;
            }
        } else {
            result = true;
        }
        return result;
    }

    @Nullable
    public Set<Long> getIds() {
        return _ids;
    }

    @Nullable
    public Pattern getName() {
        return _name;
    }

    @Nullable
    public DateRange getJoinedBetween() {
        return _joinedBetween;
    }
}

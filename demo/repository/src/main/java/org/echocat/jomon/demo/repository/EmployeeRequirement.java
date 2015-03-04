/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012-2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.demo.repository;

import org.echocat.jomon.runtime.annotations.Excluding;
import org.echocat.jomon.runtime.annotations.Including;
import org.echocat.jomon.runtime.generation.Requirement;
import org.echocat.jomon.runtime.generation.StringRequirement;
import org.echocat.jomon.runtime.numbers.ExactLongRequirement;
import org.echocat.jomon.runtime.numbers.LongRangeRequirement;
import org.echocat.jomon.runtime.numbers.LongRequirement;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import static java.lang.Long.MAX_VALUE;
import static org.echocat.jomon.runtime.generation.StringRequirement.UNIQUE_VALUE_PLACE_HOLDER;

public class EmployeeRequirement implements Requirement {

    @Nonnull
    public static EmployeeRequirement requirement() {
        return new EmployeeRequirement();
    }

    @Nonnull
    public static EmployeeRequirement employee() {
        return requirement();
    }

    @Nonnull
    private StringRequirement _name;
    @Nonnull
    private LongRequirement _id;

    public EmployeeRequirement() {
        withNameStartingWith("user");
        withIdInRange(0, MAX_VALUE);
    }

    @Nonnull
    public EmployeeRequirement withName(@Nonnull String name) {
        _name = new StringRequirement(name);
        return this;
    }

    @Nonnull
    public EmployeeRequirement withNameStartingWith(@Nonnull String namePrefix) {
        _name = new StringRequirement(namePrefix + "-" + UNIQUE_VALUE_PLACE_HOLDER);
        return this;
    }

    @Nonnull
    public EmployeeRequirement withId(@Nonnegative long id) {
        _id = new ExactLongRequirement(id);
        return this;
    }

    @Nonnull
    public EmployeeRequirement withIdInRange(@Nonnegative @Including long from, @Nonnegative @Excluding long to) {
        _id = new LongRangeRequirement(from, to);
        return this;
    }

    @Nonnull
    public StringRequirement getName() {
        return _name;
    }

    @Nonnull
    public LongRequirement getId() {
        return _id;
    }

}

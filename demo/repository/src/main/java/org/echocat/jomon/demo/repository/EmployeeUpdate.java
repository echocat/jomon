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

import org.echocat.jomon.runtime.repository.Update;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EmployeeUpdate implements Update {

    @Nonnull
    public static EmployeeUpdate update() {
        return new EmployeeUpdate();
    }

    @Nonnull
    public static EmployeeUpdate employee() {
        return update();
    }

    @Nullable
    private String _name;
    private boolean _incrementNumberOfLogins;

    @Nonnull
    public EmployeeUpdate changeNameTo(@Nonnull String name) {
        _name = name;
        return this;
    }

    @Nonnull
    public EmployeeUpdate incrementNumberOfLogins() {
        _incrementNumberOfLogins = true;
        return this;
    }

    @Nullable
    public String getName() {
        return _name;
    }

    public boolean isIncrementNumberOfLogins() {
        return _incrementNumberOfLogins;
    }

}

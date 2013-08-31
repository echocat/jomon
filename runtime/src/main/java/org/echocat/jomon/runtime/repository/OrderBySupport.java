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

package org.echocat.jomon.runtime.repository;

import javax.annotation.Nonnull;

public abstract class OrderBySupport<F> implements OrderBy<F> {

    private F _field;
    private Direction _direction;

    public OrderBySupport() {}

    public OrderBySupport(@Nonnull F field, @Nonnull Direction direction) {
        _field = field;
        _direction = direction;
    }

    @Nonnull
    @Override
    public F getField() {
        return _field;
    }

    public void setField(F field) {
        _field = field;
    }

    @Nonnull
    @Override
    public Direction getDirection() {
        return _direction;
    }

    public void setDirection(Direction direction) {
        _direction = direction;
    }

}

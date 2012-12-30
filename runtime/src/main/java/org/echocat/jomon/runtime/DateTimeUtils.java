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

package org.echocat.jomon.runtime;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Date;

public class DateTimeUtils {

    @Nonnull
    public static Date now() {
        return new Date();
    }

    @Nonnull
    public static Date addHours(@Nonnull Date date, @Nonnegative int hours) {
        return addMinutes(date, hours * 60);
    }

    @Nonnull
    public static Date addDays(@Nonnull Date date, @Nonnegative int days) {
        return addHours(date, days * 24);
    }

    @Nonnull
    public static Date addMinutes(@Nonnull Date date, @Nonnegative int minutes) {
        return new Date(date.getTime() + minutes * 60 * 1000);
    }

    @Nonnull
    public static Date addSeconds(@Nonnull Date date, @Nonnegative int seconds) {
        return new Date(date.getTime() + seconds * 1000);
    }

    @Nonnull
    public static Date addMilliseconds(@Nonnull Date date, @Nonnegative int milliseconds) {
        return new Date(date.getTime() + milliseconds);
    }

    private DateTimeUtils() {}

}

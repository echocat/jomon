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

import org.echocat.jomon.runtime.util.Duration;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.System.currentTimeMillis;

public class DateTimeUtils {

    public static final String ISO_PATTERN = "yyyy-MM-dd'T'HH:mm:ssZ";

    @Nonnull
    public static Date now() {
        return new Date();
    }

    @Nonnull
    public static Date addWeeks(@Nonnull Date date, @Nonnegative int weeks) {
        return addDays(date, weeks * 7);
    }

    @Nonnull
    public static Date addDays(@Nonnull Date date, @Nonnegative int days) {
        return addHours(date, days * 24);
    }

    @Nonnull
    public static Date addHours(@Nonnull Date date, @Nonnegative int hours) {
        return addMinutes(date, hours * 60);
    }

    @Nonnull
    public static Date addMinutes(@Nonnull Date date, @Nonnegative int minutes) {
        return addSeconds(date, minutes * 60);
    }

    @Nonnull
    public static Date addSeconds(@Nonnull Date date, @Nonnegative int seconds) {
        return addMilliseconds(date, seconds * 1000);
    }

    @Nonnull
    public static Date addMilliseconds(@Nonnull Date date, @Nonnegative int milliseconds) {
        return new Date(date.getTime() + milliseconds);
    }

    @Nonnull
    public static Date nowBefore(@Nonnull String duration) {
        return nowBefore(new Duration(duration));
    }

    @Nonnull
    public static Date nowBefore(@Nonnull Duration duration) {
        return new Date(currentTimeMillis() - duration.toMilliSeconds());
    }

    @Nonnull
    public static Date nowIn(@Nonnull String duration) {
        return nowIn(new Duration(duration));
    }

    @Nonnull
    public static Date nowIn(@Nonnull Duration duration) {
        return new Date(currentTimeMillis() + duration.toMilliSeconds());
    }

    @Nonnull
    public static Date parseIsoDate(@Nonnull String asString) throws IllegalArgumentException {
        try {
            return new SimpleDateFormat(ISO_PATTERN).parse(asString);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Could not parse: " + asString + ", it does not match pattern: " + ISO_PATTERN, e);
        }
    }

    private DateTimeUtils() {}

}

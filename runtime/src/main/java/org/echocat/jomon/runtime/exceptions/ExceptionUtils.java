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

package org.echocat.jomon.runtime.exceptions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ExceptionUtils {

    @Nullable
    public static <T extends Throwable> T findExceptionInCausesOf(@Nonnull Throwable start, @Nonnull Class<T> expectedType) {
        Throwable current = start;
        T result = null;
        while (result == null && current != null) {
            if (expectedType.isInstance(current)) {
                result = expectedType.cast(current);
            } else if (current.equals(current.getCause())) {
                current = null;
            } else {
                current = current.getCause();
            }
        }
        return result;
    }

    public static boolean containsException(@Nonnull Throwable start, @Nonnull Class<? extends Throwable> ofType) {
        // noinspection ThrowableResultOfMethodCallIgnored
        return findExceptionInCausesOf(start, ofType) != null;
    }

    public static boolean matches(@Nonnull ClassNotFoundException cnfe, @Nonnull String classNamePrefix) {
        final String message = cnfe.getMessage();
        return message != null && message.startsWith(classNamePrefix);
    }

    public static boolean containsClassNotFoundException(@Nonnull Throwable start, @Nonnull String classNamePrefix) {
        // noinspection ThrowableResultOfMethodCallIgnored
        final ClassNotFoundException cnfe = findExceptionInCausesOf(start, ClassNotFoundException.class);
        return cnfe != null && matches(cnfe, classNamePrefix);
    }

    private ExceptionUtils() {}
}

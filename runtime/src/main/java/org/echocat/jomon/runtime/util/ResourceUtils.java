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

package org.echocat.jomon.runtime.util;

import javax.annotation.Nullable;

public class ResourceUtils {

    public static void closeQuietlyIfAutoCloseable(@Nullable Object autoCloseable) {
        try {
            if (autoCloseable instanceof AutoCloseable) {
                ((AutoCloseable)autoCloseable).close();
            }
        } catch (Exception ignored) {}
    }

    public static void closeQuietly(@Nullable AutoCloseable autoCloseable) {
        closeQuietlyIfAutoCloseable(autoCloseable);
    }

    public static void closeQuietlyIfAutoCloseable(@Nullable Iterable<?> elements) {
        try {
            if (elements != null) {
                for (Object element : elements) {
                    closeQuietlyIfAutoCloseable(element);
                }
            }
        } catch (Exception ignored) {}
    }

    public static void closeQuietly(@Nullable Iterable<? extends AutoCloseable> elements) {
        closeQuietlyIfAutoCloseable(elements);
    }

    public static void closeQuietlyIfAutoCloseable(@Nullable Object[] elements) {
        try {
            if (elements != null) {
                for (Object element : elements) {
                    closeQuietlyIfAutoCloseable(element);
                }
            }
        } catch (Exception ignored) {}
    }

    public static void closeQuietly(@Nullable AutoCloseable[] elements) {
        closeQuietlyIfAutoCloseable(elements);
    }

    private ResourceUtils() {}
}

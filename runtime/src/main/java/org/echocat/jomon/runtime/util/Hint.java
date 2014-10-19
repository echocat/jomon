/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012-2014 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.runtime.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Hint<T> extends Key<T> {

    public static class Impl<T> extends Key.Impl<T> implements Hint<T> {

        @Nonnull
        public static <T> Hint<T> hint(@Nonnull Class<T> valueType, @Nonnull String fullName) {
            return hint(valueType, fullName, (T) null);
        }

        @Nonnull
        public static <T> Hint<T> hint(@Nonnull Class<T> valueType, @Nonnull String fullName, @Nullable T defaultValue) {
            return new Hint.Impl<>(valueType, fullName, defaultValue, true);
        }

        @Nonnull
        public static <T> Hint<T> hint(@Nonnull Class<T> valueType, @Nonnull Class<?> forContainer, @Nonnull String withName) {
            return hint(valueType, forContainer, withName, null);
        }

        @Nonnull
        public static <T> Hint<T> hint(@Nonnull Class<T> valueType, @Nonnull Class<?> forContainer, @Nonnull String withName, @Nullable T defaultValue) {
            return hint(valueType, forContainer.getName() + "." + withName, defaultValue);
        }

        @Nonnull
        public static <T> Hint<T> hint(@Nonnull Class<T> valueType, @Nonnull Object forContainer, @Nonnull String withName) {
            return hint(valueType, forContainer, withName, null);
        }

        @Nonnull
        public static <T> Hint<T> hint(@Nonnull Class<T> valueType, @Nonnull Object forContainer, @Nonnull String withName, @Nullable T defaultValue) {
            return hint(valueType, forContainer.getClass(), withName, defaultValue);
        }

        @Nonnull
        public static <T> Hint<T> nonNullHint(@Nonnull Class<T> valueType, @Nonnull String fullName, @Nonnull T defaultValue) {
            return new Hint.Impl<>(valueType, fullName, defaultValue, false);
        }

        @Nonnull
        public static <T> Hint<T> nonNullHint(@Nonnull Class<T> valueType, @Nonnull Class<?> forContainer, @Nonnull String withName, @Nonnull T defaultValue) {
            return nonNullHint(valueType, forContainer.getName() + "." + withName, defaultValue);
        }

        @Nonnull
        public static <T> Hint<T> nonNullHint(@Nonnull Class<T> valueType, @Nonnull Object forContainer, @Nonnull String withName, @Nonnull T defaultValue) {
            return nonNullHint(valueType, forContainer.getClass(), withName, defaultValue);
        }

        public Impl(@Nonnull Class<T> valueType, @Nonnull String name, @Nullable T defaultValue, boolean nullValueAllowed) {
            super(valueType, name, defaultValue, nullValueAllowed);
        }

    }

}

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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Hint<T> {

    @Nonnull
    public Class<T> getValueType();

    @Nonnull
    public String getName();

    @Nullable
    public T getDefaultValue();

    public boolean isNullValueAllowed();

    public static class Impl<T> implements Hint<T> {

        @Nonnull
        public static <T> Hint<T> hint(@Nonnull Class<T> valueType, @Nonnull String fullName) {
            return hint(valueType, fullName, (T) null);
        }

        @Nonnull
        public static <T> Hint<T> hint(@Nonnull Class<T> valueType, @Nonnull String fullName, @Nullable T defaultValue) {
            return new Impl<>(valueType, fullName, defaultValue, true);
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
            return new Impl<>(valueType, fullName, defaultValue, false);
        }

        @Nonnull
        public static <T> Hint<T> nonNullHint(@Nonnull Class<T> valueType, @Nonnull Class<?> forContainer, @Nonnull String withName, @Nonnull T defaultValue) {
            return nonNullHint(valueType, forContainer.getName() + "." + withName, defaultValue);
        }

        @Nonnull
        public static <T> Hint<T> nonNullHint(@Nonnull Class<T> valueType, @Nonnull Object forContainer, @Nonnull String withName, @Nonnull T defaultValue) {
            return nonNullHint(valueType, forContainer.getClass(), withName, defaultValue);
        }

        private final Class<T> _valueType;
        private final String _name;
        private final T _defaultValue;
        private final boolean _nullValueAllowed;

        public Impl(@Nonnull Class<T> valueType, @Nonnull String name, @Nullable T defaultValue, boolean nullValueAllowed) {
            _valueType = valueType;
            _name = name;
            _defaultValue = defaultValue;
            _nullValueAllowed = nullValueAllowed;
        }

        @Nonnull
        @Override
        public Class<T> getValueType() {
            return _valueType;
        }

        @Nonnull
        @Override
        public String getName() {
            return _name;
        }

        @Override
        public T getDefaultValue() {
            return _defaultValue;
        }

        @Override
        public boolean isNullValueAllowed() {
            return _nullValueAllowed;
        }

        @Override
        public String toString() {
            return getName();
        }

        @Override
        public boolean equals(Object o) {
            final boolean result;
            if (this == o) {
                result = true;
            } else if (!(o instanceof Hint)) {
                result = false;
            } else {
                result = getName().equals(((Hint)o).getName());
            }
            return result;
        }

        @Override
        public int hashCode() {
            return _name.hashCode();
        }
    }

}

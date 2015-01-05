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

public interface Key<T> {

    @Nonnull
    public Class<T> getValueType();

    @Nonnull
    public String getName();

    @Nullable
    public T getDefaultValue();

    public boolean isNullValueAllowed();

    public static class Impl<T> implements Key<T> {

        @Nonnull
        public static <T> Key<T> key(@Nonnull Class<T> valueType, @Nonnull String fullName) {
            return key(valueType, fullName, (T) null);
        }

        @Nonnull
        public static <T> Key<T> key(@Nonnull Class<T> valueType, @Nonnull String fullName, @Nullable T defaultValue) {
            return new Impl<>(valueType, fullName, defaultValue, true);
        }

        @Nonnull
        public static <T> Key<T> key(@Nonnull Class<T> valueType, @Nonnull Class<?> forContainer, @Nonnull String withName) {
            return key(valueType, forContainer, withName, null);
        }

        @Nonnull
        public static <T> Key<T> key(@Nonnull Class<T> valueType, @Nonnull Class<?> forContainer, @Nonnull String withName, @Nullable T defaultValue) {
            return key(valueType, forContainer.getName() + "." + withName, defaultValue);
        }

        @Nonnull
        public static <T> Key<T> key(@Nonnull Class<T> valueType, @Nonnull Object forContainer, @Nonnull String withName) {
            return key(valueType, forContainer, withName, null);
        }

        @Nonnull
        public static <T> Key<T> key(@Nonnull Class<T> valueType, @Nonnull Object forContainer, @Nonnull String withName, @Nullable T defaultValue) {
            return key(valueType, forContainer.getClass(), withName, defaultValue);
        }

        @Nonnull
        public static <T> Key<T> nonNullKey(@Nonnull Class<T> valueType, @Nonnull String fullName, @Nullable T defaultValue) {
            return new Impl<>(valueType, fullName, defaultValue, false);
        }

        @Nonnull
        public static <T> Key<T> nonNullKey(@Nonnull Class<T> valueType, @Nonnull String fullName) {
            return new Impl<>(valueType, fullName, null, false);
        }

        @Nonnull
        public static <T> Key<T> nonNullKey(@Nonnull Class<T> valueType, @Nonnull Class<?> forContainer, @Nonnull String withName, @Nonnull T defaultValue) {
            return nonNullKey(valueType, forContainer.getName() + "." + withName, defaultValue);
        }

        @Nonnull
        public static <T> Key<T> nonNullKey(@Nonnull Class<T> valueType, @Nonnull Class<?> forContainer, @Nonnull String withName) {
            return nonNullKey(valueType, forContainer.getName() + "." + withName);
        }

        @Nonnull
        public static <T> Key<T> nonNullKey(@Nonnull Class<T> valueType, @Nonnull Object forContainer, @Nonnull String withName, @Nonnull T defaultValue) {
            return nonNullKey(valueType, forContainer.getClass(), withName, defaultValue);
        }

        @Nonnull
        public static <T> Key<T> nonNullKey(@Nonnull Class<T> valueType, @Nonnull Object forContainer, @Nonnull String withName) {
            return nonNullKey(valueType, forContainer.getClass(), withName);
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
            } else if (!(o instanceof Key)) {
                result = false;
            } else {
                result = getName().equals(((Key)o).getName());
            }
            return result;
        }

        @Override
        public int hashCode() {
            return _name.hashCode();
        }
    }

}

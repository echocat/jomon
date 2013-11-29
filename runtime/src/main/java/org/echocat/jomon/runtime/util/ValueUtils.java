package org.echocat.jomon.runtime.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ValueUtils {

    @Nullable
    public static <T> T findValue(@Nullable Object plain, @Nonnull Class<T> valueType, @Nullable T defaultValue) {
        final T result;
        if (plain == null) {
            result = null;
        } else if (valueType.isInstance(plain)) {
            result = valueType.cast(plain);
        } else if (plain instanceof ValueHolder) {
            final ValueHolder<?> holder = (ValueHolder<?>) plain;
            if (valueType.isAssignableFrom(holder.providesValueOfType())) {
                result = valueType.cast(holder.getValue());
            } else {
                throw new IllegalArgumentException(holder + " provides value of " + holder.providesValueOfType().getName() + " which is not assignable from expected type " + valueType.getName() + ".");
            }
        } else {
            throw new IllegalArgumentException("The provided value " + plain + " is neither an instance of expected value type " + valueType.getName() + " nor " + ValueHolder.class.getName() + ".");
        }
        return result != null ? result : defaultValue;
    }

    @Nullable
    public static <T> T findValue(@Nullable Object plain, @Nonnull Class<T> valueType) {
        return findValue(plain, valueType, null);
    }

    @Nonnull
    public static <T> T getValue(@Nullable Object plain, @Nonnull Class<T> valueType, @Nonnull T defaultValue) {
        return findValue(plain, valueType, defaultValue);
    }

    @Nonnull
    public static <T> T getValue(@Nonnull Object plain, @Nonnull Class<T> valueType) {
        final T result = findValue(plain, valueType);
        if (result == null) {
            if (plain instanceof ValueHolder) {
                throw new NullPointerException(plain + " returned null.");
            } else {
                throw new NullPointerException("Given object is null.");
            }
        }
        return result;
    }

    private ValueUtils() {}

}

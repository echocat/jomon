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

package org.echocat.jomon.testing;

import com.google.common.base.Predicate;
import org.echocat.jomon.runtime.util.Duration;
import org.hamcrest.*;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.IsNull;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static java.util.Arrays.asList;

public class BaseMatchers {

    @Nonnull
    public static <T> Matcher<T> isSameAs(@Nullable final T expected) {
        return new TypeSafeMatcher<T>() {
            @Override
            public boolean matchesSafely(T item) {
                // noinspection ObjectEquality
                return item == expected;
            }

            @Override
            public void describeTo(@Nonnull Description description) {
                description.appendText("is same as ").appendValue(expected);
            }
        };
    }

    @Nonnull
    public static <T> Matcher<T> isEqualTo(@Nullable final T expected) {
        return CoreMatchers.is(expected);
    }

    @Nonnull
    public static <T> Matcher<T> is(@Nullable final T expected) {
        return isEqualTo(expected);
    }

    @Nonnull
    public static <T> Matcher<T> isNot(@Nullable final T expected) {
        return not(is(expected));
    }

    @Nonnull
    public static <T> Matcher<T> isOneOf(@Nonnull final T firstExpected, @Nullable final T... others) {
        final Set<T> them = new HashSet<>();
        them.add(firstExpected);
        if (others != null) {
            them.addAll(asList(others));
        }
        return isOneOf(them);
    }

    @Nonnull
    public static <T> Matcher<T> isOneOf(@Nonnull final Collection<T> them) {
        if (them.isEmpty()) {
            throw new IllegalArgumentException("There is no item provided.");
        }
        return new TypeSafeMatcher<T>() {
            @Override
            public boolean matchesSafely(T t) {
                return them.contains(t);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is one of ").appendValue(them);
            }
        };
    }

    @Nonnull
    public static <T> Matcher<T> equalTo(@Nullable final T expected) {
        return isEqualTo(expected);
    }

    @Nonnull
    public static Matcher<Object> isNull() {
        return IsNull.nullValue();
    }

    @Nonnull
    public static Matcher<Object> isNotNull() {
        return IsNull.notNullValue();
    }
    
    @Nonnull
    public static Matcher<Boolean> isTrue() {
        return isEqualTo(true);
    }

    @Nonnull 
    public static Matcher<Boolean> isFalse() {
        return isEqualTo(false);
    }
    
    @Nonnull
    public static <T> Matcher<T> not(Matcher<T> anotherMatcher) {
        return IsNot.not(anotherMatcher);
    }
    
    @Nonnull
    public static <T> Matcher<T> isEmpty() {
        return new TypeSafeMatcher<T>() {
            @Override
            public boolean matchesSafely(Object item) {
                final boolean result;
                if (item == null) {
                    result = true;
                } else if (item instanceof Collection) {
                    result = ((Collection<?>)item).isEmpty();
                } else if (item instanceof Iterable) {
                    result = !((Iterable<?>)item).iterator().hasNext();
                } else if (item instanceof Map) {
                    result = ((Map<?, ?>)item).isEmpty();
                } else if (item instanceof Object[]) {
                    result = ((Object[])item).length == 0;
                } else if (item instanceof CharSequence) {
                    result = ((CharSequence)item).length() == 0;
                } else {
                    throw new IllegalArgumentException("Could not handle an argument " + item);
                }
                return result;
            }

            @Override
            public void describeTo(Description description) {
                description.appendValue("is empty");
            }
        };
    }

    @Nonnull
    public static Matcher<Object> hasNoItems() {
        return isEmpty();
    }
    
    @Nonnull
    public static Matcher<Object> hasItems() {
        return new BaseMatcher<Object>() {
            @Override
            public boolean matches(Object item) {
                final boolean result;
                if (item == null) {
                    result = false;
                } else if (item instanceof Collection) {
                    result = !((Collection<?>)item).isEmpty();
                } else if (item instanceof Iterable) {
                    result = ((Iterable<?>)item).iterator().hasNext();
                } else if (item instanceof Map) {
                    result = !((Map<?, ?>)item).isEmpty();
                } else if (item instanceof Object[]) {
                    result = ((Object[])item).length > 0;
                } else if (item instanceof CharSequence) {
                    result = ((CharSequence)item).length() > 0;
                } else {
                    throw new IllegalArgumentException("Could not handle an argument " + item);
                }
                return result;
            }

            @Override
            public void describeTo(Description description) {
                description.appendValue("has items");
            }
        };
    }

    @Nonnull
    public static Matcher<Object> isNotEmpty() {
        return hasItems();
    }

    @Nonnull
    public static <T> Matcher<T> isGreaterThan(@Nonnull final T what) {
        return new TypeSafeMatcher<T>() {

            @Override
            public boolean matchesSafely(T value) {
                final boolean result;
                if (what instanceof Byte) {
                    result = ((Byte)value) > ((Byte)what);
                } else if (what instanceof Character) {
                    result = ((Character)value) > ((Character)what);
                } else if (what instanceof Short) {
                    result = ((Short)value) > ((Short)what);
                } else if (what instanceof Integer) {
                    result = ((Integer)value) > ((Integer)what);
                } else if (what instanceof Long) {
                    result = ((Long)value) > ((Long)what);
                } else if (what instanceof Float) {
                    result = ((Float)value) > ((Float)what);
                } else if (what instanceof Double) {
                    result = ((Double)value) > ((Double)what);
                } else if (what instanceof Date) {
                    result = ((Date)value).after((Date)what);
                } else if (what instanceof Duration) {
                    result = ((Duration)value).isGreaterThan((Duration)what);
                } else {
                    result = false;
                }
                return result;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is greater than ").appendValue(what);
            }
        };
    }

    @Nonnull
    public static <T> Matcher<T> isGreaterThanOrEqualTo(@Nonnull final T what) {
        return new TypeSafeMatcher<T>() {

            @Override
            public boolean matchesSafely(T value) {
                final boolean result;
                if (what instanceof Byte) {
                    result = ((Byte)value) >= ((Byte)what);
                } else if (what instanceof Character) {
                    result = ((Character)value) >= ((Character)what);
                } else if (what instanceof Short) {
                    result = ((Short)value) >= ((Short)what);
                } else if (what instanceof Integer) {
                    result = ((Integer)value) >= ((Integer)what);
                } else if (what instanceof Long) {
                    result = ((Long)value) >= ((Long)what);
                } else if (what instanceof Float) {
                    result = ((Float)value) >= ((Float)what);
                } else if (what instanceof Double) {
                    result = ((Double)value) >= ((Double)what);
                } else if (what instanceof Date) {
                    result = ((Date)value).after((Date)what) || value.equals(what);
                } else if (what instanceof Duration) {
                    result = ((Duration)value).isGreaterThanOrEqualTo((Duration) what);
                } else {
                    result = false;
                }
                return result;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is greater than or equal to ").appendValue(what);
            }
        };
    }

    @Nonnull
    public static <T> Matcher<T> isLessThan(@Nonnull final T what) {
        return new TypeSafeMatcher<T>() {

            @Override
            public boolean matchesSafely(T value) {
                final boolean result;
                if (what instanceof Byte) {
                    result = ((Byte)value) < ((Byte)what);
                } else if (what instanceof Character) {
                    result = ((Character)value) < ((Character)what);
                } else if (what instanceof Short) {
                    result = ((Short)value) < ((Short)what);
                } else if (what instanceof Integer) {
                    result = ((Integer)value) < ((Integer)what);
                } else if (what instanceof Long) {
                    result = ((Long)value) < ((Long)what);
                } else if (what instanceof Float) {
                    result = ((Float)value) < ((Float)what);
                } else if (what instanceof Double) {
                    result = ((Double)value) < ((Double)what);
                } else if (what instanceof Date) {
                    result = ((Date)value).before((Date) what);
                } else if (what instanceof Duration) {
                    result = ((Duration)value).isLessThan((Duration) what);
                } else {
                    result = false;
                }
                return result;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is less than ").appendValue(what);
            }
        };
    }

    @Nonnull
    public static <T> Matcher<T> isLessThanOrEqualTo(@Nonnull final T what) {
        return new TypeSafeMatcher<T>() {

            @Override
            public boolean matchesSafely(T value) {
                final boolean result;
                if (what instanceof Byte) {
                    result = ((Byte)value) <= ((Byte)what);
                } else if (what instanceof Character) {
                    result = ((Character)value) <= ((Character)what);
                } else if (what instanceof Short) {
                    result = ((Short)value) <= ((Short)what);
                } else if (what instanceof Integer) {
                    result = ((Integer)value) <= ((Integer)what);
                } else if (what instanceof Long) {
                    result = ((Long)value) <= ((Long)what);
                } else if (what instanceof Float) {
                    result = ((Float)value) <= ((Float)what);
                } else if (what instanceof Double) {
                    result = ((Double)value) <= ((Double)what);
                } else if (what instanceof Date) {
                    result = ((Date)value).before((Date) what) || value.equals(what);
                } else if (what instanceof Duration) {
                    result = ((Duration)value).isLessThanOrEqualTo((Duration) what);
                } else {
                    result = false;
                }
                return result;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is less than or equal to ").appendValue(what);
            }
        };
    }

    @Nonnull
    public static <T> Matcher<T> isInstanceOf(@Nonnull final Class<?> what) {
        return new BaseMatcher<T>() {
            @Override
            public boolean matches(Object o) {
                return what.isInstance(o);
            }

            @Override
            public void describeMismatch(@Nullable Object actual, @Nonnull Description description) {
                description.appendValue(actual != null ? actual.getClass().getName() : null);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is instance of ").appendValue(what.getName());
            }
        };
    }

    @Nonnull
    public static <T> Matcher<T> hasSize(@Nonnegative final int size) {
        return new BaseMatcher<T>() {

            @Override
            public boolean matches(@Nullable Object item) {
                final boolean result;
                if (item != null) {
                    result = size == getSizeOf(item);
                } else {
                    result = false;
                }
                return result;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("has size of ").appendValue(size);
            }

            @Override
            public void describeMismatch(@Nullable Object actual, @Nonnull Description description) {
                handleDiscribeSizeMismatch(actual, description);
            }

        };
    }

    @Nonnull
    public static <T> Matcher<T> hasSameSizeAs(@Nullable final Object what) {
        return new BaseMatcher<T>() {

            @Override
            public boolean matches(@Nullable Object item) {
                final int size = what != null ? getSizeOf(what) : 0;
                return size == 0 ? (item == null || getSizeOf(item) == 0) : (item != null && getSizeOf(item) == size);
            }

            @Override
            public void describeTo(Description description) {
                final int size = what != null ? getSizeOf(what) : 0;
                description.appendText("has size of ").appendValue(size);
            }

            @Override
            public void describeMismatch(@Nullable Object actual, @Nonnull Description description) {
                handleDiscribeSizeMismatch(actual, description);
            }

        };
    }

    @Nonnull
    public static <T> Matcher<T> applies(@Nonnull final Predicate<T> predicate) {
        return new BaseMatcher<T>() {

            @Override
            public boolean matches(@Nullable Object item) {
                // noinspection unchecked
                return predicate.apply((T) item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("applies ").appendValue(predicate);
            }

        };
    }

    @Nonnegative
    protected static int getSizeOf(@Nullable Object what) {
        final int result;
        if (what == null) {
            result = 0;
        } else if (what instanceof Collection) {
            result = ((Collection) what).size();
        } else if (what instanceof Map) {
            result = ((Map) what).size();
        } else if (what instanceof Object[]) {
            result = ((Object[]) what).length;
        } else if (what instanceof CharSequence) {
            result = ((CharSequence) what).length();
        } else {
            throw new IllegalArgumentException("Could not get size of " + what + ".");
        }
        return result;
    }

    protected static void handleDiscribeSizeMismatch(@Nullable Object actual, @Nonnull Description description) {
        description.appendText("was ");
        if (actual != null) {
            description.appendValue(null);
        } else {
            description.appendValue(getSizeOf(actual));
            if (actual instanceof Map || actual instanceof Collection) {
                description.appendText(" (Values: ").appendValue(actual).appendText(")");
            } else if (actual instanceof Object[]) {
                description.appendText(" (Values: ").appendValue(Arrays.toString((Object[]) actual)).appendText(")");
            } else if (actual instanceof CharSequence) {
                description.appendText(" (Content: ").appendValue(actual).appendText(")");
            }
        }
    }

    private BaseMatchers() {}
}

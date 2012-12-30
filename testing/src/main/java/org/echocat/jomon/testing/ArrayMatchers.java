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

package org.echocat.jomon.testing;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import static com.google.common.collect.Iterators.forArray;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.ArrayUtils.contains;

public class ArrayMatchers {


    @SafeVarargs
    @Nonnull
    public static <T> Matcher<T[]> isEqualTo(@Nullable T... expectedItems) {
        return isEqualTo(expectedItems != null ? asList(expectedItems) : Collections.<T>emptyList());
    }

    @Nonnull
    public static <T> Matcher<T[]> isEqualTo(@Nonnull final Iterable<T> expectedItems) {
        return new TypeSafeMatcher<T[]>() {
            @Override
            public boolean matchesSafely(T[] items) {
                boolean result;
                if (items != null) {
                    result = true;
                    final Iterator<T> expectedItemsIterator = expectedItems.iterator();
                    final Iterator<T> itemsIterator = forArray(items);
                    while (expectedItemsIterator.hasNext() && itemsIterator.hasNext() && result) {
                        final T expectedItem = expectedItemsIterator.next();
                        final T item = itemsIterator.next();
                        result = expectedItem != null ? expectedItem.equals(item) : item == null;
                    }
                    if (result) {
                        result = !expectedItemsIterator.hasNext() && !itemsIterator.hasNext();
                    }
                } else {
                    result = false;
                }
                return result;
            }

            @Override
            public void describeTo(Description description) {
                description.appendValue("is equal to ").appendValue(expectedItems);
            }
        };
    }

    @SafeVarargs
    @Nonnull
    public static <T> Matcher<T[]> containsAllItemsOf(@Nullable T... expectedItems) {
        return containsAllItemsOf(expectedItems != null ? asList(expectedItems) : Collections.<T>emptyList());
    }

    @Nonnull
    public static <T> Matcher<T[]> containsAllItemsOf(@Nonnull final Collection<T> expectedItems) {
        return new TypeSafeMatcher<T[]>() {
            @Override
            public boolean matchesSafely(T[] items) {
                boolean result;
                if (items != null) {
                    if (expectedItems.size() == items.length) {
                        final Iterator<T> i = expectedItems.iterator();
                        result = true;
                        while (i.hasNext() & result) {
                            final T expectedItem = i.next();
                            result = contains(items, expectedItem);
                        }
                    } else {
                        result = false;
                    }
                } else {
                    result = false;
                }
                return result;
            }

            @Override
            public void describeTo(Description description) {
                description.appendValue("contains all items ").appendValue(expectedItems);
            }
        };
    }

    @SafeVarargs
    @Nonnull
    public static <T> Matcher<T[]> containsItemsOf(@Nullable T... expectedItems) {
        return containsItemsOf(expectedItems != null ? asList(expectedItems) : Collections.<T>emptyList());
    }

    @Nonnull
    public static <T> Matcher<T[]> containsItemsOf(@Nonnull final Collection<T> expectedItems) {
        return new TypeSafeMatcher<T[]>() {
            @Override
            public boolean matchesSafely(T[] items) {
                boolean result;
                if (items != null) {
                    final Iterator<T> i = expectedItems.iterator();
                    result = true;
                    while (i.hasNext() & result) {
                        final T expectedItem = i.next();
                        result = contains(items, expectedItem);
                    }
                } else {
                    result = false;
                }
                return result;
            }

            @Override
            public void describeTo(Description description) {
                description.appendValue("contains items ").appendValue(expectedItems);
            }
        };
    }

    @Nonnull
    public static <T> Matcher<T[]> hasSize(@Nonnegative final int size) {
        return new TypeSafeMatcherWithActual<T[]>() {

            @Override
            public boolean matchesSafely(@Nullable T[] item) {
                return size == 0 ? (item == null || item.length == 0) : (item != null && size == item.length);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("has size ").appendValue(size);
            }

            @Override
            public void describeExpectedTo(@Nonnull Description description, @Nullable T[] actual) {
                description.appendValue(actual != null ? actual.length : 0).appendText(" (Values: ").appendValue(actual).appendText(")");
            }
        };
    }

    @Nonnull
    public static <T> Matcher<T[]> hasSameSizeAs(@Nullable final Object what) {
        return new TypeSafeMatcherWithActual<T[]>() {

            @Override
            public boolean matchesSafely(@Nullable T[] item) {
                return getSizeOf(what) == 0 ? (item == null || item.length == 0) : (item != null && getSizeOf(what) == item.length);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("has size ").appendValue(what != null ? getSizeOf(what) : 0).appendText(" (Same as: ").appendValue(what).appendText(")");
            }

            @Override
            public void describeExpectedTo(@Nonnull Description description, @Nullable T[] actual) {
                description.appendValue(actual != null ? actual.length : 0).appendText(" (Values: ").appendValue(actual).appendText(")");
            }

            private int getSizeOf(@Nullable Object what) {
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
        };
    }

    private ArrayMatchers() {}
}

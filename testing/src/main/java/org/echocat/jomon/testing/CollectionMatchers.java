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

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static java.util.Arrays.asList;

public class CollectionMatchers {


    @SafeVarargs
    @Nonnull
    public static <T> Matcher<Iterable<T>> isEqualTo(@Nullable T... expectedItems) {
        return isEqualTo(expectedItems != null ? asList(expectedItems) : Collections.<T>emptyList());
    }
    
    @Nonnull
    public static <T> Matcher<Iterable<T>> isEqualTo(@Nonnull final Iterable<T> expectedItems) {
        return new TypeSafeMatcher<Iterable<T>>() {
            @Override
            public boolean matchesSafely(Iterable<T> items) {
                boolean result;
                if (items != null) {
                    result = true;
                    final Iterator<T> expectedItemsIterator = expectedItems.iterator();
                    final Iterator<T> itemsIterator = items.iterator();
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
    public static <T> Matcher<Collection<T>> containsAllItemsOf(@Nullable T... expectedItems) {
        return containsAllItemsOf(expectedItems != null ? asList(expectedItems) : Collections.<T>emptyList());
    }

    @Nonnull
    public static <T> Matcher<Collection<T>> containsAllItemsOf(@Nonnull final Collection<T> expectedItems) {
        return new TypeSafeMatcher<Collection<T>>() {
            @Override
            public boolean matchesSafely(Collection<T> items) {
                boolean result;
                if (items != null) {
                    if (expectedItems.size() == items.size()) {
                        final Iterator<T> i = expectedItems.iterator();
                        result = true;
                        while (i.hasNext() & result) {
                            final T expectedItem = i.next();
                            result = items.contains(expectedItem);
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
    public static <T> Matcher<Collection<T>> containsItemsOf(@Nullable T... expectedItems) {
        return containsItemsOf(expectedItems != null ? asList(expectedItems) : Collections.<T>emptyList());
    }

    @Nonnull
    public static <T> Matcher<Collection<T>> containsItemsOf(@Nonnull final Collection<T> expectedItems) {
        return new TypeSafeMatcher<Collection<T>>() {
            @Override
            public boolean matchesSafely(Collection<T> items) {
                boolean result;
                if (items != null) {
                    final Iterator<T> i = expectedItems.iterator();
                    result = true;
                    while (i.hasNext() & result) {
                        final T expectedItem = i.next();
                        result = items.contains(expectedItem);
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
    public static Matcher<Collection<?>> hasSize(@Nonnegative final int size) {
        return BaseMatchers.hasSize(size);
    }

    @Nonnull
    public static Matcher<Collection<?>> hasSameSizeAs(@Nullable final Object what) {
        return BaseMatchers.hasSameSizeAs(what);
    }

    private CollectionMatchers() {}
}

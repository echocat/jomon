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
import org.hamcrest.TypeSafeMatcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static java.util.Arrays.asList;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

public class IteratorMatchers {

    @Nonnull
    public static Matcher<Iterator<?>> returnsNothing() {
        return new TypeSafeMatcher<Iterator<?>>() {
            @Override
            public boolean matchesSafely(Iterator<?> iterator) {
                try {
                    return iterator != null && !iterator.hasNext();
                } finally {
                    if (iterator instanceof AutoCloseable) {
                        closeQuietly((AutoCloseable)iterator);
                    }
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("returns nothing");
            }
        };
    }

    @SafeVarargs
    @Nonnull
    public static <T> Matcher<Iterator<T>> returnsItems(@Nullable T... expectedItems) {
        return returnsItems(expectedItems != null ? asList(expectedItems) : Collections.<T>emptyList());
    }

    @Nonnull
    public static <T> Matcher<Iterator<T>> returnsItems(@Nonnull final Iterable<T> expectedItems) {
        return new TypeSafeMatcher<Iterator<T>>() {
            @Override
            public boolean matchesSafely(Iterator<T> itemsIterator) {
                try {
                    boolean result;
                    if (itemsIterator != null) {
                        result = true;
                        final Iterator<T> expectedItemsIterator = expectedItems.iterator();
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
                } finally {
                    if (itemsIterator instanceof AutoCloseable) {
                        closeQuietly((AutoCloseable)itemsIterator);
                    }
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is equal to ").appendValue(expectedItems);
            }
        };
    }

    @SafeVarargs
    @Nonnull
    public static <T> Matcher<Iterator<T>> returnsAllItemsOf(@Nullable T... expectedItems) {
        return returnsAllItemsOf(expectedItems != null ? asList(expectedItems) : Collections.<T>emptyList());
    }

    @Nonnull
    public static <T> Matcher<Iterator<T>> returnsAllItemsOf(@Nonnull final Collection<T> expectedItems) {
        return new TypeSafeMatcher<Iterator<T>>() {
            @Override
            public boolean matchesSafely(Iterator<T> itemsIterator) {
                try {
                    boolean result;
                    if (itemsIterator != null) {
                        result = true;
                        int index = 0;
                        while (itemsIterator.hasNext() && result) {
                            final T item = itemsIterator.next();
                            result = expectedItems.contains(item);
                            index++;
                        }
                        if (result) {
                            result = index == expectedItems.size();
                        }
                    } else {
                        result = false;
                    }
                    return result;
                } finally {
                    if (itemsIterator instanceof AutoCloseable) {
                        closeQuietly((AutoCloseable)itemsIterator);
                    }
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is equal to ").appendValue(expectedItems);
            }
        };
    }

    private IteratorMatchers() {}
}

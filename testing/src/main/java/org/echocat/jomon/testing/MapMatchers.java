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
import java.util.Map;
import java.util.Map.Entry;

public class MapMatchers {

    @Nonnull
    public static <K, V> Matcher<Map<K, V>> hasSameEntriesAs(@Nullable final Map<K, V> expected) {
        return new TypeSafeMatcher<Map<K, V>>() {
            @Override
            public boolean matchesSafely(Map<K, V> item) {
                boolean result;
                if (item == null && expected == null) {
                    result = true;
                } else if (item == null || expected == null || item.size() != expected.size()) {
                    result = false;
                } else {
                    result = true;
                    for (Entry<K, V> expectedKeyAndValue : expected.entrySet()) {
                        final K expectedKey = expectedKeyAndValue.getKey();
                        final V expectedValue = expectedKeyAndValue.getValue();
                        final V foundValue = item.get(expectedKey);
                        if (expectedValue != null ? expectedValue.equals(foundValue) : foundValue == null) {
                            result = true;
                        } else {
                            result = false;
                            break;
                        }
                    }
                }
                return result;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("has same entries as ").appendValue(expected);
            }
        };
    }

    @Nonnull
    public static Matcher<Map<?, ?>> hasSize(@Nonnegative final int size) {
        return new TypeSafeMatcherWithActual<Map<?, ?>>() {

            @Override
            public boolean matchesSafely(@Nullable Map<?, ?> item) {
                return size == 0 ? (item == null || item.isEmpty()) : (item != null && size == item.size());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("has size ").appendValue(size);
            }

            @Override
            public void describeExpectedTo(@Nonnull Description description, @Nullable Map<?, ?> actual) {
                description.appendValue(actual != null ? actual.size() : 0).appendText(" (Values: ").appendValue(actual).appendText(")");
            }
        };
    }

    @Nonnull
    public static Matcher<Map<?, ?>> hasSameSizeAs(@Nullable final Object what) {
        return new TypeSafeMatcherWithActual<Map<?, ?>>() {

            @Override
            public boolean matchesSafely(@Nullable Map<?, ?> item) {
                return getSizeOf(what) == 0 ? (item == null || item.isEmpty()) : (item != null && getSizeOf(what) == item.size());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("has size ").appendValue(what != null ? getSizeOf(what) : 0).appendText(" (Same as: ").appendValue(what).appendText(")");
            }

            @Override
            public void describeExpectedTo(@Nonnull Description description, @Nullable Map<?, ?> actual) {
                description.appendValue(actual != null ? actual.size() : 0).appendText(" (Values: ").appendValue(actual).appendText(")");
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

    private MapMatchers() {}

}

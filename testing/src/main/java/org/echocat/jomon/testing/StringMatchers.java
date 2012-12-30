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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StringMatchers {


    @Nonnull
    public static Matcher<String> startsWith(@Nonnull final String expectedStart) {
        return new TypeSafeMatcher<String>() {
            @Override
            public boolean matchesSafely(String item) {
                return item != null && item.startsWith(expectedStart);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("starts with ").appendValue(expectedStart);
            }
        };
    }

    @Nonnull
    public static Matcher<String> endsWith(@Nonnull final String expectedEnd) {
        return new TypeSafeMatcher<String>() {
            @Override
            public boolean matchesSafely(String item) {
                return item != null && item.endsWith(expectedEnd);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("starts with ").appendValue(expectedEnd);
            }
        };
    }

    @Nonnull
    public static Matcher<String> asTrimmedContentEqualsTo(@Nullable final String expected) {
        return new TypeSafeMatcher<String>() {
            @Override
            public boolean matchesSafely(String item) {
                final boolean result;
                if (expected != null) {
                    if (item != null) {
                        result = expected.trim().equals(item.trim());
                    } else {
                        result = false;
                    }
                } else {
                    result = item == null;
                }
                return result;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("as trimmed content equals to ").appendValue(expected);
            }
        };
    }

    @Nonnull
    public static Matcher<String> contains(@Nullable final String expected) {
        return new TypeSafeMatcher<String>() {
            @Override
            public boolean matchesSafely(String item) {
                final boolean result;
                if (expected != null) {
                    if (item != null) {
                        result = item.contains(expected);
                    } else {
                        result = false;
                    }
                } else {
                    result = item == null;
                }
                return result;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("contains ").appendValue(expected);
            }
        };
    }
    
    private StringMatchers() {}
}

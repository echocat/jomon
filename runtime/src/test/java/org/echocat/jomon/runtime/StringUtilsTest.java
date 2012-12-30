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

package org.echocat.jomon.runtime;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class StringUtilsTest {

    @Test
    public void testContainsNone() {
        assertThat(StringUtils.containsNoneOf("abcd", "ac", "ad", "bd", "ba", "ca", "da"), is(true));
        assertThat(StringUtils.containsNoneOf("abcd", "ac", "ad", "bd", "ba", "ca", "da", "cd"), is(false));
        assertThat(StringUtils.containsNoneOf("abcd", "ac", "ad", "bd", "ba", "ca", "da", "cd", "bc"), is(false));
        assertThat(StringUtils.containsNoneOf("abcd", ""), is(false));
        final String nullString = null;
        assertThat(StringUtils.containsNoneOf("abcd", nullString), is(true));
        assertThat(StringUtils.containsNoneOf(nullString, nullString), is(true));
    }

    @Test
    public void testContainsAny() {
        assertThat(StringUtils.containsAnyOf("abcd", "ac", "ad", "bd", "ba", "ca", "da"), is(false));
        assertThat(StringUtils.containsAnyOf("abcd", "ac", "ad", "bd", "ba", "ca", "da", "cd"), is(true));
        assertThat(StringUtils.containsAnyOf("abcd", "ac", "ad", "bd", "ba", "ca", "da", "cd", "bc"), is(true));
        assertThat(StringUtils.containsAnyOf("abcd", ""), is(true));
        final String nullString = null;
        assertThat(StringUtils.containsAnyOf("abcd", nullString), is(false));
        assertThat(StringUtils.containsAnyOf(nullString, nullString), is(false));
    }
}

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

package org.echocat.jomon.format;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.echocat.jomon.format.CombinedFormatProvider.formatProvider;
import static org.echocat.jomon.testing.Assert.assertThat;

public class CombinedFormatProviderUnitTest {

    @Test
    public void testFindSourceFormatBy() throws Exception {
        assertThat(formatProvider().findSourceFormatBy("textPlain"), isFormat("textPlain"));
        assertThat(formatProvider().findSourceFormatBy("html"), isFormat("html"));

        assertThat(formatProvider().findSourceFormatBy("confluence"), isFormat("confluence"));
        assertThat(formatProvider().findSourceFormatBy("mediaWiki"), isFormat("mediaWiki"));
        assertThat(formatProvider().findSourceFormatBy("textile"), isFormat("textile"));

        assertThat(formatProvider().findSourceFormatBy("foo"), isFormat(null));
    }

    @Test
    public void testFindTargetFormatBy() throws Exception {
        assertThat(formatProvider().findTargetFormatBy("textPlain"), isFormat("textPlain"));
        assertThat(formatProvider().findTargetFormatBy("html"), isFormat("html"));

        assertThat(formatProvider().findTargetFormatBy("confluence"), isFormat(null));
        assertThat(formatProvider().findTargetFormatBy("mediaWiki"), isFormat(null));
        assertThat(formatProvider().findTargetFormatBy("textile"), isFormat(null));

        assertThat(formatProvider().findTargetFormatBy("foo"), isFormat(null));
    }

    @Nonnull
    protected static Matcher<Format> isFormat(@Nullable final String name) {
        return new BaseMatcher<Format>() {


            @Override
            public boolean matches(@Nullable Object item) {
                final boolean result;
                if (name == null) {
                    result = item == null;
                } else {
                    result = item instanceof Format && name.equals(((Format)item).getName());
                }
                return result;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is format ").appendValue(name);
            }
        };
    }
}

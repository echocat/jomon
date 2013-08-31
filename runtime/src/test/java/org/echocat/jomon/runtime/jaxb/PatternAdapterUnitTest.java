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

package org.echocat.jomon.runtime.jaxb;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.*;
import static org.junit.Assert.assertThat;

public class PatternAdapterUnitTest {

    private static final PatternAdapter ADAPTER = new PatternAdapter();

    @Test
    public void testUnmarshal() throws Exception {
        assertThat(ADAPTER.unmarshal("/foo.*bar/"), is(compile("foo.*bar")));
        assertThat(ADAPTER.unmarshal("/foo.*bar/i"), is(compile("foo.*bar", CASE_INSENSITIVE)));
        assertThat(ADAPTER.unmarshal("/foo.*bar/m"), is(compile("foo.*bar", MULTILINE)));
        assertThat(ADAPTER.unmarshal("/foo.*bar/s"), is(compile("foo.*bar", DOTALL)));
        assertThat(ADAPTER.unmarshal("/foo.*bar/x"), is(compile("foo.*bar", COMMENTS)));
        assertThat(ADAPTER.unmarshal("/foo.*bar/xsmi"), is(compile("foo.*bar", CASE_INSENSITIVE | MULTILINE | DOTALL | COMMENTS)));
    }

    @Test
    public void testMarshal() throws Exception {
        assertThat(ADAPTER.marshal(compile("foo.*bar")), CoreMatchers.is("/foo.*bar/"));
        assertThat(ADAPTER.marshal(compile("foo.*bar", CASE_INSENSITIVE)), CoreMatchers.is("/foo.*bar/i"));
        assertThat(ADAPTER.marshal(compile("foo.*bar", MULTILINE)), CoreMatchers.is("/foo.*bar/m"));
        assertThat(ADAPTER.marshal(compile("foo.*bar", DOTALL)), CoreMatchers.is("/foo.*bar/s"));
        assertThat(ADAPTER.marshal(compile("foo.*bar", COMMENTS)), CoreMatchers.is("/foo.*bar/x"));
        assertThat(ADAPTER.marshal(compile("foo.*bar", CASE_INSENSITIVE | MULTILINE | DOTALL | COMMENTS)), CoreMatchers.is("/foo.*bar/imsx"));
    }

    @Nonnull
    protected static Matcher<Pattern> is(@Nonnull final Pattern expected) {
        return new TypeSafeMatcher<Pattern>() {
            @Override
            public boolean matchesSafely(Pattern item) {
                return item != null && expected.pattern().equals(item.pattern()) && expected.flags() == item.flags();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is ").appendValue(expected);
            }
        };
    }
}

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

package org.echocat.jomon.format.mylyn;

import org.echocat.jomon.format.Source;
import org.echocat.jomon.format.Source.Format.Impl;
import org.echocat.jomon.format.Target;
import org.echocat.jomon.testing.environments.LogEnvironment;
import org.junit.Rule;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MylynWikitextFormatterUnitTest {

    @Rule
    public final LogEnvironment _logEnvironment = new LogEnvironment();

    protected static final MylynWikitextFormatter FORMATTER = new MylynWikitextFormatter();

    @Test
    public void testConfluence2Plain() throws Exception {
        assertThat(format(sourceFormat("confluence"), "<b> *foo* </b>", Target.Format.textPlain), is("<b> foo </b>"));
    }

    @Test
    public void testConfluence2Html() throws Exception {
        assertThat(format(sourceFormat("confluence"), "<b> *foo* </b>", Target.Format.html), is("<p>&lt;b> <strong>foo</strong> &lt;/b></p>"));
    }

    @Test
    public void testTextile2Plain() throws Exception {
        assertThat(format(sourceFormat("textile"), "<b> *foo* </b>", Target.Format.textPlain), is("foo"));
    }

    @Test
    public void testTextile2Html() throws Exception {
        assertThat(format(sourceFormat("textile"), "<b> *foo* </b>", Target.Format.html), is("<p><b> <strong>foo</strong> </b></p>"));
    }

    @Test
    public void testMediaWiki2Plain() throws Exception {
        assertThat(format(sourceFormat("mediaWiki"), "<b> '''foo''' </b>", Target.Format.textPlain), is("foo"));
    }

    @Test
    public void testMediaWiki2Html() throws Exception {
        assertThat(format(sourceFormat("mediaWiki"), "<b> '''foo''' </b>", Target.Format.html), is("<p><b> <b>foo</b> </b></p>"));
    }

    @Nonnull
    protected static String format(@Nonnull Source.Format sourceFormat, @Nonnull String source, @Nonnull Target.Format targetFormat) throws IOException {
        return FORMATTER.format(sourceFormat, source, targetFormat);
    }

    @Nonnull
    protected static Impl sourceFormat(@Nonnull String name) {
        return new Impl(name);
    }
}

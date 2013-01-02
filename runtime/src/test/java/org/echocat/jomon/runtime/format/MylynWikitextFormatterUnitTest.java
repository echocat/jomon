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

package org.echocat.jomon.runtime.format;

import org.junit.Test;

import javax.annotation.Nonnull;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MylynWikitextFormatterUnitTest {

    protected static final MylynWikitextFormatter FORMATTER = new MylynWikitextFormatter();

    @Test
    public void testPlain2Plain() throws Exception {
        assertThat(format(Source.Format.plain, "<b> *foo* </b>", Target.Format.plain), is("<b> *foo* </b>"));
    }

    @Test
    public void testPlain2Html() throws Exception {
        assertThat(format(Source.Format.plain, "<b> *foo* </b>", Target.Format.html), is("&lt;b&gt; *foo* &lt;/b&gt;"));
    }

    @Test
    public void testHtml2Plain() throws Exception {
        assertThat(format(Source.Format.html, "<b> *foo* </b>", Target.Format.plain), is("*foo*"));
    }

    @Test
    public void testHtml2Html() throws Exception {
        assertThat(format(Source.Format.html, "<b> *foo* </b>", Target.Format.html), is("<b> *foo* </b>"));
    }

    @Test
    public void testConfluence2Plain() throws Exception {
        assertThat(format(Source.Format.confluence, "<b> *foo* </b>", Target.Format.plain), is("<b> foo </b>"));
    }

    @Test
    public void testConfluence2Html() throws Exception {
        assertThat(format(Source.Format.confluence, "<b> *foo* </b>", Target.Format.html), is("<p>&lt;b> <strong>foo</strong> &lt;/b></p>"));
    }

    @Test
    public void testTextile2Plain() throws Exception {
        assertThat(format(Source.Format.textile, "<b> *foo* </b>", Target.Format.plain), is("foo"));
    }

    @Test
    public void testTextile2Html() throws Exception {
        assertThat(format(Source.Format.textile, "<b> *foo* </b>", Target.Format.html), is("<p><b> <strong>foo</strong> </b></p>"));
    }

    @Test
    public void testMediaWiki2Plain() throws Exception {
        assertThat(format(Source.Format.mediaWiki, "<b> '''foo''' </b>", Target.Format.plain), is("foo"));
    }

    @Test
    public void testMediaWiki2Html() throws Exception {
        assertThat(format(Source.Format.mediaWiki, "<b> '''foo''' </b>", Target.Format.html), is("<p><b> <b>foo</b> </b></p>"));
    }

    @Nonnull
    protected static String format(@Nonnull Source.Format sourceFormat, @Nonnull String source, @Nonnull Target.Format targetFormat) throws IOException {
        return FORMATTER.format(sourceFormat, source, targetFormat);
    }
}

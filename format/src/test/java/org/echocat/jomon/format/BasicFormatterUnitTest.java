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

import org.echocat.jomon.testing.environments.LogEnvironment;
import org.junit.Rule;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class BasicFormatterUnitTest {

    @Rule
    public final LogEnvironment _logEnvironment = new LogEnvironment();

    protected static final Formatter FORMATTER = new BasicFormatter();

    @Test
    public void testPlain2Plain() throws Exception {
        assertThat(format(Source.Format.textPlain, "<b> *foo* </b>", Target.Format.textPlain), is("<b> *foo* </b>"));
    }

    @Test
    public void testPlain2Html() throws Exception {
        assertThat(format(Source.Format.textPlain, "<b> *foo* </b>", Target.Format.html), is("&lt;b&gt; *foo* &lt;/b&gt;"));
    }

    @Test
    public void testHtml2Plain() throws Exception {
        assertThat(format(Source.Format.html, "<b> *foo* </b>", Target.Format.textPlain), is("*foo*"));
    }

    @Test
    public void testHtml2Html() throws Exception {
        assertThat(format(Source.Format.html, "<b> *foo* </b>", Target.Format.html), is("<b> *foo* </b>"));
    }
    @Test
    public void testFoo2Plain() throws Exception {
        try {
            format(sourceFormat("foo"), "<b> *foo* </b>", Target.Format.textPlain);
            fail("Expected exception missing.");
        } catch (final IllegalArgumentException ignored) {}
    }

    @Test
    public void testPlain2Foo() throws Exception {
        try {
            format(Source.Format.textPlain, "<b> *foo* </b>", targetFormat("foo"));
            fail("Expected exception missing.");
        } catch (final IllegalArgumentException ignored) {}
    }

    @Nonnull
    protected static String format(@Nonnull Source.Format sourceFormat, @Nonnull String source, @Nonnull Target.Format targetFormat) throws IOException {
        return FORMATTER.format(sourceFormat, source, targetFormat);
    }

    @Nonnull
    protected static Source.Format sourceFormat(@Nonnull String name) {
        return new Source.Format.Impl(name);
    }

    @Nonnull
    protected static Target.Format targetFormat(@Nonnull String name) {
        return new Target.Format.Impl(name);
    }


}

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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class BasicFormatProviderTest {

    protected static final BasicFormatProvider PROVIDER = new BasicFormatProvider();

    @Test
    public void testFindSourceFormatBy() throws Exception {
        assertThat(PROVIDER.findSourceFormatBy("html"), is(Source.Format.html));
        assertThat(PROVIDER.findSourceFormatBy("textPlain"), is(Source.Format.textPlain));
        assertThat(PROVIDER.findSourceFormatBy("foo"), is((Source.Format) null));
    }

    @Test
    public void testFindTargetFormatBy() throws Exception {
        assertThat(PROVIDER.findTargetFormatBy("html"), is(Target.Format.html));
        assertThat(PROVIDER.findTargetFormatBy("textPlain"), is(Target.Format.textPlain));
        assertThat(PROVIDER.findTargetFormatBy("foo"), is((Target.Format) null));
    }
}

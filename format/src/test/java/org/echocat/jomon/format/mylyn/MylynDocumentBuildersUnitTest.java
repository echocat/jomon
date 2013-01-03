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

import org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;
import org.junit.Test;

import static org.echocat.jomon.format.Target.Format.html;
import static org.echocat.jomon.format.Target.Format.textPlain;
import static org.echocat.jomon.format.mylyn.MylynDocumentBuilders.mylynDocumentBuilders;
import static org.echocat.jomon.testing.Assert.assertThat;
import static org.echocat.jomon.testing.BaseMatchers.is;
import static org.echocat.jomon.testing.BaseMatchers.isNull;

public class MylynDocumentBuildersUnitTest {

    @Test
    public void testFindTypeFor() throws Exception {
        assertThat(mylynDocumentBuilders().findTypeFor(html), is((Class) HtmlDocumentBuilder.class));
        assertThat(mylynDocumentBuilders().findTypeFor(textPlain), isNull());
    }

    @Test
    public void testFindFormatBy() throws Exception {
        assertThat(mylynDocumentBuilders().findTargetFormatBy("html"), isNull());
        assertThat(mylynDocumentBuilders().findTargetFormatBy("plain"), isNull());

        assertThat(mylynDocumentBuilders().findSourceFormatBy("html"), isNull());
        assertThat(mylynDocumentBuilders().findSourceFormatBy("plain"), isNull());
        assertThat(mylynDocumentBuilders().findSourceFormatBy("confluence"), isNull());
        assertThat(mylynDocumentBuilders().findSourceFormatBy("mediaWiki"), isNull());
        assertThat(mylynDocumentBuilders().findSourceFormatBy("textile"), isNull());
    }
}

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

import org.echocat.jomon.format.Source.Format;
import org.echocat.jomon.format.Source.Format.Impl;
import org.eclipse.mylyn.wikitext.confluence.core.ConfluenceLanguage;
import org.eclipse.mylyn.wikitext.mediawiki.core.MediaWikiLanguage;
import org.eclipse.mylyn.wikitext.textile.core.TextileLanguage;
import org.junit.Test;

import javax.annotation.Nonnull;

import static org.echocat.jomon.format.Source.Format.html;
import static org.echocat.jomon.format.Source.Format.textPlain;
import static org.echocat.jomon.format.mylyn.MylynMarkupLanguages.mylynMarkupLanguages;
import static org.echocat.jomon.testing.Assert.assertThat;
import static org.echocat.jomon.testing.BaseMatchers.is;
import static org.echocat.jomon.testing.BaseMatchers.isNull;

public class MylynMarkupLanguagesUnitTest {

    @Test
    public void testFindMarkupLanguageType() throws Exception {
        assertThat(mylynMarkupLanguages().findTypeFor(html), isNull());
        assertThat(mylynMarkupLanguages().findTypeFor(textPlain), isNull());
        assertThat(mylynMarkupLanguages().findTypeFor(format("confluence")), is((Class) ConfluenceLanguage.class));
        assertThat(mylynMarkupLanguages().findTypeFor(format("mediaWiki")), is((Class) MediaWikiLanguage.class));
        assertThat(mylynMarkupLanguages().findTypeFor(format("textile")), is((Class) TextileLanguage.class));
    }

    @Test
    public void testFindFormatBy() throws Exception {
        assertThat(mylynMarkupLanguages().findTargetFormatBy("html"), isNull());
        assertThat(mylynMarkupLanguages().findTargetFormatBy("plain"), isNull());

        assertThat(mylynMarkupLanguages().findSourceFormatBy("html"), isNull());
        assertThat(mylynMarkupLanguages().findSourceFormatBy("plain"), isNull());
        assertThat(mylynMarkupLanguages().findSourceFormatBy("confluence"), is(format("confluence")));
        assertThat(mylynMarkupLanguages().findSourceFormatBy("mediaWiki"), is(format("mediaWiki")));
        assertThat(mylynMarkupLanguages().findSourceFormatBy("textile"), is(format("textile")));
    }

    @Nonnull
    protected static Format format(@Nonnull String name) {
        return new Impl(name);
    }
}

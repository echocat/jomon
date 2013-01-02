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

import net.htmlparser.jericho.MasonTagTypes;
import net.htmlparser.jericho.MicrosoftConditionalCommentTagTypes;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.TextExtractor;

import javax.annotation.Nonnull;
import java.io.*;

public class FormatUtils {

    static {
        MicrosoftConditionalCommentTagTypes.register();
        MasonTagTypes.register();
    }

    public static void htmlToPlainText(@Nonnull Reader from, @Nonnull Writer to) throws IOException {
        final Source source = new Source(from);
        source.fullSequentialParse();
        final TextExtractor extractor = source.getTextExtractor().setIncludeAttributes(true);
        extractor.writeTo(to);
    }

    @Nonnull
    public static String htmlToPlainText(@Nonnull String html) throws IOException {
        final StringReader reader = new StringReader(html);
        final StringWriter writer = new StringWriter();
        htmlToPlainText(reader, writer);
        return writer.toString();
    }

    private FormatUtils() {
    }

}

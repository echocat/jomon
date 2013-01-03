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

import org.apache.commons.io.IOUtils;
import org.echocat.jomon.runtime.util.Hints;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

import static org.apache.commons.lang3.StringEscapeUtils.ESCAPE_HTML4;

public class BasicFormatter extends FormatterSupport {

    @Override
    public void format(@Nonnull Source source, @Nonnull Target target, @Nullable Hints hints) throws IllegalArgumentException, IOException {
        final Source.Format sourceFormat = source.getFormat();
        final Target.Format targetFormat = target.getFormat();
        if ((sourceFormat.equals(Source.Format.textPlain) && targetFormat.equals(Target.Format.textPlain)) || (sourceFormat.equals(Source.Format.html) && targetFormat.equals(Target.Format.html))) {
            copy(source, target);
        } else if (sourceFormat.equals(Source.Format.html) && targetFormat.equals(Target.Format.textPlain)) {
            htmlToPlainText(source, target);
        } else if (sourceFormat.equals(Source.Format.textPlain) && targetFormat.equals(Target.Format.html)) {
            final String input = IOUtils.toString(source.getReader());
            ESCAPE_HTML4.translate(input, target.getWriter());
        } else {
            throw new IllegalArgumentException("Could not handle the combination of " + source + " and " + target + ".");
        }
    }

    protected void htmlToPlainText(@Nonnull Source from, @Nonnull Target to) throws IOException {
        FormatUtils.htmlToPlainText(from.getReader(), to.getWriter());
    }

    protected void copy(@Nonnull Source from, @Nonnull Target to) throws IOException {
        IOUtils.copy(from.getReader(), to.getWriter());
    }

    @Override
    public boolean canHandle(@Nonnull Source source, @Nonnull Target target, @Nullable Hints hints) {
        return canHandle(source) && canHandle(target);
    }

    protected boolean canHandle(@Nonnull Source source) {
        final Source.Format sourceFormat = source.getFormat();
        return Source.Format.html.equals(sourceFormat) || Source.Format.textPlain.equals(sourceFormat);
    }

    protected boolean canHandle(@Nonnull Target target) {
        final Target.Format targetFormat = target.getFormat();
        return Target.Format.html.equals(targetFormat) || Target.Format.textPlain.equals(targetFormat);
    }
}

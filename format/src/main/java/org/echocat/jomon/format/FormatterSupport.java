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

import org.echocat.jomon.runtime.util.Hints;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.StringWriter;

import static org.echocat.jomon.format.Source.sourceOf;
import static org.echocat.jomon.format.Target.targetOf;
import static org.echocat.jomon.runtime.util.Hints.EMPTY_HINTS;

public abstract class FormatterSupport implements Formatter {

    @Override
    public void format(@Nonnull Source source, @Nonnull Target target) throws IllegalArgumentException, IOException {
        format(source, target, EMPTY_HINTS);
    }

    @Override
    @Nonnull
    public String format(@Nonnull Source.Format sourceFormat, @Nonnull String source, @Nonnull Target.Format targetFormat) throws IllegalArgumentException, IOException {
        return format(sourceFormat, source, targetFormat, EMPTY_HINTS);
    }

    @Override
    @Nonnull
    public String format(@Nonnull Source.Format sourceFormat, @Nonnull String source, @Nonnull Target.Format targetFormat, @Nullable Hints hints) throws IllegalArgumentException, IOException {
        final StringWriter writer = new StringWriter();
        format(sourceOf(sourceFormat, source), targetOf(targetFormat, writer));
        return writer.toString();
    }

    @Override
    public boolean canHandle(@Nonnull Source source, @Nonnull Target target) {
        return canHandle(source, target, EMPTY_HINTS);
    }
}

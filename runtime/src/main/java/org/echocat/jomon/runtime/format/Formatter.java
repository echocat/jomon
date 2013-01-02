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

import org.echocat.jomon.runtime.util.Hints;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

public interface Formatter {

    public void format(@Nonnull Source source, @Nonnull Target target) throws IllegalArgumentException, IOException;

    public void format(@Nonnull Source source, @Nonnull Target target, @Nullable Hints hints) throws IllegalArgumentException, IOException;

    @Nonnull
    public String format(@Nonnull Source.Format sourceFormat, @Nonnull String source, @Nonnull Target.Format targetFormat) throws IllegalArgumentException, IOException;

    @Nonnull
    public String format(@Nonnull Source.Format sourceFormat, @Nonnull String source, @Nonnull Target.Format targetFormat, @Nullable Hints hints) throws IllegalArgumentException, IOException;

}

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

import static org.echocat.jomon.runtime.util.Hints.EMPTY_HINTS;

public class FormatProviderSupport implements FormatProvider {

    @Override
    @Nullable
    public Source.Format findSourceFormatBy(@Nonnull String name) {
        return findSourceFormatBy(name, EMPTY_HINTS);
    }

    @Override
    @Nullable
    public Source.Format findSourceFormatBy(@Nonnull String name, @Nullable Hints hints) {
        return null;
    }

    @Override
    @Nullable
    public Target.Format findTargetFormatBy(@Nonnull String name) {
        return findTargetFormatBy(name, EMPTY_HINTS);
    }

    @Override
    @Nullable
    public Target.Format findTargetFormatBy(@Nonnull String name, @Nullable Hints hints) {
        return null;
    }

}

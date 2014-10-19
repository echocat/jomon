/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012-2014 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.runtime.util;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.regex.Pattern;

public class IncludesExcludesPatternPredicate extends BaseIncludesExcludesPredicate<Pattern, String, IncludesExcludesPatternPredicate> {

    @Nonnull
    public static IncludesExcludesPatternPredicate includesExcludesPatternPredicate() {
        return new IncludesExcludesPatternPredicate();
    }

    @Nonnull
    public static IncludesExcludesPatternPredicate filteringPatternPredicate() {
        return includesExcludesPatternPredicate();
    }

    @Nonnull
    public static IncludesExcludesPatternPredicate patternPredicate() {
        return includesExcludesPatternPredicate();
    }

    @Override
    protected boolean isContained(@Nonnull String what, @Nonnull Collection<? extends Pattern> inFilterRule) {
        boolean result = false;
        for (final Pattern pattern : inFilterRule) {
            if (pattern.matcher(what).matches()) {
                result = true;
                break;
            }
        }
        return result;
    }

}

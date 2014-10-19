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

package org.echocat.jomon.runtime.reflection;

import org.echocat.jomon.runtime.util.BaseIncludesExcludesPredicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public class IncludesExcludesClassPredicate<V> extends BaseIncludesExcludesPredicate<Class<? extends V>, Class<? extends V>, IncludesExcludesClassPredicate<V>> {

    @Nonnull
    public static <T> IncludesExcludesClassPredicate<T> includesExcludesClassPredicate() {
        return new IncludesExcludesClassPredicate<>();
    }

    @Nonnull
    public static <T> IncludesExcludesClassPredicate<T> filteringClassPredicate() {
        return includesExcludesClassPredicate();
    }

    @Nonnull
    public static <T> IncludesExcludesClassPredicate<T> classPredicate() {
        return includesExcludesClassPredicate();
    }

    @Override
    protected boolean isIncluded(@Nonnull Class<? extends V> input) {
        final Collection<? extends Class<? extends V>> includes = getIncludes();
        boolean result;
        if (includes == null) {
            result = true;
        } else if (includes.contains(input)) {
            result = true;
        } else {
            result = false;
            for (final Class<? extends V> include : includes) {
                if (include.isAssignableFrom(input)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    @Override
    protected boolean isExcluded(@Nonnull Class<? extends V> input) {
        final Collection<? extends Class<? extends V>> excludes = getExcludes();
        boolean result;
        if (excludes == null) {
            result = false;
        } else if (excludes.contains(input)) {
            result = true;
        } else {
            result = false;
            for (final Class<? extends V> include : excludes) {
                if (include.isAssignableFrom(input)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    @Nullable
    @Override
    public Collection<Class<? extends V>> getIncludes() {
        // noinspection unchecked
        return (Collection<Class<? extends V>>) (Collection) super.getIncludes();
    }

    @Nullable
    @Override
    public Collection<Class<? extends V>> getExcludes() {
        // noinspection unchecked
        return (Collection<Class<? extends V>>) (Collection) super.getExcludes();
    }
}

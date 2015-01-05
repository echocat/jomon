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

import com.google.common.base.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

import static org.echocat.jomon.runtime.CollectionUtils.asImmutableList;

public class BaseIncludesExcludesPredicate<F, V, T extends BaseIncludesExcludesPredicate<F, V, T>> implements Predicate<V> {

    @Nullable
    private Collection<? extends F> _includes;
    @Nullable
    private Collection<? extends F> _excludes;

    protected boolean isIncluded(@Nonnull V input) {
        final Collection<? extends F> includes = getIncludes();
        return includes == null || includes.isEmpty() || isIncluded(input, includes);
    }

    protected boolean isIncluded(@Nonnull V what, @Nonnull Collection<? extends F> by) {
        return isContained(what, by);
    }

    protected boolean isExcluded(@Nonnull V input) {
        final Collection<? extends F> excludes = getExcludes();
        return excludes != null && !excludes.isEmpty() && isExcluded(input, excludes);
    }

    protected boolean isExcluded(@Nonnull V what, @Nonnull Collection<? extends F> by) {
        return isContained(what, by);
    }

    protected boolean isContained(@Nonnull V what, @Nonnull Collection<? extends F> inFilterRule) {
        // noinspection SuspiciousMethodCalls
        return inFilterRule.contains(what);
    }

    @Override
    public boolean apply(@Nullable V input) {
        return input != null
            && isIncluded(input)
            && !isExcluded(input);
    }

    @Nullable
    public Collection<? extends F> getIncludes() {
        return _includes;
    }

    public void setIncludes(@Nullable Collection<? extends F> includes) {
        _includes = includes;
    }

    @Nullable
    public Collection<? extends F> getExcludes() {
        return _excludes;
    }

    public void setExcludes(@Nullable Collection<? extends F> excludes) {
        _excludes = excludes;
    }

    @Nonnull
    public T including(@Nullable F... includes) {
        return including(includes != null ? asImmutableList(includes) : null);
    }

    @Nonnull
    public T including(@Nullable Collection<F> includes) {
        setIncludes(includes);
        return thisInstance();
    }

    @Nonnull
    public T excluding(@Nullable F... excludes) {
        return excluding(excludes != null ? asImmutableList(excludes) : null);
    }

    @Nonnull
    public T excluding(@Nullable Collection<F> includes) {
        setExcludes(includes);
        return thisInstance();
    }

    @Nonnull
    protected T thisInstance() {
        //noinspection unchecked
        return (T) this;
    }

}

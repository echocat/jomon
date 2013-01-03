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
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

import static java.util.Collections.unmodifiableList;
import static java.util.ServiceLoader.load;

@ThreadSafe
@Immutable
public class CombinedFormatProvider extends FormatProviderSupport {

    private static final CombinedFormatProvider INSTANCE = new CombinedFormatProvider();

    @Nonnull
    public static FormatProvider formatProvider() {
        return INSTANCE;
    }

    @Nonnull
    public static List<FormatProvider> getSystemProviders(@Nullable ClassLoader classLoader) {
        final ServiceLoader<FormatProvider> providers = classLoader != null ? load(FormatProvider.class, classLoader) : load(FormatProvider.class);
        final List<FormatProvider> result = new ArrayList<>();
        for (FormatProvider provider : providers) {
            result.add(provider);
        }
        return unmodifiableList(result);
    }


    @Nonnull
    private final List<FormatProvider> _delegates;

    public CombinedFormatProvider(@Nullable List<FormatProvider> delegates) {
        _delegates = delegates != null ? delegates : Collections.<FormatProvider>emptyList();
    }

    public CombinedFormatProvider(@Nullable ClassLoader classLoader) {
        this(getSystemProviders(classLoader));
    }

    public CombinedFormatProvider() {
        this((ClassLoader) null);
    }

    @Override
    public Source.Format findSourceFormatBy(@Nonnull String name, @Nullable Hints hints) {
        Source.Format result = null;
        for (FormatProvider delegate : _delegates) {
            result = delegate.findSourceFormatBy(name, hints);
            if (result != null) {
                break;
            }
        }
        return result;
    }

    @Override
    public Target.Format findTargetFormatBy(@Nonnull String name, @Nullable Hints hints) {
        Target.Format result = null;
        for (FormatProvider delegate : _delegates) {
            result = delegate.findTargetFormatBy(name, hints);
            if (result != null) {
                break;
            }
        }
        return result;
    }

    @Nonnull
    public List<FormatProvider> getDelegates() {
        return _delegates;
    }
}

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

import static java.util.Collections.unmodifiableList;
import static java.util.ServiceLoader.load;

@ThreadSafe
@Immutable
public class CombinedFormatter extends FormatterSupport {

    private static final CombinedFormatter INSTANCE = new CombinedFormatter();

    @Nonnull
    public static Formatter formatter() {
        return INSTANCE;
    }

    @Nonnull
    public static List<Formatter> getSystemFormatters(@Nullable ClassLoader classLoader) {
        final ServiceLoader<Formatter> formatters = classLoader != null ? load(Formatter.class, classLoader) : load(Formatter.class);
        final List<Formatter> result = new ArrayList<>();
        for (final Formatter formatter : formatters) {
            result.add(formatter);
        }
        return unmodifiableList(result);
    }

    @Nonnull
    private final List<Formatter> _delegates;

    public CombinedFormatter(@Nullable List<Formatter> delegates) {
        _delegates = delegates != null ? delegates : Collections.<Formatter>emptyList();
    }

    public CombinedFormatter(@Nullable ClassLoader classLoader) {
        this(getSystemFormatters(classLoader));
    }

    public CombinedFormatter() {
        this((ClassLoader) null);
    }

    @Override
    public void format(@Nonnull Source source, @Nonnull Target target, @Nullable Hints hints) throws IllegalArgumentException, IOException {
        for (final Formatter delegate : _delegates) {
            if (delegate.canHandle(source, target, hints)) {
                delegate.format(source, target, hints);
            }
        }
    }

    @Override
    public boolean canHandle(@Nonnull Source source, @Nonnull Target target, @Nullable Hints hints) {
        boolean result = false;
        for (final Formatter delegate : _delegates) {
            if (delegate.canHandle(source, target, hints)) {
                result = true;
                break;
            }
        }
        return result;
    }

    @Nonnull
    public List<Formatter> getDelegates() {
        return _delegates;
    }

}

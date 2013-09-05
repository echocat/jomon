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

package org.echocat.jomon.runtime.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public class StackedByThreadHints extends Hints {

    private final ThreadLocal<Deque<Hints>> _hintStack = new ThreadLocal<>();

    private final Hints _superHints;

    public StackedByThreadHints(@Nullable Hints superHints) {
        _superHints = superHints;
    }

    public StackedByThreadHints pushStackElement() {
        return pushStackElement(null);
    }

    public StackedByThreadHints pushStackElement(@Nullable Hints hints) {
        Deque<Hints> allHints = _hintStack.get();
        if (allHints == null) {
            allHints = new ArrayDeque<>();
            _hintStack.set(allHints);
        }
        allHints.addLast(_superHints != null ? new ExtendingHints(_superHints, hints) : hints);
        return this;
    }

    public StackedByThreadHints popStackElement() {
        final Deque<Hints> allHints = _hintStack.get();
        allHints.removeLast();
        if (allHints.isEmpty()) {
            _hintStack.remove();
        }
        return this;
    }

    @Override
    public <T> T get(@Nonnull Hint<T> hint, @Nullable T defaultValue) {
        return getCurrent().get(hint, defaultValue);
    }

    @Override
    public <T> void remove(@Nonnull Hint<T> hint) {
        getCurrent().remove(hint);
    }

    @Override
    public boolean isSet(@Nonnull Hint<?> hint) {
        return getCurrent().isSet(hint);
    }

    @Override
    public Iterator<Entry<Hint<?>, Object>> iterator() {
        return getCurrent().iterator();
    }

    @Nonnull
    protected Hints getCurrent() {
        final Deque<Hints> allHints = _hintStack.get();
        return allHints != null && !allHints.isEmpty() ? allHints.getLast() : _superHints;
    }

}

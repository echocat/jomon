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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;

import static org.echocat.jomon.runtime.CollectionUtils.asImmutableList;
import static org.echocat.jomon.runtime.CollectionUtils.asList;

@NotThreadSafe
public class ClassIterator implements Iterator<Class<?>> {

    @Nonnull
    public static Iterator<Class<?>> classIteratorFor(@Nonnull Class<?> clazz) {
        return new ClassIterator(clazz);
    }

    @Nonnull
    private final Deque<Iterator<Class<?>>> _stack = new ArrayDeque<>();
    @Nullable
    private Class<?> _next;

    public ClassIterator(@Nonnull Class<?> startingAt) {
        _stack.addLast(iteratorFor(startingAt));
        _next = startingAt;
    }

    @Override
    public boolean hasNext() {
        while (_next == null && !_stack.isEmpty()) {
            final Iterator<Class<?>> childrenIterator = _stack.getLast();
            if (childrenIterator.hasNext()) {
                _next = childrenIterator.next();
                _stack.addLast(iteratorFor(_next));
            } else {
                _stack.removeLast();
            }
        }
        return _next != null;
    }

    @Override
    public Class<?> next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        final Class<?> next = _next;
        _next = null;
        return next;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    protected Iterator<Class<?>> iteratorFor(@Nonnull Class<?> what) {
        final List<Class<?>> all = asList(what.getInterfaces());
        final Class<?> superClass = what.getSuperclass();
        if (superClass != null && !what.equals(superClass)) {
            all.add(superClass);
        }
        return asImmutableList(all).iterator();
    }

}

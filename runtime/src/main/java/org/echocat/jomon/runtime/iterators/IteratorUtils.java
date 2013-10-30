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

package org.echocat.jomon.runtime.iterators;

import com.google.common.base.Predicate;
import com.google.common.collect.AbstractIterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static com.google.common.collect.Iterators.forArray;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietlyIfAutoCloseable;

public class IteratorUtils {

    public static void loopOverAllElementsOf(@Nullable Iterator<?> i) {
        try {
            if (i != null) {
                while (i.hasNext()) {
                    i.next();
                }
            }
        } finally {
            if (i instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) i).close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Nonnull
    public static <T> CloseableIterator<T> emptyCloseableIterator() {
        return new CloseableIterator<T>() {
            @Override
            public void close() {
            }

            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public T next() {
                throw new NoSuchElementException();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Nonnull
    public static <T> CloseableIterator<T> toCloseableIterator(@Nonnull final Iterator<T> original) {
        return new CloseableIterator<T>() {
            @Override
            public void close() {
                closeQuietlyIfAutoCloseable(original);
            }

            @Override
            public boolean hasNext() {
                return original.hasNext();
            }

            @Override
            public T next() {
                return original.next();
            }

            @Override
            public void remove() {
                original.remove();
            }
        };
    }

    @Nonnull
    public static <T> CloseableIterator<T> toCloseableIterator(@Nonnull final Iterator<T> original, @Nullable final AutoCloseable toClose) {
        return new CloseableIterator<T>() {
            @Override
            public void close() {
                if (toClose != null) {
                    closeQuietly(toClose);
                }
            }

            @Override
            public boolean hasNext() {
                return original.hasNext();
            }

            @Override
            public T next() {
                return original.next();
            }

            @Override
            public void remove() {
                original.remove();
            }
        };
    }

    @SafeVarargs
    @Nonnull
    public static <T> CloseableIterator<T> toCloseableIterator(T... items) {
        final CloseableIterator<T> result;
        if (items == null || items.length == 0) {
            result = emptyCloseableIterator();
        } else {
            result = toCloseableIterator(forArray(items));
        }
        return result;
    }

    @Nonnull
    public static <T> CloseableIterator<T> filter(@Nonnull final Iterator<T> unfiltered, @Nonnull final Predicate<? super T> predicate) {
        final AbstractIterator<T> i = new AbstractIterator<T>() { @Override protected T computeNext() {
            T result = null;
            boolean found = false;
            while (unfiltered.hasNext() && !found) {
                final T element = unfiltered.next();
                if (predicate.apply(element)) {
                    found = true;
                    result = element;
                }
            }
            if (!found) {
                result = endOfData();
            }
            return result;
        }};
        return toCloseableIterator(i, unfiltered instanceof AutoCloseable ? (AutoCloseable) unfiltered : null);
    }

    private IteratorUtils() {}

}

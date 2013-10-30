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

package org.echocat.jomon.runtime;

import com.google.common.base.Predicate;
import org.apache.commons.collections15.MultiMap;
import org.echocat.jomon.runtime.iterators.CloseableIterator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.WillClose;
import java.util.*;

import static com.google.common.base.Predicates.equalTo;
import static java.util.Collections.*;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietlyIfAutoCloseable;

public class CollectionUtils {


    /**
     * Returns a the given map enriched
     * with the mappings <code>a[0] => a[1], a[2] => a[3], ...</code>.
     * @param a the elements to construct a {@link Map} from.
     * @return a {@link Map} constructed of the specified elements.
     */
    @Nonnull
    public static <K, V> Map<K, V> putAll(@Nonnull Map<K, V> original, @Nullable Object... a) {
        if (a != null) {
            final int length = a.length;
            if (length % 2 == 1) {
                throw new IllegalArgumentException("You must provide an even number of arguments.");
            }
            for (int i = 0; i < length; i += 2) {
                // noinspection unchecked
                original.put((K) a[i], (V) a[i + 1]);
            }
        }
        return original;
    }

    @Nonnull
    public static <K, V> Map<K, V> putAll(@Nonnull Map<K, V> original, @Nullable Map<K, V> other) {
        if (other != null) {
            original.putAll(other);
        }
        return original;
    }

    /**
     * Returns a {@link LinkedHashMap}
     * with the mappings <code>a[0] => a[1], a[2] => a[3], ...</code>.
     * @param a the elements to construct a {@link Map} from.
     * @return a {@link Map} constructed of the specified elements.
     */
    @Nonnull
    public static <K, V> Map<K, V> asMap(@Nullable Object... a) {
        return putAll(new LinkedHashMap<K, V>(), a);
    }

    /**
     * Returns a the given map enriched
     * with the mappings <code>a[0] => a[1], a[2] => a[3], ...</code>.
     * @param a the elements to construct a {@link Map} from.
     * @return a immutable {@link Map} constructed of the specified elements.
     */
    @Nonnull
    public static <K, V> Map<K, V> putAllAndMakeImmutable(@Nonnull Map<K, V> original, @Nullable Object... a) {
        return asImmutableMap(CollectionUtils.<K, V>putAll(original, a));
    }

    /**
     * Returns a {@link LinkedHashMap}
     * with the mappings <code>a[0] => a[1], a[2] => a[3], ...</code>.
     * @param a the elements to construct a {@link Map} from.
     * @return a immutable {@link Map} constructed of the specified elements.
     */
    @Nonnull
    public static <K, V> Map<K, V> asImmutableMap(@Nullable Object... a) {
        return putAllAndMakeImmutable(new LinkedHashMap<K, V>(), a);
    }

    @Nonnull
    public static <K, V> Map<K, V> asImmutableMap(@Nullable Map<K, V> map) {
        return map != null ? unmodifiableMap(map) : Collections.<K, V>emptyMap();
    }

    public static boolean isNotEmpty(@Nullable Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }

    public static boolean isNotEmpty(@Nullable Iterable<?> iterable) {
        return iterable != null && isNotEmpty(iterable.iterator());
    }

    public static boolean isNotEmpty(@Nullable Iterator<?> iterator) {
        return iterator != null && iterator.hasNext();
    }

    public static boolean isNotEmpty(@Nullable Map<?, ?> map) {
        return map != null && !map.isEmpty();
    }

    public static boolean isNotEmpty(@Nullable MultiMap<?, ?> map) {
        return map != null && !map.isEmpty();
    }

    public static boolean isEmpty(@Nullable Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isEmpty(@Nullable Iterable<?> iterable) {
        return iterable == null || isEmpty(iterable.iterator());
    }

    public static boolean isEmpty(@Nullable Iterator<?> iterator) {
        return iterator == null || !iterator.hasNext();
    }

    public static boolean isEmpty(@Nullable Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isEmpty(@Nullable MultiMap<?, ?> map) {
        return map == null || map.isEmpty();
    }

    @Nonnull
    public static <T, C extends Collection<T>> C addAll(@Nonnull C to, @Nullable T... elements) {
        if (elements != null) {
            Collections.addAll(to, elements);
        }
        return to;
    }

    @Nonnull
    public static <T, C extends Collection<T>> C addAll(@Nonnull C to, @Nullable Iterable<T> elements) {
        if (elements != null) {
            try {
                addAll(to, elements.iterator());
            } finally {
                closeQuietlyIfAutoCloseable(elements);
            }
        }
        return to;
    }

    @Nonnull
    public static <T, C extends Collection<T>> C addAll(@Nonnull C to, @Nullable Iterator<T> elements) {
        if (elements != null) {
            try {
                while (elements.hasNext()) {
                    to.add(elements.next());
                }
            } finally {
                closeQuietlyIfAutoCloseable(elements);
            }
        }
        return to;
    }

    /**
     * Returns a {@link List} containing the given <code>objects</code>,
     * returns an empty List, if <code>objects</code> is null.
     */
    @Nonnull
    public static <T> List<T> asList(@Nullable T... objects) {
        final List<T> result;
        if (objects == null) {
            result = new ArrayList<>();
        } else {
            final int initialCapacity = Math.max(16, ((objects.length + 2) / 3) * 4);
            result = new ArrayList<>(initialCapacity);
            result.addAll(new ArrayWrapper<>(objects));
        }
        return result;
    }

    /**
     * Returns an unmodifiable {@link List} containing the given <code>objects</code>,
     * returns an empty List, if <code>objects</code> is null.
     */
    @Nonnull
    public static <T> List<T> asImmutableList(@Nullable T... objects) {
        return unmodifiableList(asList(objects));
    }

    @Nonnull
    public static <T> List<T> addAllAndMakeImmutable(@Nonnull List<T> original, @Nullable T... objects) {
        return unmodifiableList(addAll(original, objects));
    }

    @Nonnull
    public static <T> List<T> asList(@Nullable Iterator<T> iterator) {
        final List<T> result = new ArrayList<>();
        try {
            if (iterator != null) {
                while (iterator.hasNext()) {
                    result.add(iterator.next());
                }
            }
        } finally {
            closeQuietlyIfAutoCloseable(iterator);
        }
        return result;
    }

    @Nonnull
    public static <T> List<T> asImmutableList(@Nullable Iterator<T> iterator) {
        return unmodifiableList(asList(iterator));
    }

    @Nonnull
    public static <T> List<T> addAllAndMakeImmutable(@Nonnull List<T> original, @Nullable Iterator<T> iterator) {
        return unmodifiableList(addAll(original, iterator));
    }

    @Nonnull
    public static <T> List<T> asList(@Nullable Iterable<T> in) {
        final List<T> result;
        if (in instanceof List) {
            result = (List<T>) in;
        } else if (in instanceof Collection) {
            result = new ArrayList<>((Collection<T>) in);
        } else {
            result = new ArrayList<>();
            addAll(result, in);
        }
        return result;
    }

    @Nonnull
    public static <T> List<T> asImmutableList(@Nullable Iterable<T> in) {
        return unmodifiableList(asList(in));
    }

    @Nonnull
    public static <T> List<T> addAllAndMakeImmutable(@Nonnull List<T> original, @Nullable Iterable<T> in) {
        return unmodifiableList(addAll(original, in));
    }

    /**
     * Returns a {@link Set} containing the given <code>objects</code>,
     * returns an empty Set, if <code>objects</code> is null.
     */
    @Nonnull
    public static <T> Set<T> asSet(@Nullable T... objects) {
        final Set<T> result;
        if (objects == null) {
            result = new LinkedHashSet<>();
        } else {
            final int initialCapacity = Math.max(16, ((objects.length + 2) / 3) * 4);
            result = new LinkedHashSet<>(initialCapacity);
            result.addAll(new ArrayWrapper<>(objects));
        }
        return result;
    }

    /**
     * Returns an unmodifiable {@link Set} containing the given <code>objects</code>,
     * returns an empty Set, if <code>objects</code> is null.
     */
    @Nonnull
    public static <T> Set<T> asImmutableSet(@Nullable T... objects) {
        return addAllAndMakeImmutable(new HashSet<T>(), objects);
    }

    /**
     * Returns an unmodifiable {@link Set} containing the given <code>objects</code>.
     */
    @Nonnull
    public static <T> Set<T> addAllAndMakeImmutable(@Nonnull Set<T> original, @Nullable T... objects) {
        return unmodifiableSet(addAll(original, objects));
    }


    @Nonnull
    public static <T> Set<T> asSet(@Nullable Iterator<T> iterator) {
        return addAll(new LinkedHashSet<T>(), iterator);
    }

    @Nonnull
    public static <T> Set<T> asImmutableSet(@Nullable Iterator<T> iterator) {
        return unmodifiableSet(asSet(iterator));
    }

    @Nonnull
    public static <T> Set<T> addAllAndMakeImmutable(@Nonnull Set<T> original, @Nullable Iterator<T> iterator) {
        return unmodifiableSet(addAll(original, iterator));
    }

    @Nonnull
    public static <T> Set<T> asSet(@Nullable Iterable<T> in) {
        final Set<T> result;
        if (in instanceof Set) {
            result = (Set<T>) in;
        } else if (in instanceof Collection) {
            result = new LinkedHashSet<>((Collection<T>) in);
        } else {
            result = addAll(new LinkedHashSet<T>(), in);
        }
        return result;
    }

    @Nonnull
    public static <T> Set<T> asImmutableSet(@Nullable Iterable<T> in) {
        return unmodifiableSet(asSet(in));
    }

    @Nonnull
    public static <T> Set<T> addAllAndMakeImmutable(@Nonnull Set<T> original, @Nullable Iterable<T> in) {
        return unmodifiableSet(addAll(original, in));
    }

    @Nonnull
    public static <T> CloseableIterator<T> asSingletonIterator(@Nullable final T element) {
        return new CloseableIterator<T>() {

            private boolean _elementFetched;

            @Override
            public boolean hasNext() {
                return !_elementFetched;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                _elementFetched = true;
                return element;
            }

            @Override public void remove() { throw new UnsupportedOperationException(); }
            @Override public void close() {}
        };
    }

    @Nonnull
    public static <T> CloseableIterator<T> asCloseableIterator(@Nullable final Iterator<T> iterator) {
        final CloseableIterator<T> result;
        if (iterator instanceof CloseableIterator) {
            result = (CloseableIterator<T>) iterator;
        } else if (iterator != null) {
            result = new CloseableIterator<T>() {
                @Override public void close() {}
                @Override public boolean hasNext() { return  iterator.hasNext(); }
                @Override public T next() { return iterator.next(); }
                @Override public void remove() { iterator.remove(); }
            };
        } else {
            result = emptyIterator();
        }
        return result;
    }

    @SafeVarargs
    @Nonnull
    public static <T> CloseableIterator<T> asIterator(@Nullable T... elements) {
        final CloseableIterator<T> result;
        if (elements != null) {
            final int length = elements.length;
            if (length == 0) {
                result = emptyIterator();
            } else if (length == 1) {
                result = asSingletonIterator(elements[0]);
            } else {
                result = asIterator(Arrays.asList(elements));
            }
        } else {
            result = emptyIterator();
        }
        return result;
    }

    @Nonnull
    public static <T> CloseableIterator<T> asIterator(@Nullable Iterable<T> elements) {
        final CloseableIterator<T> result;
        if (elements != null) {
            final Iterator<T> iterator = elements.iterator();
            if (iterator.hasNext()) {
                result = asCloseableIterator(iterator);
            } else {
                result = emptyIterator();
            }
        } else {
            result = emptyIterator();
        }
        return result;
    }

    @Nonnull
    public static <T> CloseableIterator<T> emptyIterator() {
        return new CloseableIterator<T>() {
            @Override public boolean hasNext() { return false; }
            @Override public T next() { throw new NoSuchElementException(); }
            @Override public void remove() { throw new UnsupportedOperationException(); }
            @Override public void close() {}
        };
    }

    public static <T> boolean oneApplies(@Nullable @WillClose Iterable<T> elements, @Nullable T predicate) {
        return elements != null && oneApplies(elements, equalTo(predicate));
    }

    public static <T> boolean oneApplies(@Nullable @WillClose Iterable<T> elements, @Nonnull Predicate<T> predicate) {
        return elements != null && oneApplies(elements.iterator(), predicate);
    }

    public static <T> boolean oneApplies(@Nullable @WillClose Iterable<T> elements, @Nullable final Collection<T> predicate) {
        return oneApplies(elements, new Predicate<T>() { @Override public boolean apply(@Nullable T input) {
            return predicate == null || predicate.contains(input);
        }});
    }

    public static <T> boolean oneApplies(@Nullable @WillClose Iterator<T> elements, @Nonnull T predicate) {
        return elements != null && oneApplies(elements, equalTo(predicate));
    }

    public static <T> boolean oneApplies(@Nullable @WillClose Iterator<T> elements, @Nonnull Predicate<T> predicate) {
        boolean result = false;
        if (elements != null) {
            try {
                while (elements.hasNext() && !result) {
                    result = predicate.apply(elements.next());
                }
            } finally {
                closeQuietlyIfAutoCloseable(elements);
            }
        }
        return result;
    }

    @Nonnegative
    public static <T> long countElementsOf(@Nullable Iterable<T> elements) {
        return countElementsOf(elements, null);
    }

    @Nonnegative
    public static <T> long countElementsOf(@Nullable Iterable<T> elements, @Nullable Predicate<T> matches) {
        return elements instanceof Collection && matches == null ? ((Collection) elements).size() : countElementsOf(elements.iterator(), matches);
    }

    @Nonnegative
    public static <T> long countElementsOf(@Nullable Iterator<T> iterator) {
        return countElementsOf(iterator, null);
    }

    @Nonnegative
    public static <T> long countElementsOf(@Nullable Iterator<T> iterator, @Nullable Predicate<T> matches) {
        long count = 0;
        try {
            if (iterator != null) {
                while (iterator.hasNext()) {
                    final T what = iterator.next();
                    if (matches == null || matches.apply(what)) {
                        count++;
                    }
                }
            }
        } finally {
            closeQuietlyIfAutoCloseable(iterator);
        }
        return count;
    }

    @Nullable
    public static <T> T findFirstOf(@Nullable Iterable<T> elements) {
        return elements != null ? findFirstOf(elements.iterator()) : null;
    }

    @Nullable
    public static <T> T findFirstOf(@Nullable Iterator<T> iterator) {
        T firstElement = null;
        try {
            if (iterator != null && iterator.hasNext()) {
                firstElement = iterator.next();
            }
        } finally {
            closeQuietlyIfAutoCloseable(iterator);
        }
        return firstElement;
    }

    @Nonnegative
    public static <T> long removeAllOf(@Nullable Iterable<T> elements) {
        return removeAllOf(elements, null);
    }

    @Nonnegative
    public static <T> long removeAllOf(@Nullable Iterable<T> elements, @Nullable Predicate<T> matching) {
        return removeAllOf(elements.iterator(), matching);
    }

    @Nonnegative
    public static <T> long removeAllOf(@Nullable Iterator<T> iterator) {
        return removeAllOf(iterator, null);
    }

    @Nonnegative
    public static <T> long removeAllOf(@Nullable Iterator<T> iterator, @Nullable Predicate<T> matching) {
        long removed = 0;
        try {
            if (iterator != null) {
                while (iterator.hasNext()) {
                    final T what = iterator.next();
                    if (matching == null || matching.apply(what)) {
                        iterator.remove();
                        removed++;
                    }
                }
            }
        } finally {
            closeQuietlyIfAutoCloseable(iterator);
        }
        return removed;
    }

    /**
     * This class is similar to the class which is returned by {@link Arrays#asList}
     * but does not clone the wrapped array if {@link #toArray} is called.
     */
    private static class ArrayWrapper<E> extends AbstractList<E> {
        private final E[] _wrappedArray;

        private ArrayWrapper(E[] arrayToWrap) {
            _wrappedArray = arrayToWrap;
        }

        @Override
        public E get(int index) {
            return _wrappedArray[index];
        }

        @Override
        public int size() {
            return _wrappedArray.length;
        }

        /**
         * Returns the wrapped array.
         */
        @Override
        public Object[] toArray() {
            return _wrappedArray;
        }
    }

    private CollectionUtils() {}
}

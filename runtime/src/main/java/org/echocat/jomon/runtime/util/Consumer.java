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

public interface Consumer<V, T extends Throwable> {

    public void consume(@Nullable V value) throws T;

    public static class Noop<V, T extends Throwable> implements Consumer<V, T> {

        @Nonnull
        private static final Noop<?, ?> INSTANCE = new Noop<>();

        @Nonnull
        public static <V, T extends Throwable> Consumer<V, T> consumeAndIgnore() {
            // noinspection unchecked
            return (Consumer<V, T>) INSTANCE;
        }

        @Override
        public void consume(@Nullable V value) throws T {}
    }

    public static class ExceptionThrowing<V, T extends Throwable> implements Consumer<V, T> {

        @Nonnull
        public static <V, T extends Throwable> Consumer<V, T> consumeAndThrow(@Nonnull T e) {
            return new ExceptionThrowing<>(e);
        }

        @Nonnull
        public static <V, T extends Throwable> Consumer<V, T> consumeAndThrow(@Nonnull Class<T> type) {
            return consumeAndThrow(type, null);
        }

        @Nonnull
        public static <V, T extends Throwable> Consumer<V, T> consumeAndThrow(@Nonnull Class<T> type, @Nullable String message) {
            return new ExceptionThrowing<>(type, message);
        }

        @Nullable
        private final T _throwable;
        @Nullable
        private final Class<T> _throwableType;
        @Nullable
        private final String _message;

        public ExceptionThrowing(@Nonnull T throwable) {
            _throwable = throwable;
            _throwableType = null;
            _message = null;
        }

        public ExceptionThrowing(@Nonnull Class<T> throwableType, @Nullable String message) {
            _throwableType = throwableType;
            _message = message;
            _throwable = null;
        }

        @Override
        public void consume(@Nullable V value) throws T {
            T target;
            if (_throwable != null) {
                target = _throwable;
            } else if (_throwableType != null) {
                try {
                    target = _throwableType.getConstructor(String.class).newInstance(_message);
                } catch (final NoSuchMethodException ignored) {
                    if (_message == null) {
                        //noinspection UnusedCatchParameter
                        try {
                            target = _throwableType.getConstructor().newInstance();
                        } catch (final NoSuchMethodException ignored2) {
                            // noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
                            throw new IllegalArgumentException("Could not find the constructor " + _throwableType.getName() + "." + _throwableType.getSimpleName() + "([message:" + String.class.getName() + "]).");
                        } catch (final Exception e) {
                            throw new RuntimeException("Could not create a exception of type '" + _throwableType.getName() + "'.", e);
                        }
                    } else {
                        throw new IllegalArgumentException("Could not find the constructor " + _throwableType.getName() + "." + _throwableType.getSimpleName() + "(message:" + String.class.getName() + ").");
                    }
                } catch (final Exception e) {
                    throw new RuntimeException("Could not create a exception of type '" + _throwableType.getName() + "' with message '" + _message + "'.", e);
                }
            } else {
                throw new IllegalStateException("This statement could not be. There must be throwable or a throwableType provided.");
            }
            throw target;
        }

    }

}

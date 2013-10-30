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

package org.echocat.jomon.process.daemon;

import com.google.common.base.Predicate;
import org.echocat.jomon.process.GeneratedProcess;
import org.echocat.jomon.process.listeners.startup.StartupListenerBasedRequirement;
import org.echocat.jomon.runtime.annotations.Excluding;
import org.echocat.jomon.runtime.annotations.Including;
import org.echocat.jomon.runtime.numbers.IntegerRange;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

import static org.echocat.jomon.runtime.CollectionUtils.asSet;

public interface ProcessDaemonRequirement<E, ID, P extends GeneratedProcess<E, ID>, T extends ProcessDaemon<E, ID, P, ?, ?>> extends StartupListenerBasedRequirement<E, ID, P> {

    public static final Predicate<Integer> DEFAULT_EXIT_CODE_VALIDATOR = new Predicate<Integer>() { @Override public boolean apply(@Nullable Integer input) {
        return input != null && input == 0;
    }};

    @Nonnull
    public Class<T> getType();

    @Nonnull
    public Predicate<Integer> getExitCodeValidator();

    public abstract static class Base<E, ID, P extends GeneratedProcess<E, ID>, T extends ProcessDaemon<E, ID, P, ?, ?>, B extends Base<E, ID, P, T, B>> extends StartupListenerBasedRequirement.Base<E, ID, P, B> implements ProcessDaemonRequirement<E, ID, P, T> {

        @Nonnull
        private final Class<T> _type;
        @Nonnull
        private Predicate<Integer> _exitCodeValidator = DEFAULT_EXIT_CODE_VALIDATOR;

        public Base(@Nonnull Class<T> type) {
            _type = type;
        }

        @Nonnull
        @Override
        public Class<T> getType() {
            return _type;
        }

        @Nonnull
        public B withExitCodeValidator(@Nonnull Predicate<Integer> validator) {
            _exitCodeValidator = validator;
            return thisObject();
        }

        @Nonnull
        public B withExitCodeValidatorThatMatches(@Nullable Integer exitCode) {
            return withExitCodeValidatorThatMatches(asSet(exitCode));
        }

        @Nonnull
        public B withExitCodeValidatorThatMatches(@Nullable Integer... exitCodes) {
            return withExitCodeValidatorThatMatches(asSet(exitCodes));
        }

        @Nonnull
        public B withExitCodeValidatorThatMatches(@Nullable final Iterable<Integer> exitCodes) {
            final Set<Integer> potentials = asSet(exitCodes);
            return withExitCodeValidator(new Predicate<Integer>() {
                @Override
                public boolean apply(@Nullable Integer input) {
                    return potentials.contains(input);
                }
            });
        }

        @Nonnull
        public B withExitCodeValidatorThatMatchesBetween(@Nullable @Including Integer from, @Nullable @Excluding Integer to) {
            return withExitCodeValidator(new IntegerRange(from, to));
        }

        @Override
        @Nonnull
        public Predicate<Integer> getExitCodeValidator() {
            return _exitCodeValidator;
        }

    }

}

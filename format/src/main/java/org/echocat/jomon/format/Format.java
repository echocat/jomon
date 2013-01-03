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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

public interface Format {

    @Nonnull
    public String getName();

    @Immutable
    @ThreadSafe
    public static class Impl {

        private final String _name;
        private final Class<? extends Format> _requiredFormat;

        public Impl(@Nonnull String name) {
            this(name, Format.class);
        }

        protected Impl(@Nonnull String name, @Nonnull Class<? extends Format> requiredFormat) {
            _name = name;
            _requiredFormat = requiredFormat;
        }

        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        @Override
        public boolean equals(Object o) {
            final boolean result;
            if (this == o) {
                result = true;
            } else if (!_requiredFormat.isInstance(o)) {
                result = false;
            } else {
                final Format that = (Format) o;
                result = getName().equals(that.getName());
            }
            return result;
        }

        @Override
        public int hashCode() {
            return getName().hashCode();
        }

        @Nonnull
        public String getName() {
            return _name;
        }

        @Override
        public String toString() {
            return getName();
        }
    }

}

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

package org.echocat.jomon.runtime;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.io.ObjectInput;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

@ThreadSafe
@Immutable
public class ImmutableMimeType extends MimeType {

    public static final ImmutableMimeType TEXT_HTML = immutableMimeType("text/html");
    public static final ImmutableMimeType TEXT_PLAIN = immutableMimeType("text/plain");
    public static final ImmutableMimeType APPLICATION_OCTET_STREAM = immutableMimeType("application/octet-stream");
    public static final ImmutableMimeType APPLICATION_FORM_URL_ENCODED = immutableMimeType("application/x-www-form-urlencoded");
    public static final ImmutableMimeType MULTIPART_FORM_DATA = immutableMimeType("multipart/form-data");

    @Nonnull
    public static Set<ImmutableMimeType> immutableMimeTypes(@Nullable String... rawDatas) throws IllegalArgumentException {
        final Set<ImmutableMimeType> result = new LinkedHashSet<>();
        if (rawDatas != null) {
            for (final String rawData : rawDatas) {
                result.add(immutableMimeType(rawData));
            }
        }
        return unmodifiableSet(result);
    }

    @Nonnull
    public static Set<ImmutableMimeType> immutableMimeTypes(@Nullable Iterable<String> rawDatas) throws IllegalArgumentException {
        final Set<ImmutableMimeType> result = new LinkedHashSet<>();
        if (rawDatas != null) {
            for (final String rawData : rawDatas) {
                result.add(immutableMimeType(rawData));
            }
        }
        return unmodifiableSet(result);
    }

    @Nonnull
    public static ImmutableMimeType immutableMimeType(@Nonnull String rawData) throws IllegalArgumentException {
        try {
            return new ImmutableMimeType(rawData);
        } catch (final MimeTypeParseException e) {
            throw new IllegalArgumentException(rawData, e);
        }
    }

    public ImmutableMimeType(@Nonnull String rawData) throws MimeTypeParseException {
        super(rawData);
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else if (!(o instanceof MimeType)) {
            result = false;
        } else {
            result = match((MimeType) o);
        }
        return result;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Nonnull
    public MimeType createModifiableCopy() {
        try {
            return new MimeType(toString());
        } catch (final MimeTypeParseException e) {
            throw new RuntimeException("Could not create a copy of my own (already parsed) rawData?", e);
        }
    }

    @Override public void setSubType(String sub) throws MimeTypeParseException { throw new UnsupportedOperationException(); }
    @Override public void setPrimaryType(String primary) throws MimeTypeParseException { throw new UnsupportedOperationException(); }
    @Override public void setParameter(String name, String value) { throw new UnsupportedOperationException(); }
    @Override public void removeParameter(String name) { throw new UnsupportedOperationException(); }
    @Override public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException { throw new UnsupportedOperationException(); }

}

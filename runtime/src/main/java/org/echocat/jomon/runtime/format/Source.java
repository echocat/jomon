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

package org.echocat.jomon.runtime.format;

import javax.annotation.Nonnull;
import java.io.Reader;
import java.io.StringReader;

import static org.echocat.jomon.runtime.format.Source.Format.plain;

public class Source {

    @Nonnull
    public static Source sourceOf(@Nonnull Format format, @Nonnull Reader reader) {
        return new Source(format, reader);
    }

    @Nonnull
    public static Source sourceOf(@Nonnull Reader reader) {
        return sourceOf(plain, reader);
    }

    @Nonnull
    public static Source sourceOf(@Nonnull Format format, @Nonnull String content) {
        return sourceOf(format, new StringReader(content));
    }

    @Nonnull
    public static Source sourceOf(@Nonnull String content) {
        return sourceOf(plain, content);
    }

    private final Format _format;
    private final Reader _reader;

    public Source(@Nonnull Format format, @Nonnull Reader reader) {
        _format = format;
        _reader = reader;
    }

    @Nonnull
    public Format getFormat() {
        return _format;
    }

    @Nonnull
    public Reader getReader() {
        return _reader;
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else if (!(o instanceof Source)) {
            result = false;
        } else {
            final Source that = (Source) o;
            result = getFormat().equals(that.getFormat()) && getReader().equals(that.getReader());
        }
        return result;
    }

    @Override
    public int hashCode() {
        int result = getFormat().hashCode();
        result = 31 * result + getReader().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Source of " + getFormat() + ": " + getReader();
    }


    @SuppressWarnings("ConstantNamingConvention")
    public static interface Format {

        public static final Format plain = new Impl("plain");
        public static final Format html = new Impl("html");
        public static final Format confluence = new Impl("confluence");
        public static final Format mediaWiki = new Impl("mediaWiki");
        public static final Format textile = new Impl("textile");

        @Nonnull
        public String getName();

        public static class Impl extends FormatSupport implements Format {

            public Impl(@Nonnull String name) {
                super(name);
            }

            @Override
            protected boolean isOfRequiredType(@Nonnull Object o) {
                return o instanceof Format;
            }

            @Override
            protected String getNameOf(@Nonnull Object o) {
                return ((Format)o).getName();
            }
        }
    }

}

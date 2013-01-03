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
import java.io.Writer;

import static org.echocat.jomon.format.Target.Format.textPlain;

public class Target {

    @Nonnull
    public static Target targetOf(@Nonnull Format format, @Nonnull Writer writer) {
        return new Target(format, writer);
    }

    @Nonnull
    public static Target targetOf(@Nonnull Writer writer) {
        return targetOf(textPlain, writer);
    }

    private final Format _format;
    private final Writer _writer;

    public Target(@Nonnull Format format, @Nonnull Writer writer) {
        _format = format;
        _writer = writer;
    }

    @Nonnull
    public Format getFormat() {
        return _format;
    }

    @Nonnull
    public Writer getWriter() {
        return _writer;
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else if (!(o instanceof Target)) {
            result = false;
        } else {
            final Target that = (Target) o;
            result = getFormat().equals(that.getFormat()) && getWriter().equals(that.getWriter());
        }
        return result;
    }

    @Override
    public int hashCode() {
        int result = getFormat().hashCode();
        result = 31 * result + getWriter().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Target of " + getFormat() + ": " + getWriter();
    }

    @SuppressWarnings("ConstantNamingConvention")
    public static interface Format extends org.echocat.jomon.format.Format {

        public static final Format textPlain = new Impl("textPlain");
        public static final Format html = new Impl("html");

        @Immutable
        @ThreadSafe
        public static class Impl extends org.echocat.jomon.format.Format.Impl implements Format {

            public Impl(@Nonnull String name) {
                super(name, Format.class);
            }

        }
    }

}

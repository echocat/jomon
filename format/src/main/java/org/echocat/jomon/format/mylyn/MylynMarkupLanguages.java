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

package org.echocat.jomon.format.mylyn;

import org.echocat.jomon.format.Source;
import org.echocat.jomon.format.Source.Format;
import org.echocat.jomon.runtime.util.Hints;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class MylynMarkupLanguages extends MylynAssignmentsSupport<Source.Format> {

    private static final MylynMarkupLanguages INSTANCE = new MylynMarkupLanguages();

    public static final String MARKUP_LANGUAGE_CLASS_NAME = "org.eclipse.mylyn.wikitext.core.parser.markup.MarkupLanguage";

    @Nonnull
    public static MylynMarkupLanguages mylynMarkupLanguages() {
        return INSTANCE;
    }

    public MylynMarkupLanguages(@Nullable Map<Source.Format, Class<?>> formatToType) {
        super(Source.Format.class, formatToType);
    }

    public MylynMarkupLanguages(@Nullable ClassLoader classLoader) {
        super(Source.Format.class, MARKUP_LANGUAGE_CLASS_NAME, classLoader);
    }

    public MylynMarkupLanguages() {
        super(Source.Format.class, MARKUP_LANGUAGE_CLASS_NAME);
    }

    @Nullable
    public static Class<?> findMarkupLanguageType(@Nonnull ClassLoader classLoader) {
        Class<?> result;
        try {
            result = classLoader.loadClass(MARKUP_LANGUAGE_CLASS_NAME);
        } catch (final ClassNotFoundException ignored) {
            result = null;
        }
        return result;
    }

    @Override
    public Format findSourceFormatBy(@Nonnull String name, @Nullable Hints hints) {
        return findFormatFor(name);
    }
}

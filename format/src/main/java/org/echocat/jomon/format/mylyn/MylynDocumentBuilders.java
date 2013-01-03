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

import org.echocat.jomon.format.Target;
import org.echocat.jomon.format.Target.Format;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class MylynDocumentBuilders extends MylynAssignmentsSupport<Target.Format> {

    private static final MylynDocumentBuilders INSTANCE = new MylynDocumentBuilders();

    public static final String DOCUMENT_BUILDER_TYPE_NAME = "org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder";

    @Nonnull
    public static MylynDocumentBuilders mylynDocumentBuilders() {
        return INSTANCE;
    }

    public MylynDocumentBuilders(@Nullable Map<Format, Class<?>> formatToType) {
        super(Target.Format.class, formatToType);
    }

    public MylynDocumentBuilders(@Nullable ClassLoader classLoader) {
        super(Target.Format.class, DOCUMENT_BUILDER_TYPE_NAME, classLoader);
    }

    public MylynDocumentBuilders() {
        super(Target.Format.class, DOCUMENT_BUILDER_TYPE_NAME);
    }

    @Nullable
    public static Class<?> findDocumentBuilderType(@Nonnull ClassLoader classLoader) {
        Class<?> result;
        try {
            result = classLoader.loadClass(DOCUMENT_BUILDER_TYPE_NAME);
        } catch (ClassNotFoundException ignored) {
            result = null;
        }
        return result;
    }
}

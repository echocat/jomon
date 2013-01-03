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

import org.echocat.jomon.runtime.util.Hints;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Collections.unmodifiableMap;

public class BasicFormatProvider extends FormatProviderSupport {

    public static final Map<String, Source.Format> NAME_TO_SOURCE_FORMAT = readFormatsOf(Source.Format.class);
    public static final Map<String, Target.Format> NAME_TO_TARGET_FORMAT = readFormatsOf(Target.Format.class);

    @Nonnull
    public static <F extends Format> Map<String, F> readFormatsOf(@Nonnull Class<F> formatType) {
        final Map<String, F> result = new LinkedHashMap<>();
        for (Field field : formatType.getDeclaredFields()) {
            final int modifiers = field.getModifiers();
            if (isStatic(modifiers) && isPublic(modifiers) && formatType.isAssignableFrom(field.getType())) {
                final F format;
                try {
                    format = formatType.cast(field.get(null));
                } catch (Exception e) {
                    throw new RuntimeException("Could not read value of " + field + ".", e);
                }
                result.put(format.getName(), format);
            }
        }
        return unmodifiableMap(result);
    }


    @Override
    public Source.Format findSourceFormatBy(@Nonnull String name, @Nullable Hints hints) {
        return NAME_TO_SOURCE_FORMAT.get(name);
    }

    @Override
    public Target.Format findTargetFormatBy(@Nonnull String name, @Nullable Hints hints) {
        return NAME_TO_TARGET_FORMAT.get(name);
    }



}

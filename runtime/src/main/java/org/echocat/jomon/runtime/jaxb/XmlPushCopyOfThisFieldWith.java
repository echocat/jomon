/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.runtime.jaxb;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Map;
import java.util.WeakHashMap;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({METHOD})
@Inherited
public @interface XmlPushCopyOfThisFieldWith {

    public String copyName();

    public Class<? extends Transformer<?, ?>> transformer();

    public interface Transformer<I, O> {

        @Nullable
        public O transform(@Nullable I input) throws Exception;

    }

    public static class TransformUtil {

        private static final Map<Class<? extends Transformer<?, ?>>, Transformer<?, ?>> CLASS_TO_INSTANCE = new WeakHashMap<>();

        @Nullable
        public static Object transform(@Nonnull XmlPushCopyOfThisFieldWith annotation, @Nullable Object input) throws Exception {
            //noinspection unchecked
            return transform((Class<? extends Transformer<Object, Object>>)annotation.transformer(), input);
        }

        @Nullable
        public static <I, O> O transform(@Nonnull Class<? extends Transformer<I, O>> type, @Nullable I input) throws Exception {
            //noinspection unchecked
            final Transformer<I, O> transformer = (Transformer<I, O>) getTransformerFor(type);
            return transformer.transform(input);
        }

        @Nonnull
        private static Transformer<?, ?> getTransformerFor(@Nonnull Class<? extends Transformer<?, ?>> type) throws Exception {
            synchronized (CLASS_TO_INSTANCE) {
                Transformer<?, ?> transformer = CLASS_TO_INSTANCE.get(type);
                if (transformer == null) {
                    transformer = type.newInstance();
                    CLASS_TO_INSTANCE.put(type, transformer);
                }
                return transformer;
            }
        }

        private TransformUtil() {}
    }
}

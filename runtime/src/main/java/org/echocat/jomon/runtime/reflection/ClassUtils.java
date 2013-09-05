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

package org.echocat.jomon.runtime.reflection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Iterator;

import static java.lang.reflect.Modifier.*;

public class ClassUtils {

    @Nullable
    public static Class<?> toClass(@Nullable Type type) {
        final Class<?> result;
        if (type == null) {
            result = null;
        } else if (type instanceof Class<?>) {
            result = (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            result = toClass(((ParameterizedType) type).getRawType());
        } else if (type instanceof TypeVariable) {
            result = toClass(((TypeVariable) type).getBounds()[0]);
        } else {
            throw new IllegalArgumentException("Could not handle: " + type);
        }
        return result;
    }

    @Nullable
    public static Type tryExtractComponentTypeOfIterable(@Nonnull Type type) {
        final Type result;
        if (type instanceof Class<?>) {
            final Class<?> aClass = (Class<?>) type;
            if (!Iterable.class.isAssignableFrom(aClass) && !Iterator.class.isAssignableFrom(aClass) && !aClass.isArray()) {
                result = null;
            } else if (aClass.isArray()) {
                result = aClass.getComponentType();
            } else {
                result = Object.class;
            }
        } else if (type instanceof ParameterizedType) {
            final ParameterizedType parameterizedType = (ParameterizedType) type;
            final Class<?> aClass = toClass(parameterizedType.getRawType());
            if (!Iterable.class.isAssignableFrom(aClass) && !Iterator.class.isAssignableFrom(aClass) && !aClass.isArray()) {
                result = null;
            } else {
                result = parameterizedType.getActualTypeArguments()[0];
            }
        } else if (type instanceof TypeVariable) {
            final TypeVariable<?> typeVariable = (TypeVariable) type;
            final Class<?> aClass = toClass(typeVariable.getBounds()[0]);
            if (!Iterable.class.isAssignableFrom(aClass) && !Iterator.class.isAssignableFrom(aClass) && !aClass.isArray()) {
                result = null;
            } else {
                result = typeVariable.getBounds()[0];
            }
        } else {
            throw new IllegalArgumentException("Could not handle: " + type);
        }
        return result;
    }

    @Nullable
    public static Class<?> tryExtractComponentClassOfIterable(@Nonnull Type type) {
        final Type componentType = tryExtractComponentTypeOfIterable(type);
        return componentType != null ? toClass(componentType) : null;
    }

    @Nonnull
    public static Type extractComponentTypeOfIterable(@Nonnull Type type) {
        final Type result = tryExtractComponentTypeOfIterable(type);
        if (result == null) {
            throw new IllegalArgumentException(type +  " is neither of type " + Iterable.class.getName() + " nor an array.");
        }
        return result;
    }

    @Nonnull
    public static Class<?> extractComponentClassOfIterable(@Nonnull Type type) {
        final Type componentType = extractComponentTypeOfIterable(type);
        return toClass(componentType);
    }

    @Nonnull
    public static Type tryExtractComponentTypeOfIterable(@Nonnull PropertyDescriptor descriptor) {
        Type result = null;
        final Method readMethod = descriptor.getReadMethod();
        if (readMethod != null) {
            result = tryExtractComponentTypeOfIterable(readMethod.getGenericReturnType());
        }
        if (result == null) {
            final Method writeMethod = descriptor.getWriteMethod();
            if (writeMethod != null) {
                final Type[] genericParameterTypes = readMethod.getGenericParameterTypes();
                result = genericParameterTypes.length == 1 ? tryExtractComponentTypeOfIterable(genericParameterTypes[0]) : null;
            } else if (readMethod == null) {
                throw new IllegalArgumentException("The descriptor " + descriptor + " has neither a setter nor a getter?");
            }
        }
        return result;
    }

    @Nonnull
    public static Class<?> tryExtractComponentClassOfIterable(@Nonnull PropertyDescriptor descriptor) {
        final Type componentType = tryExtractComponentTypeOfIterable(descriptor);
        return componentType != null ? toClass(componentType) : null;
    }

    @Nonnull
    public static Type extractComponentTypeOfIterable(@Nonnull PropertyDescriptor descriptor) {
        final Type result = tryExtractComponentTypeOfIterable(descriptor);
        if (result == null) {
            throw new IllegalArgumentException("Type of " + descriptor +  " is neither of type " + Iterable.class.getName() + " nor an array.");
        }
        return result;
    }

    @Nonnull
    public static Class<?> extractComponentClassOfIterable(@Nonnull PropertyDescriptor descriptor) {
        final Type result = extractComponentTypeOfIterable(descriptor);
        return toClass(result);
    }

    @Nonnull
    public static Type getGenericTypeOf(@Nonnull PropertyDescriptor descriptor) {
        Type result = null;
        final Method readMethod = descriptor.getReadMethod();
        if (readMethod != null) {
            result = readMethod.getGenericReturnType();
        } else {
            final Method writeMethod = descriptor.getWriteMethod();
            if (writeMethod != null) {
                result = readMethod.getGenericParameterTypes()[0];
            } else if (readMethod == null) {
                throw new IllegalArgumentException("The descriptor " + descriptor + " has neither a setter nor a getter?");
            }
        }
        return result;
    }

    @Nonnull
    public static Method getPublicMethodOf(@Nonnull Class<?> ofType, @Nonnull Class<?> returnType, @Nonnull String methodName, @Nullable Class<?>... parameterTypes) {
        try {
            final Method method = ofType.getMethod(methodName, parameterTypes);
            final int modifiers = method.getModifiers();
            if (isStatic(modifiers)) {
                throw new IllegalArgumentException(method + " is static.");
            }
            if (!isPublic(modifiers)) {
                throw new IllegalArgumentException(method + " is not public.");
            }
            if (!returnType.equals(method.getReturnType())) {
                throw new IllegalArgumentException(method + " does not return " + returnType.getName() + ".");
            }
            return method;
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Could not find public static " + returnType.getSimpleName() + " " + ofType.getSimpleName() + "." + methodName + "(" + Arrays.toString(parameterTypes) + ").", e);
        }
    }

    @Nullable
    public static <T extends Annotation> T findAnnotation(@Nonnull Class<T> annotationType, @Nonnull PropertyDescriptor of) {
        final Method readMethod = of.getReadMethod();
        T result = readMethod != null ? readMethod.getAnnotation(annotationType) : null;
        if (result == null) {
            final Method writeMethod = of.getWriteMethod();
            result = writeMethod != null ? writeMethod.getAnnotation(annotationType) : null;
        }
        return result;
    }

    private ClassUtils() {}
}

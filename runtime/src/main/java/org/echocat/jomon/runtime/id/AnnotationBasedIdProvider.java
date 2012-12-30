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

package org.echocat.jomon.runtime.id;

import org.echocat.jomon.runtime.jaxb.XmlId;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableMap;

public class AnnotationBasedIdProvider<ID, B> implements IdProvider<ID, B> {

    private static final List<Class<? extends Annotation>> ANNOTATION_TYPES = Arrays.<Class<? extends Annotation>>asList(XmlId.class);

    private final Map<Class<?>, Method> _typeToGetIdMethod;

    public AnnotationBasedIdProvider(@Nullable Class<?>... types) {
        this(types != null ? asList(types) : null);
    }

    public AnnotationBasedIdProvider(@Nullable Iterable<Class<?>> types) {
        _typeToGetIdMethod = types != null ? getTypeToGetIdMethodForInternal(types) : Collections.<Class<?>, Method>emptyMap();
    }

    @Nonnull
    protected Map<Class<?>, Method> getTypeToGetIdMethodForInternal(@Nonnull Iterable<Class<?>> types) {
        final Map<Class<?>, Method> typeToGetIdMethod = new HashMap<>();
        for (Class<?> type : types) {
            typeToGetIdMethod.put(type, getGetIdMethodForInternal(type));
        }
        return unmodifiableMap(typeToGetIdMethod);
    }

    @Nonnull
    protected Method getGetIdMethodForInternal(@Nonnull Class<?> type) {
        final BeanInfo beanInfo = getBeanInfoFor(type);
        Method getIdMethod = null;
        for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
            final Method readMethod = descriptor.getReadMethod();
            if (readMethod != null) {
                if (hasIdAnnotation(type, descriptor)) {
                    if (getIdMethod != null) {
                        throw new IllegalArgumentException(type.getName() + " has multiple id annotations.");
                    }
                    getIdMethod = readMethod;
                }
            }
        }
        if (getIdMethod == null) {
            throw new IllegalArgumentException(type.getName() + " has no id annotations.");
        }
        return getIdMethod;
    }

    @Nonnull
    protected BeanInfo getBeanInfoFor(@Nonnull Class<?> type) {
        try {
            return Introspector.getBeanInfo(type, Object.class);
        } catch (IntrospectionException e) {
            throw new RuntimeException("Could not read beanInfo of " + type.getName() + ".", e);
        }
    }

    protected boolean hasIdAnnotation(@Nonnull Class<?> type, @Nonnull PropertyDescriptor descriptor) {
        final Method readMethod = descriptor.getReadMethod();
        final Method writeMethod = descriptor.getWriteMethod();
        final boolean readMethodHasIdAnnotation = readMethod != null && hasIdAnnotation(readMethod);
        final boolean writeMethodHasIdAnnotation = writeMethod != null && hasIdAnnotation(writeMethod);
        if (readMethodHasIdAnnotation && writeMethodHasIdAnnotation) {
            throw new IllegalArgumentException("Property " + type.getName() + "." + descriptor.getName() + " has multiple id annotations.");
        }
        return readMethodHasIdAnnotation || writeMethodHasIdAnnotation;
    }

    protected boolean hasIdAnnotation(@Nonnull Method method) {
        boolean result = false;
        for (Class<? extends Annotation> annotationType : getIdAnnotationTypes()) {
            if (method.getAnnotation(annotationType) != null) {
                result = true;
                break;
            }
        }
        return result;
    }

    @Nonnull
    protected Iterable<Class<? extends Annotation>> getIdAnnotationTypes() {
        return ANNOTATION_TYPES;
    }

    @Override
    public ID provideIdOf(@Nonnull B bean) {
        final Method getIdMethod = getGetIdMethodFor(bean);
        try {
            // noinspection unchecked
            return (ID) getIdMethod.invoke(bean);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not access " + getIdMethod + ".", e);
        } catch (InvocationTargetException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof Error) {
                // noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
                throw (Error)cause;
            } else if (cause instanceof RuntimeException) {
                // noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
                throw (RuntimeException)cause;
            } else {
                throw new RuntimeException("Could not invoke " + getIdMethod + ".", cause != null ? cause : e);
            }
        }
    }

    @Nonnull
    protected Method getGetIdMethodFor(@Nonnull B bean) {
        return getGetIdMethodFor(bean.getClass());
    }

    @Nonnull
    protected Method getGetIdMethodFor(@Nonnull Class<?> type) {
        Method method = _typeToGetIdMethod.get(type);
        if (method == null) {
            for (Entry<Class<?>, Method> typeAndMethod : _typeToGetIdMethod.entrySet()) {
                if (typeAndMethod.getKey().isAssignableFrom(type)) {
                    method = typeAndMethod.getValue();
                    break;
                }
            }
            if (method == null) {
                throw new IllegalArgumentException("Unknown type: " + type.getName());
            }
        }
        return method;
    }
}

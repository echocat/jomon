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

package org.echocat.jomon.runtime.valuemodule.access;

import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import static java.util.Collections.synchronizedMap;
import static org.slf4j.LoggerFactory.getLogger;

public class GetterAndSetter<T> {

    private static final Logger LOG = getLogger(GetterAndSetter.class);
    private static final Map<Class<?>, GetterAndSetter<?>> TYPE_TO_INSTANCE_CACHE = synchronizedMap(new WeakHashMap<Class<?>, GetterAndSetter<?>>());

    @Nonnull
    public static <T> GetterAndSetter<T> getterAndSetter(@Nonnull Class<T> type) {
        // noinspection unchecked
        GetterAndSetter<T> result = (GetterAndSetter<T>)TYPE_TO_INSTANCE_CACHE.get(type);
        if (result == null) {
            result = new GetterAndSetter<>(type);
            TYPE_TO_INSTANCE_CACHE.put(type, result);
        }
        return result;
    }

    private final Map<Method, PropertyDescriptor> _getter;
    private final Map<Method, PropertyDescriptor> _setter;

    protected GetterAndSetter(@Nonnull Class<T> of) {
        _getter = new HashMap<>();
        _setter = new HashMap<>();
        final BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(of, Object.class);
        } catch (IntrospectionException e) {
            throw new RuntimeException("Could not read beanInfo of " + of.getName() + ".", e);
        }
        for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
            final Method getter = descriptor.getReadMethod();
            final Method setter = descriptor.getWriteMethod();
            if (setter != null && getter != null) {
                _setter.put(setter, descriptor);
                _getter.put(getter, descriptor);
            } else if (setter != null) {
                LOG.warn("Found setter " + setter + " but no corresponding getter for it. This setter will be ignored in this proxy. If this is not a real setter rename it. If this is a real setter it could cause problems because without an getter could this property not be synchronized.");
            }
        }
    }

    public boolean isSetter(@Nonnull Method method) {
        return _setter.containsKey(method);
    }

    public boolean isGetter(@Nonnull Method method) {
        return _getter.containsKey(method);
    }

    @Nullable
    public PropertyDescriptor findDescriptorForGetter(@Nonnull Method getter) {
        return _getter.get(getter);
    }

    @Nullable
    public PropertyDescriptor findDescriptorForSetter(@Nonnull Method setter) {
        return _setter.get(setter);
    }

    @Nullable
    public PropertyDescriptor findDescriptorFor(@Nonnull Method method) {
        final PropertyDescriptor first = _setter.get(method);
        return first != null ? first : _getter.get(method);
    }
}

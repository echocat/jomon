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

package org.echocat.jomon.process.local;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.*;
import java.lang.reflect.Field;
import java.util.ServiceLoader;

public abstract class ProcessPidResolver {

    private static final ProcessPidResolver INSTANCE = createInstance();

    @Nonnull
    public static ProcessPidResolver getInstance() {
        return INSTANCE;
    }

    public abstract long resolvePidOf(@Nonnull Process process);

    @Nonnull
    protected static ProcessPidResolver createInstance() {
        final ServiceLoader<ProcessPidResolver> found = ServiceLoader.load(ProcessPidResolver.class);
        ProcessPidResolver resolver = null;
        for (final ProcessPidResolver candidate : found) {
            if (candidate.couldHandleThisVirtualMachine()) {
                resolver = candidate;
                break;
            }
        }
        if (resolver == null) {
            throw new IllegalArgumentException("Could not find any matching implementation of " + ProcessPidResolver.class.getName() + " for this virtual machine.");
        }
        resolver.init();
        return resolver;
    }


    protected abstract boolean couldHandleThisVirtualMachine();

    protected abstract void init();

    @Nullable
    protected static Class<?> findClass(@Nonnull String className) {
        Class<?> result;
        try {
            result = ProcessPidResolver.class.getClassLoader().loadClass(className);
        } catch (final ClassNotFoundException ignored) {
            result = null;
        }
        return result;
    }

    @Nullable
    protected static Field findFieldOf(@Nonnull String fieldName, @Nullable Class<?> ofType, @Nonnull Class<?> from) {
        Field field;
        if (from != null) {
            try {
                field = from.getDeclaredField(fieldName);
                if (!field.getType().equals(ofType)) {
                    field = null;
                }
                field.setAccessible(true);
            } catch (final NoSuchFieldException ignored) {
                field = null;
            }
        } else {
            field = null;
        }
        return field;
    }

    @Nullable
    protected Object getFieldValue(@Nonnull Field field, @Nonnull Object instance) {
        try {
            return field.get(instance);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("Could not read value of " + field + " from " + instance + ".", e);
        }
    }

}

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

package org.echocat.jomon.runtime.valuemodule.annotation;

import org.echocat.jomon.runtime.valuemodule.ValueModule;
import org.echocat.jomon.runtime.valuemodule.ValueModule.IsModularizedBy;
import org.echocat.jomon.runtime.valuemodule.ValueModule.IsModuleAnnotation;
import org.echocat.jomon.runtime.valuemodule.ValueModuleAssignment;
import org.echocat.jomon.runtime.valuemodule.access.ValueModuleToAssignmentStack;
import org.echocat.jomon.runtime.valuemodule.access.ValueModuleToAssignmentStackFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

import static java.beans.Introspector.getBeanInfo;
import static org.echocat.jomon.runtime.reflection.ClassUtils.*;
import static org.echocat.jomon.runtime.valuemodule.ValueModuleUtils.valuesOf;

public class AnnotationBasedValueModuleToAssignmentStackFactory implements ValueModuleToAssignmentStackFactory {

    @Override
    @Nonnull
    public <B> ValueModuleToAssignmentStack<ValueModule, B> getStack(@Nullable Class<? extends B>... valueTypes) {
        final Map<ValueModule, Deque<ValueModuleAssignment<? extends ValueModule, ? extends B>>> moduleToAssignmentStack = new HashMap<>();

        for (ValueModuleAssignment<? extends ValueModule, ? extends B> moduleAssignment : getModuleAssignmentsFor(valueTypes)) {
            final Deque<ValueModuleAssignment<? extends ValueModule, ? extends B>> currentStack = new LinkedList<>();
            moduleToAssignmentStack.putAll(toStack(moduleAssignment, currentStack));
        }

        return new ValueModuleToAssignmentStack<>(moduleToAssignmentStack);
    }

    @Nonnull
    protected <B> Map<? extends ValueModule, Deque<ValueModuleAssignment<? extends ValueModule, ? extends B>>> toStack(@Nonnull ValueModuleAssignment<? extends ValueModule, ? extends B> moduleAssignment, @Nonnull Deque<ValueModuleAssignment<? extends ValueModule, ? extends B>> currentStack) {
        final Map<ValueModule, Deque<ValueModuleAssignment<? extends ValueModule, ? extends B>>> moduleToAssignmentStack = new HashMap<>();
        currentStack.addLast(moduleAssignment);

        moduleToAssignmentStack.put(moduleAssignment.getModule(), new LinkedList<>(currentStack));

        for (ValueModuleAssignment<? extends ValueModule, ? extends B> child : moduleAssignment.getChildren()) {
            moduleToAssignmentStack.putAll(toStack(child, currentStack));
        }

        currentStack.removeLast();
        return moduleToAssignmentStack;
    }

    @Nonnull
    protected Class<? extends Annotation> getIsModuleTypeFor(@Nonnull Class<? extends ValueModule> type) {
        assertType(type);
        final IsModuleAnnotation isModuleAnnotation = type.getAnnotation(IsModuleAnnotation.class);
        final Class<? extends Annotation> annotationType = isModuleAnnotation.value();
        if (!annotationType.isAnnotation()) {
            throw new IllegalArgumentException(annotationType.getName() + " is no annotation.");
        }
        return annotationType;
    }

    @Nonnull
    protected <B> Collection<ValueModuleAssignment<? extends ValueModule, ? extends B>> getModuleAssignmentsFor(@Nullable Class<? extends B>... valueTypes) {
        final Collection<ValueModuleAssignment<? extends ValueModule, ? extends B>> result = new HashSet<>();
        if (valueTypes != null) {
            for (Class<? extends B> valueType : valueTypes) {
                final Class<? extends ValueModule> moduleType = findModuleTypeOf(valueType);
                if (moduleType != null) {
                    final BeanInfo beanInfo = getBeanInfoFor(valueType);
                    final Class<? extends Annotation> isModuleType = getIsModuleTypeFor(moduleType);
                    for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
                        final ValueModule module = findModuleOf(isModuleType, valueType, descriptor, moduleType);
                        if (module != null) {
                            final Collection<? extends ValueModule> includes = module.getIncludes();
                            if (includes != null && !includes.isEmpty()) {
                                throw new IllegalArgumentException("It is not possible to annotate a module with includes at " + valueType.getName() + "." + descriptor.getName() + ".");
                            }
                            // noinspection unchecked
                            final Collection<ValueModuleAssignment<? extends ValueModule, ? extends B>> childrenAssignments = getModuleAssignmentsFor((Class<? extends B>)getTypeOf(descriptor));
                            final ValueModuleAssignment<ValueModule, B> assignment = new ValueModuleAssignment<>(module, valueType, descriptor, childrenAssignments);
                            result.add(assignment);
                        }
                    }
                }
            }
        }
        return result;
    }

    @Nullable
    protected Class<? extends ValueModule> findModuleTypeOf(@Nonnull Class<?> beanType) {
        final IsModularizedBy isModularizedBy = beanType.getAnnotation(IsModularizedBy.class);
        final Class<? extends ValueModule> moduleType;
        if (isModularizedBy != null) {
            moduleType = isModularizedBy.value();
        } else {
            moduleType = null;
        }
        return moduleType;
    }

    @Nonnull
    protected BeanInfo getBeanInfoFor(@Nonnull Class<?> beanType) {
        final BeanInfo beanInfo;
        try {
            beanInfo = getBeanInfo(beanType, Object.class);
        } catch (IntrospectionException e) {
            throw new RuntimeException("Could not get beanInfo of " + beanType.getName() + ".", e);
        }
        return beanInfo;
    }

    @Nullable
    protected ValueModule findModuleOf(@Nonnull Class<? extends Annotation> isModuleType, @Nonnull Class<?> beanType, @Nonnull PropertyDescriptor descriptor, @Nonnull Class<? extends ValueModule> expectedValueType) {
        final Method writeMethod = descriptor.getWriteMethod();
        final Method readMethod = descriptor.getReadMethod();
        final ValueModule module;
        if (writeMethod != null && readMethod != null) {
            Annotation annotation = writeMethod.getAnnotation(isModuleType);
            if (annotation == null) {
                annotation = readMethod.getAnnotation(isModuleType);
            }
            if (annotation != null) {
                module = getValueOf(annotation);
                if (module != null && !expectedValueType.isInstance(module)) {
                    throw new IllegalArgumentException("The property " + beanType.getName() + "." + descriptor.getName() + " is configured with a different module type.");
                }
            } else {
                module = null;
            }
        } else {
            module = null;
        }
        return module;
    }

    @Nonnull
    protected Class<?> getTypeOf(@Nonnull PropertyDescriptor descriptor) {
        final Type genericType = getGenericTypeOf(descriptor);
        final Class<?> componentType = tryExtractComponentClassOfIterable(genericType);
        return componentType != null ? componentType : toClass(genericType);
    }

    @Nonnull
    protected ValueModule getValueOf(@Nonnull Annotation annotation) {
        try {
            // noinspection unchecked
            return (ValueModule) annotation.getClass().getMethod("value").invoke(annotation);
        } catch (Exception e) {
            throw new RuntimeException("Could not get value of " + annotation + ".", e);
        }
    }

    @Nonnull
    protected Map<String, ValueModule> getAllModulesOf(@Nullable Class<? extends ValueModule>... types) {
        final Map<String, ValueModule> modules = new LinkedHashMap<>();
        if (types != null) {
            for (Class<? extends ValueModule> type : types) {
                //noinspection unchecked, rawtypes, RedundantCast
                for (Enum value : valuesOf((Class<Enum>) (Object) type)) {
                    assertType(type);
                    modules.put(((ValueModule)value).getId(), (ValueModule)value);
                }
            }
        }
        return modules;
    }

    protected void assertType(@Nonnull Class<? extends ValueModule> type) {
        if (!ValueModule.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException(type.getName() + " is not of type " + ValueModule.class.getName() + ".");
        }
        if (!type.isEnum()) {
            throw new IllegalArgumentException(type.getName() + " is not a enum.");
        }
    }
}

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

import org.echocat.jomon.runtime.valuemodule.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import static org.echocat.jomon.runtime.reflection.ClassUtils.getPublicMethodOf;

public class DefaultValueModuleAccessor<VM extends ValueModule, B> implements ValueModuleAccessor<VM, B> {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultValueModuleAccessor.class);

    protected static final Object UNKNOWN = new Object();

    public static final Method EQUALS_METHOD = getPublicMethodOf(Object.class, boolean.class, "equals", Object.class);
    public static final Method HASH_CODE_METHOD = getPublicMethodOf(Object.class, int.class, "hashCode");
    public static final String IS_MODULE_ANNOTATION_NAME = "IsModule";

    private final ValueModuleToAssignmentStack<VM, B> _assignmentStack;

    public DefaultValueModuleAccessor(@Nonnull ValueModuleToAssignmentStack<VM, B> assignmentStack) {
        _assignmentStack = assignmentStack;
    }

    @Override
    public boolean isResponsibleFor(@Nonnull Class<?> type) {
        return _assignmentStack.isResponsibleFor(type);
    }

    @Override
    public Object getValueOf(@Nullable B baseBean, @Nonnull VM module) {
        return getValueOf(baseBean, module, null);
    }

    @Override
    public Object getValueOf(@Nullable B baseBean, @Nonnull VM module, @Nullable SourcePath sourcePath) {
        final Iterator<ValueModuleAssignment<? extends VM, ? extends B>> assignments = iterateOverAssignmentsFor(module);
        final Iterator<SourceElement> sourceElements = sourcePath != null ? sourcePath.iterator() : null;
        Object current = baseBean;
        int position = 0;
        while (assignments.hasNext() && (sourceElements == null || sourceElements.hasNext()) && current != null) {
            final ValueModuleAssignment<? extends VM, ? extends B> assignment = assignments.next();
            final SourceElement sourceElement = sourceElements != null ? sourceElements.next() : null;
            current = getValueOf(current, assignment, sourceElement, baseBean, module, position);
            position++;
        }
        if (sourceElements != null && (assignments.hasNext() || sourceElements.hasNext())) {
            throw new IllegalArgumentException("The count of assignments is not the same as the sourcePath (" + sourcePath + ") for " + baseBean + " to receive " + module + ".");
        }
        return current;
    }

    @Nullable
    protected Object getValueOf(@Nonnull Object bean, @Nonnull ValueModuleAssignment<? extends VM, ? extends B> assignment, @Nullable SourceElement sourceElement, @Nonnull B baseBean, @Nonnull VM module, @Nonnegative int position) {
        return getValueOf(bean, assignment.getDescriptor(), sourceElement, baseBean, module, position);
    }

    @Nullable
    protected Object getValueOf(@Nonnull Object bean, @Nonnull PropertyDescriptor descriptor, @Nullable SourceElement sourceElement, @Nonnull B baseBean, @Nonnull VM module, @Nonnegative int position) {
        final Method readMethod = descriptor.getReadMethod();
        if (readMethod == null) {
            throw new IllegalArgumentException("Could not read property " + locationIdentificationFor(baseBean, module, position) + " because there is no getter.");
        }
        final Object targetBean = getTargetBeanFor(bean, descriptor, sourceElement, baseBean);
        try {
            return targetBean != null ? readMethod.invoke(targetBean) : null;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not access " + readMethod + " while reading property " + locationIdentificationFor(baseBean, module, position) + ".", e);
        } catch (InvocationTargetException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                // noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
                throw (RuntimeException)cause;
            } else if (cause instanceof Error) {
                // noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
                throw (Error)cause;
            } else {
                throw new RuntimeException("Could not read property " + locationIdentificationFor(baseBean, module, position) + ".", cause != null ? cause : e);
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Could not read property " + locationIdentificationFor(baseBean, module, position) + ".", e);
        }
    }

    @Nullable
    protected Object getTargetBeanFor(@Nonnull Object bean, @Nonnull PropertyDescriptor descriptor, @Nullable SourceElement sourceElement, @Nonnull B baseBean) {
        final Object targetBean;
        if (bean instanceof Iterable/* && !descriptor.getPropertyType().isInstance(bean)*/) {
            targetBean = handleIterable((Iterable<?>)bean, descriptor, sourceElement, baseBean);
        } else {
            targetBean = bean;
        }
        return targetBean;
    }

    @Nullable
    protected Object handleIterable(@Nonnull Iterable<?> iterable, @Nonnull PropertyDescriptor descriptor, @Nullable SourceElement sourceElement, @Nonnull B baseBean) {
        // noinspection ObjectEquality
        if (sourceElement == null) {
            throw new IllegalArgumentException("Could not get the value of " + iterable + " because it is an interable and not the required type of " + descriptor.getPropertyType() + ". There is also no sourceElement provide to find the requested value of module in this iterable.");
        }
        Object targetBean = UNKNOWN;
        // noinspection ObjectEquality
        if (iterable instanceof List && sourceElement instanceof IndexedSourceElement) {
            final List<?> list = (List<?>) iterable;
            final int index = ((IndexedSourceElement) sourceElement).getIndex();
            if (list.size() > index) {
                targetBean = list.get(index);
            }
        }
        // noinspection ObjectEquality
        if (targetBean == UNKNOWN) {
            final Object sourceElementContent = sourceElement.getElement();
            for (Object potentialTargetBean : iterable) {
                if (sourceElementContent != null ? sourceElementContent.equals(potentialTargetBean) : potentialTargetBean == null) {
                    targetBean = potentialTargetBean;
                    break;
                }
            }
        }
        // noinspection ObjectEquality
        if (targetBean == UNKNOWN) {
            final Object sourceElementContent = sourceElement.getElement();
            if (sourceElementContent != null) {
                final Method equalsMethod = getPublicMethodOf(sourceElementContent.getClass(), boolean.class, "equals", Object.class);
                final Method hashCodeMethod = getPublicMethodOf(sourceElementContent.getClass(), int.class, "hashCode");
                if (EQUALS_METHOD.equals(equalsMethod) || HASH_CODE_METHOD.equals(hashCodeMethod)) {
                    throw new IllegalArgumentException("Could not find " + sourceElementContent + " in " + iterable + ". The equals and hashCode method of " + sourceElementContent.getClass() + " does not override the default implementation of " + Object.class.getName() + ".");
                } else {
                    LOG.warn("Could not find " + sourceElementContent + " in " + iterable + ". It seems that the " + baseBean + " was modified since initial load.");
                    targetBean = null;
                }
            } else {
                LOG.warn("Could not find " + sourceElementContent + " in " + iterable + ". It seems that the " + baseBean + " was modified since initial load.");
                targetBean = null;
            }
        }
        return targetBean;
    }

    @Override
    public void setValueOf(@Nonnull B baseBean, @Nonnull VM module, @Nullable Object value) {
        setValueOf(baseBean, module, value, null);
    }

    @Override
    public void setValueOf(@Nonnull B baseBean, @Nonnull VM module, @Nullable Object value, @Nullable SourcePath sourcePath) {
        int position = 0;
        final Iterator<ValueModuleAssignment<? extends VM, ? extends B>> assignments = iterateOverAssignmentsFor(module);
        final Iterator<SourceElement> sourceElements = sourcePath != null ? sourcePath.iterator() : null;
        Object current = baseBean;
        while (assignments.hasNext() && (sourceElements == null || sourceElements.hasNext()) && current != null) {
            final ValueModuleAssignment<? extends VM,? extends B> assignment = assignments.next();
            final SourceElement sourceElement = sourceElements != null ? sourceElements.next() : null;
            if (isLastElementOf(assignments)) {
                setValueOf(current, assignment, baseBean, module, position, value);
            } else {
                current = getElementFor(current, assignment, sourceElement, baseBean, module, position);
                position++;
            }
        }
        if (sourceElements != null && (assignments.hasNext() || sourceElements.hasNext())) {
            throw new IllegalArgumentException("The count of assignments is not the same as the sourcePath (" + sourcePath + ") for " + baseBean + " to receive " + module + ".");
        }
    }

    @Nullable
    protected Object setValueOf(@Nonnull Object bean, @Nonnull ValueModuleAssignment<? extends VM, ? extends B> assignment, @Nonnull B baseBean, @Nonnull VM module, @Nonnegative int position, @Nullable Object value) {
        return setValueOf(bean, assignment.getDescriptor(), baseBean, module, position, value);
    }

    @Nullable
    protected Object setValueOf(@Nonnull Object bean, @Nonnull PropertyDescriptor descriptor, @Nonnull B baseBean, @Nonnull VM module, @Nonnegative int position, @Nullable Object value) {
        final Method writeMethod = descriptor.getWriteMethod();
        if (writeMethod == null) {
            throw new IllegalArgumentException("Could not write property " + locationIdentificationFor(baseBean, module, position) + " because there is no setter.");
        }
        try {
            return writeMethod.invoke(bean, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not access " + writeMethod + " while writing property " + locationIdentificationFor(baseBean, module, position) + ".", e);
        } catch (InvocationTargetException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                // noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
                throw (RuntimeException)cause;
            } else if (cause instanceof Error) {
                // noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
                throw (Error)cause;
            } else {
                throw new RuntimeException("Could not write property " + locationIdentificationFor(baseBean, module, position) + ".", cause != null ? cause : e);
            }
        }
    }

    @Nonnull
    protected Object getElementFor(@Nonnull Object bean, @Nonnull ValueModuleAssignment<? extends VM, ? extends B> assignment, @Nullable SourceElement sourceElement, @Nonnull B baseBean, @Nonnull VM module, @Nonnegative int position) {
        final Object result = getValueOf(bean, assignment, sourceElement, baseBean, module, position);
        if (result == null) {
            throw new NullPointerException("Property " + locationIdentificationFor(baseBean, module, position) + " is null.");
        }
        return result;
    }

    @Nonnull
    protected String locationIdentificationFor(@Nonnull B baseBean, @Nonnull VM module, @Nonnegative int position) {
        final StringBuilder sb = new StringBuilder();
        sb.append(baseBean);
        final Iterator<ValueModuleAssignment<? extends VM, ? extends B>> i = iterateOverAssignmentsFor(module);
        int c = 0;
        while (i.hasNext() && c <= position) {
            sb.append('.').append(i.next().getDescriptor().getName());
            c++;
        }
        return sb.toString();
    }

    protected boolean isLastElementOf(@Nonnull Iterator<?> i) {
        return !i.hasNext();
    }

    @Nonnull
    protected Iterator<ValueModuleAssignment<? extends VM, ? extends B>> iterateOverAssignmentsFor(@Nonnull VM module) {
        final Deque<ValueModuleAssignment<? extends VM, ? extends B>> assignmentStack = _assignmentStack.findBy(module);
        if (assignmentStack == null) {
            throw new IllegalArgumentException("Unknown module " + module + ".");
        }
        return assignmentStack.iterator();
    }

    @Override
    @Nullable
    public VM findModuleFor(@Nonnull PropertyDescriptor descriptor) {
        final ValueModuleAssignment<? extends VM, ? extends B> assignment = _assignmentStack.findAssignmentBy(descriptor);
        return assignment != null ? assignment.getModule() : null;
    }

}

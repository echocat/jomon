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

package org.echocat.jomon.runtime.concurrent;

import org.echocat.jomon.runtime.util.Duration;

import javax.annotation.Nonnull;
import javax.management.*;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

import static javax.management.MBeanOperationInfo.ACTION;

public class MBeanDaemonWrapper implements  DynamicMBean {
    
    private final Daemon _delegate;
    private final Runnable _task;
    private final Map<String,PropertyDescriptor> _propertyNameToDescriptor;

    public MBeanDaemonWrapper(@Nonnull Daemon delegate) {
        _delegate = delegate;
        _task = delegate.getTask();
        _propertyNameToDescriptor = getPropertyNameToDescriptorFor(_task.getClass());
    }

    @Nonnull
    protected Map<String, PropertyDescriptor> getPropertyNameToDescriptorFor(@Nonnull Class<?> clazz) {
        final Map<String, PropertyDescriptor> propertyNameToDescriptor = new HashMap<>();
        final BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(clazz);
        } catch (IntrospectionException e) {
            throw new RuntimeException("Could not load beanInfo of " + clazz.getName() + ".", e);
        }
        final PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            propertyNameToDescriptor.put(propertyDescriptor.getName(), propertyDescriptor);
        }
        propertyNameToDescriptor.remove("class");
        return propertyNameToDescriptor;
    }
    
    
    @Override
    public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
        final Object result;
        if ("active".equals(attribute)) {
            result = _delegate.isActive();
        } else if ("interval".equals(attribute)) {
            result = _delegate.getInterval().toPattern();
        } else if ("running".equals(attribute)) {
            result = _delegate.isRunning();
        } else if ("nextExecution".equals(attribute)) {
            result = _delegate.getNextExecution();
        } else if ("lastExecution".equals(attribute)) {
            result = _delegate.getLastExecution();
        } else if ("lastExecutionDuration".equals(attribute)) {
            result = _delegate.getLastExecutionDuration() != null ? _delegate.getLastExecutionDuration().toPattern() : null;
        } else if ("overallExecutionDuration".equals(attribute)) {
            result = _delegate.getOverallExecutionDuration().toPattern();
        } else if ("overallExecutionCount".equals(attribute)) {
            result = _delegate.getOverallExecutionCount();
        } else {
            final PropertyDescriptor descriptor = _propertyNameToDescriptor.get(attribute);
            if (descriptor != null && descriptor.getReadMethod() != null) {
                final Method readMethod = descriptor.getReadMethod();
                try {
                    final Object value = readMethod.invoke(_task);
                    if (value == null || isTransportableType(value.getClass())) {
                        result = value;
                    } else {
                        result = value.toString();
                    }
                } catch (Exception e) {
                    throw new ReflectionException(e);
                }
            } else {
                throw new AttributeNotFoundException(attribute);
            }
        }
        return result;
    }

    @Override
    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        final Object value = attribute.getValue();
        if ("active".equals(attribute.getName())) {
            if (value instanceof Boolean) {
                _delegate.setActive((Boolean)value);
            } else {
                throw new InvalidAttributeValueException();
            }
        } else if ("interval".equals(attribute.getName())) {
            if (value instanceof String) {
                _delegate.setInterval(new Duration(value.toString()));
            } else {
                throw new InvalidAttributeValueException();
            }
        } else {
            final PropertyDescriptor descriptor = _propertyNameToDescriptor.get(attribute.getName());
            if (descriptor != null && descriptor.getWriteMethod() != null) {
                final Object targetValue;
                if (value == null || isTransportableType(descriptor.getPropertyType())) {
                    targetValue = value;
                } else if (value instanceof String) {
                    targetValue = fromString((String)value, descriptor.getPropertyType());
                } else {
                    throw new InvalidAttributeValueException();
                }
                final Method writeMethod = descriptor.getWriteMethod();
                try {
                    writeMethod.invoke(_task, targetValue);
                } catch (Exception e) {
                    throw new ReflectionException(e);
                }
            } else {
                throw new AttributeNotFoundException(attribute.getName());
            }
        }
    }

    @Nonnull
    protected Object fromString(@Nonnull String value, @Nonnull Class<?> propertyType) throws ReflectionException {
        final Constructor<?> constructor;
        try {
            constructor = propertyType.getConstructor(String.class);
        } catch (NoSuchMethodException e) {
            throw new ReflectionException(e, "Could not find a constructor of " + propertyType.getName() + " to create a instance of it with: " + value);
        }
        try {
            return constructor.newInstance(value);
        } catch (Exception e) {
            throw new ReflectionException(e, "Could not create a new instance of " + propertyType.getName() + " with: " + value);
        }
    }

    @Override
    public AttributeList getAttributes(String[] attributes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AttributeList setAttributes(AttributeList attributes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
        if ("run".equals(actionName)) {
            _delegate.run();
        } else {
            throw new UnsupportedOperationException("Unknown method '" + actionName + "'.");
        }
        return null;
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        final List<MBeanAttributeInfo> attributes = new ArrayList<>();
        attributes.add(new MBeanAttributeInfo("interval", String.class.getName(), null, true, true, false));
        attributes.add(new MBeanAttributeInfo("active", Boolean.class.getName(), null, true, true, true));
        attributes.add(new MBeanAttributeInfo("running", Boolean.class.getName(), null, true, false, true));
        attributes.add(new MBeanAttributeInfo("nextExecution", Date.class.getName(), null, true, false, false));
        attributes.add(new MBeanAttributeInfo("lastExecution", Date.class.getName(), null, true, false, false));
        attributes.add(new MBeanAttributeInfo("lastExecutionDuration", String.class.getName(), null, true, false, false));
        attributes.add(new MBeanAttributeInfo("overallExecutionDuration", String.class.getName(), null, true, false, false));
        attributes.add(new MBeanAttributeInfo("overallExecutionCount", Long.class.getName(), null, true, false, false));
        for (PropertyDescriptor descriptor : _propertyNameToDescriptor.values()) {
            final Method readMethod = descriptor.getReadMethod();
            final Method writeMethod = descriptor.getWriteMethod();
            if (readMethod != null || writeMethod != null) {
                final Class<?> propertyType = descriptor.getPropertyType();
                final Class<?> targetPropertyType = isTransportableType(propertyType) ? propertyType : String.class;
                attributes.add(new MBeanAttributeInfo(descriptor.getName(), targetPropertyType.getName(), descriptor.getShortDescription(), readMethod != null, writeMethod != null, Boolean.class.isAssignableFrom(targetPropertyType)));
            }
        }

        return new MBeanInfo(_delegate.getClass().getName(), null, attributes.toArray(new MBeanAttributeInfo[attributes.size()]), null, new MBeanOperationInfo[]{
            new MBeanOperationInfo("run", null, new MBeanParameterInfo[]{}, "void", ACTION)
        }, null);
    }

    protected boolean isTransportableType(Class<?> type) {
        return String.class.isAssignableFrom(type) ||
            Double.class.isAssignableFrom(type) ||
            Float.class.isAssignableFrom(type) ||
            Long.class.isAssignableFrom(type) || 
            Integer.class.isAssignableFrom(type) || 
            Short.class.isAssignableFrom(type) || 
            Character.class.isAssignableFrom(type) ||
            Byte.class.isAssignableFrom(type) ||
            Boolean.class.isAssignableFrom(type);
    }
}

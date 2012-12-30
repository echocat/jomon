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

package org.echocat.jomon.runtime.valuemodule.proxy;

import org.echocat.jomon.runtime.valuemodule.ValueModule;
import org.echocat.jomon.runtime.valuemodule.ValueModuleRegistry;
import org.echocat.jomon.runtime.valuemodule.access.GetterAndSetter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static java.lang.Boolean.TRUE;
import static org.echocat.jomon.runtime.valuemodule.access.GetterAndSetter.getterAndSetter;

public abstract class ValueModuleProxyHandlerSupport<VM extends ValueModule, B, D> implements ValueModuleProxyHandler {

    private final ThreadLocal<Boolean> _isLoading = new ThreadLocal<>();

    private final ValueModuleRegistry<VM, B> _registry;
    private final D _delegate;
    private final GetterAndSetter<D> _getterAndSetter;

    public ValueModuleProxyHandlerSupport(@Nonnull D delegate, @Nonnull ValueModuleRegistry<VM, B> registry) {
        _delegate = delegate;
        _registry = registry;
        // noinspection unchecked
        _getterAndSetter = getterAndSetter((Class<D>)delegate.getClass());
    }

    @Override
    public Object invoke(@Nonnull Object proxy, @Nonnull Method thisMethod, @Nonnull Method proceed, @Nonnull Object[] args) throws Throwable {
        final PropertyDescriptor descriptorOfGetter = _getterAndSetter.findDescriptorForGetter(thisMethod);
        final Object result;
        if (descriptorOfGetter != null) {
            if (_registry.isLoadOfValueRequiredFor(descriptorOfGetter) && !TRUE.equals(_isLoading.get())) {
                _isLoading.set(TRUE);
                try {
                    result = loadAndBackupValueFor(proxy, descriptorOfGetter);
                } finally {
                    _isLoading.remove();
                }
                _registry.notifyPropertyIsNowKnown(descriptorOfGetter);
            } else {
                result = invoke(thisMethod, args);
            }
        } else {
            final PropertyDescriptor descriptorOfSetter = _getterAndSetter.findDescriptorForSetter(thisMethod);
            if (descriptorOfSetter != null && !TRUE.equals(_isLoading.get())) {
                _registry.notifyPropertyIsNowKnown(descriptorOfSetter);
                _isLoading.set(TRUE);
                try {
                    setValueAndGetNewProxyFor(proxy, descriptorOfSetter, args[0]);
                } finally {
                    _isLoading.remove();
                }
                result = null;
            } else {
                result = invoke(thisMethod, args);
            }
        }
        return result;
    }

    @Nonnull
    protected abstract Object loadAndBackupValueFor(@Nonnull Object proxy, @Nonnull PropertyDescriptor descriptor) throws Exception;

    @Nonnull
    protected abstract Object setValueAndGetNewProxyFor(@Nonnull Object proxy, @Nonnull PropertyDescriptor descriptor, @Nullable Object value) throws Exception;

    protected Object invoke(@Nonnull Method method, @Nullable Object... arguments) {
        try {
            return method.invoke(_delegate, arguments);
        } catch (InvocationTargetException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof Error) {
                // noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
                throw (Error)cause;
            } else if (cause instanceof RuntimeException) {
                // noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
                throw (RuntimeException)cause;
            } else {
                throw new RuntimeException("Could not invoke " + method + ".", cause != null ? cause : e);
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Could not access a method of a property? " + method, e);
        }
    }

    @Nonnull
    protected ValueModuleRegistry<VM, B> getRegistry() {
        return _registry;
    }

    @Nullable
    protected D getDelegate() {
        return _delegate;
    }

    @Nonnull
    protected GetterAndSetter<D> getGetterAndSetter() {
        return _getterAndSetter;
    }
}

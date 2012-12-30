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

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import org.echocat.jomon.runtime.valuemodule.*;
import org.echocat.jomon.runtime.valuemodule.fetching.ValueFetcher;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static java.util.Collections.*;
import static org.echocat.jomon.runtime.reflection.ClassUtils.extractComponentClassOfIterable;

public class DefaultValueModuleProxyFactory implements ValueModuleProxyFactory {

    protected static final Object DISCONNECTED = new Object();

    @Override
    @Nonnull
    public <VM extends ValueModule, ID, B> B createFor(@Nonnull ValueModuleRegistry<VM, B> registry, @Nonnull ValueFetcher<VM, ID, B> fetcher, @Nonnull B baseBean, @Nullable InitialSource initialSource) throws IllegalArgumentException {
        return createForInternal(registry, fetcher, baseBean, baseBean, null, null, initialSource);
    }

    @Nonnull
    protected <VM extends ValueModule, ID, B, D> D createForInternalUnchecked(@Nonnull ValueModuleRegistry<VM, B> registry, @Nonnull ValueFetcher<VM, ID, B> fetcher, @Nonnull B baseBean, @Nonnull D delegate, @Nullable Object parent, @Nullable Object container, @Nullable InitialSource initialSource) throws Exception {
        // noinspection unchecked
        final Class<D> type = (Class<D>) delegate.getClass();
        final MethodHandler handler = handlerFor(registry, fetcher, baseBean, delegate, parent, container, initialSource);
        final ProxyFactory factory = factoryFor(type);
        try {
            // noinspection unchecked
            return (D) factory.create(new Class[0], new Object[0], handler);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Could not find an empty public constructor in " + type.getName() + ".", e);
        }
    }

    @Nonnull
    protected <VM extends ValueModule, ID, B, D> D createForInternal(@Nonnull ValueModuleRegistry<VM, B> registry, @Nonnull ValueFetcher<VM, ID, B> fetcher, @Nonnull B baseBean, @Nonnull D delegate, @Nullable Object parent, @Nullable Object container, @Nullable InitialSource initialSource) {
        try {
            return createForInternalUnchecked(registry, fetcher, baseBean, delegate, parent, container, initialSource);
        } catch (InstantiationException | InvocationTargetException | ExceptionInInitializerError e) {
            final Throwable cause = e.getCause();
            if (cause instanceof Error) {
                // noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
                throw (Error)cause;
            } else if (cause instanceof RuntimeException) {
                // noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
                throw (RuntimeException)cause;
            } else if (e instanceof ExceptionInInitializerError) {
                throw (ExceptionInInitializerError)e;
            } else {
                throw new RuntimeException("Could not create instance for " + registry + ".", e);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Could not create instance for " + registry + ".", e);
        }
    }

    @Nonnull
    protected <VM extends ValueModule, ID, B, D> MethodHandler handlerFor(@Nonnull ValueModuleRegistry<VM, B> registry, @Nonnull ValueFetcher<VM, ID, B> fetcher, @Nonnull B baseBean, @Nonnull D delegate, @Nullable Object parent, @Nullable Object container, @Nullable InitialSource initialSource) {
        return new ValueModuleProxyHandlerImpl<>(registry, fetcher, baseBean, delegate, parent, container, initialSource);
    }

    @Nonnull
    protected  <T> ProxyFactory factoryFor(@Nonnull Class<T> type) {
        final ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(type);
        factory.setInterfaces(new Class[]{ ValueModuleProxy.class });
        return factory;
    }

    @Nonnull
    @Override
    public <B> B getDelegate(@Nonnull B base) {
        final B result;
        if (base instanceof ProxyObject) {
            final MethodHandler plainHandler = ((ProxyObject) base).getHandler();
            if (plainHandler instanceof ValueModuleProxyHandlerImpl) {
                final ValueModuleProxyHandlerImpl<?, ?, ?, ?> handler = (ValueModuleProxyHandlerImpl<?, ?, ?, ?>) plainHandler;
                // noinspection unchecked
                result = (B) handler.getDelegate();
            } else {
                result = base;
            }
        } else {
            result = base;
        }
        return result;
    }

    protected class ValueModuleProxyHandlerImpl<VM extends ValueModule, ID, B, D> extends ValueModuleProxyHandlerSupport<VM, B, D> {

        private final ValueFetcher<VM, ID, B> _fetcher;
        private final InitialSource _initialSource;

        private volatile B _baseBean;
        private volatile Object _parent;
        private volatile Object _container;

        public ValueModuleProxyHandlerImpl(@Nonnull ValueModuleRegistry<VM, B> registry, @Nonnull ValueFetcher<VM, ID, B> fetcher, @Nonnull B baseBean, @Nonnull D delegate, @Nullable Object parent, @Nullable Object container, @Nullable InitialSource initialSource) {
            super(delegate, registry);
            _fetcher = fetcher;
            _baseBean = baseBean;
            _parent = parent;
            _container = container;
            _initialSource = initialSource;
        }

        @Override
        public Object invoke(@Nonnull Object proxy, @Nonnull Method thisMethod, @Nonnull Method proceed, @Nonnull Object[] args) throws Throwable {
            if (_baseBean == null) {
                throw new BeanDisconnectedException("This instance of " + getDelegate().getClass() + " was already disconnected from baseBean. No invocation of each module method is possible.");
            }
            return super.invoke(proxy, thisMethod, proceed, args);
        }

        @Override
        @Nonnull
        protected Object loadAndBackupValueFor(@Nonnull Object proxy, @Nonnull PropertyDescriptor descriptor) throws Exception {
            final B baseBean = getBaseBean();
            final SourcePath sourcePath = getSourcePathOf(proxy, _initialSource);
            final VM valueModule = getRegistry().findModuleFor(descriptor);
            if (valueModule == null) {
                throw new IllegalArgumentException("Could not find a module for " + descriptor + ".");
            }
            final Object value = _fetcher.fetchValueOf(baseBean, valueModule, sourcePath);
            final Object newValue;
            if (value != null) {
                final Class<?> propertyType = descriptor.getPropertyType();
                if (Set.class.equals(propertyType)) {
                    // noinspection unchecked
                    newValue = unmodifiableSet(toIterable(baseBean, proxy, (Iterable) value, descriptor, HashSet.class));
                } else if (List.class.equals(propertyType) || Collection.class.equals(propertyType)) {
                    // noinspection unchecked
                    newValue = unmodifiableList((toIterable(baseBean, proxy, (Iterable) value, descriptor, ArrayList.class)));
                } else if (Map.class.equals(propertyType)) {
                    newValue = unmodifiableMap(toMap(baseBean, (Map) value, descriptor));
                } else if (Map.class.isAssignableFrom(propertyType) || Collection.class.isAssignableFrom(propertyType)) {
                    throw new IllegalArgumentException("The property " + descriptor + " could not be used for automatic value module system. Because the type is to specialized. Use a type like " + Set.class.getName() + ", " + List.class + ", " + Collection.class + " or " + Map.class + ".");
                } else if (_fetcher.isResponsibleFor(propertyType)) {
                    newValue = createForInternal(getRegistry(), _fetcher, baseBean, value, proxy, null, _initialSource);
                } else {
                    newValue = value;
                }
            } else {
                newValue = null;
            }
            return setValueAndGetNewProxyFor(proxy, descriptor, newValue);
        }

        @Nonnull
        protected <T extends Collection<Object>> T toIterable(@Nonnull B baseBean, @Nonnull Object parent, @Nonnull Iterable<?> originals, @Nonnull PropertyDescriptor descriptor, @Nonnull Class<T> collectionType) {
            final Class<?> componentType = extractComponentClassOfIterable(descriptor);
            final T result;
            if (_fetcher.isResponsibleFor(componentType) || collectionType.isAssignableFrom(originals.getClass())) {
                try {
                    result = collectionType.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("Could not create new instance of " + collectionType.getName() + ".", e);
                }
                final Object container = result instanceof List || Collection.class.equals(collectionType) ? result : null;
                for (Object original : originals) {
                    if (_fetcher.isResponsibleFor(componentType)) {
                        result.add(createForInternal(getRegistry(), _fetcher, baseBean, original, parent, container, _initialSource));
                    } else {
                        result.add(original);
                    }
                }
            } else {
                // noinspection unchecked
                result = (T) originals;
            }
            return result;
        }

        @Nonnull
        protected Map<?, ?> toMap(@Nonnull B baseBean, @Nonnull Map<?, ?> original, @Nonnull PropertyDescriptor descriptor) {
            throw new UnsupportedOperationException("Maps are currently not supported to transform " + original + "(" + descriptor + ") of " + baseBean + " to a controlled map.");
        }

        @Nonnull
        @Override
        protected Object setValueAndGetNewProxyFor(@Nonnull Object proxy, @Nonnull PropertyDescriptor descriptor, @Nullable Object value) throws Exception {
            final Object old = invoke(descriptor.getReadMethod());
            invoke(descriptor.getWriteMethod(), value);
            disconnect(old);
            return value;
        }

        @Nonnull
        protected B getBaseBean() {
            final B baseBean = _baseBean;
            if (baseBean == null) {
                throw new BeanDisconnectedException("This instance of " + getDelegate().getClass() + " was already disconnected from baseBean. No invocation of each module method is possible.");
            }
            return baseBean;
        }

        @Nullable
        protected SourcePath getSourcePathOf(@Nonnull Object proxy, @Nullable InitialSource initialSource) {
            Object currentProxy = proxy;
            final List<SourceElement> result = new ArrayList<>(10);
            while (currentProxy != null) {
                if (currentProxy instanceof ProxyObject) {
                    final MethodHandler plainHandler = ((ProxyObject) currentProxy).getHandler();
                    if (plainHandler instanceof ValueModuleProxyHandlerImpl) {
                        final ValueModuleProxyHandlerImpl<?, ?, ?, ?> handler = (ValueModuleProxyHandlerImpl<?, ?, ?, ?>) plainHandler;
                        final Object container = handler._container;
                        // noinspection ObjectEquality
                        if (container == DISCONNECTED) {
                            throw new BeanDisconnectedException("This instance of " + handler.getDelegate() + " was already disconnected from parent. No invocation of each module method is possible.");
                        }
                        if (container instanceof List) {
                            result.add(0, new IndexedSourceElement(currentProxy, indexOf(currentProxy, (Iterable<?>) container)));
                        } else if (container != null) {
                            throw new IllegalStateException("Currently only container of type " + List.class + " are supported but found " + container + " for " + handler.getDelegate() + ".");
                        } else {
                            result.add(0, new SourceElement(currentProxy));
                        }
                        currentProxy = handler._parent;
                        // noinspection ObjectEquality
                        if (currentProxy == DISCONNECTED) {
                            throw new BeanDisconnectedException("This instance of " + handler.getDelegate() + " was already disconnected from parent. No invocation of each module method is possible.");
                        }
                    } else {
                        currentProxy = null;
                    }
                } else {
                    currentProxy = null;
                }
            }
            return new SourcePath(initialSource) { @Override public Iterator<SourceElement> iterator() {
                return result.iterator();
            }};
        }

        @Nonnegative
        protected int indexOf(@Nonnull Object o, @Nonnull Iterable<?> in) {
            Integer result = null;
            int i = 0;
            for (Object test : in) {
                // noinspection ObjectEquality
                if (test == o) {
                    result = i;
                }
                i++;
            }
            if (result == null) {
                throw new IllegalStateException("Could not find " + o + " in container " + in + ".");
            }
            return result;
        }

        protected void disconnect(@Nullable Object old) {
            if (old instanceof ProxyObject) {
                final MethodHandler plainHandler = ((ProxyObject) old).getHandler();
                if (plainHandler instanceof ValueModuleProxyHandlerImpl) {
                    final ValueModuleProxyHandlerImpl<?, ?, ?, ?> handler = (ValueModuleProxyHandlerImpl<?, ?, ?, ?>) plainHandler;
                    handler._baseBean = null;
                    handler._parent = DISCONNECTED;
                    handler._container = DISCONNECTED;
                }
            }
        }
    }
}

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

package org.echocat.jomon.maven.boot;

import org.apache.maven.artifact.Artifact;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;
import java.util.concurrent.Callable;

public class ArtifactWithDependenciesFactory {

    private static final Class<?>[] INTERFACES = new Class<?>[]{ArtifactWithDependencies.class, ProxyHelper.class};
    private static final Method GET_DEPENDENCIES_METHOD;
    private static final Method GET_DELEGATE_METHOD;
    private static final Method EQUALS_METHOD;

    static {
        try {
            EQUALS_METHOD = Object.class.getDeclaredMethod("equals", Object.class);
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException("Could not find " + Object.class.getName() + ".equals().", e);
        }
        try {
            GET_DEPENDENCIES_METHOD = ArtifactWithDependencies.class.getDeclaredMethod("getDependencies");
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException("Could not find " + ArtifactWithDependencies.class.getName() + ".getDependencies().", e);
        }
        if (!Set.class.equals(GET_DEPENDENCIES_METHOD.getReturnType())) {
            throw new IllegalStateException(ArtifactWithDependencies.class.getName() + ".getDependencies() does not return " + Set.class.getName() + ".");
        }
        try {
            GET_DELEGATE_METHOD = ProxyHelper.class.getDeclaredMethod("getDelegate");
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException("Could not find " + ProxyHelper.class.getName() + ".getDelegate().", e);
        }
        if (!Artifact.class.equals(GET_DELEGATE_METHOD.getReturnType())) {
            throw new IllegalStateException(ProxyHelper.class.getName() + ".getDelegate() does not return " + Set.class.getName() + ".");
        }
    }

    @Nonnull
    public static ArtifactWithDependencies create(@Nonnull final Artifact delegate, @Nonnull final Callable<Set<ArtifactWithDependencies>> getDependencies) {
        //noinspection unchecked
        return (ArtifactWithDependencies) Proxy.newProxyInstance(ArtifactWithDependencies.class.getClassLoader(), INTERFACES, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                final Object result;
                if (GET_DEPENDENCIES_METHOD.equals(method)) {
                    result = getDependencies.call();
                } else if (GET_DELEGATE_METHOD.equals(method)) {
                    result = delegate;
                } else if (EQUALS_METHOD.equals(method)) {
                    result = method.invoke(delegate, args[0] instanceof ProxyHelper ? ((ProxyHelper)args[0]).getDelegate() : args[0]);
                } else {
                    result = method.invoke(delegate, args);
                }
                return result;
            }
        });
    }

    private ArtifactWithDependenciesFactory() {}

    private static interface ProxyHelper {
        public Artifact getDelegate();
    }
}

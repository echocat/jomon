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

package org.echocat.jomon.net.dns;

import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SimpleResolver;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;

import static java.lang.reflect.Modifier.isStatic;

public class ResolverUtils {

    private static final Method SIMPLE_RESOLVER_GET_ADDRESS_METHOD = tryGetSimpleResolverGetAddressMethod();
    private static final Field LOOKUP_RESOLVER_FIELD = tryGetLookupResolverField();

    @Nonnull
    public static String toString(@Nullable Resolver resolver) {
        final String result;
        if (resolver instanceof SimpleResolver) {
            result = toString((SimpleResolver)resolver);
        } else if (resolver instanceof ExtendedResolver) {
            result = toString((ExtendedResolver)resolver);
        } else {
            result = resolver != null ? resolver.toString() : "null";
        }
        return result;
    }

    @Nonnull
    public static String toString(@Nullable SimpleResolver resolver) {
        final InetSocketAddress address = tryGetAddressOf(resolver);
        final String result;
        if (address != null) {
            result = address.toString();
        } else if (resolver != null) {
            result = resolver.toString();
        } else {
            result = "null";
        }
        return result;
    }

    @Nonnull
    public static String toString(@Nullable ExtendedResolver resolver) {
        final StringBuilder result = new StringBuilder();
        if (resolver != null) {
            final Resolver[] resolvers = resolver.getResolvers();
            if (resolvers != null && resolvers.length > 0) {
                for (int i = 0; i < resolvers.length; i++) {
                    if (i > 0) {
                        result.append(',');
                    }
                    result.append(toString(resolvers[i]));
                }
            } else {
                result.append("null");
            }
        } else {
            result.append("null");
        }
        return result.toString();
    }

    @Nonnull
    public static String toStringOfResolver(@Nullable Lookup lookup) {
        final Resolver resolver = findResolverOf(lookup);
        return resolver != null ? toString(resolver) : "unknown";
    }

    @Nullable
    public static Resolver findResolverOf(@Nullable Lookup lookup) {
        Resolver resolver = null;
        if (lookup != null && LOOKUP_RESOLVER_FIELD != null) {
            try {
                resolver = (Resolver) LOOKUP_RESOLVER_FIELD.get(lookup);
            } catch (IllegalAccessException ignored) {}
        }
        return resolver;
    }

    @Nullable
    private static InetSocketAddress tryGetAddressOf(@Nullable SimpleResolver resolver) {
        InetSocketAddress result = null;
        if (resolver != null && SIMPLE_RESOLVER_GET_ADDRESS_METHOD != null) {
            try {
                result = (InetSocketAddress) SIMPLE_RESOLVER_GET_ADDRESS_METHOD.invoke(resolver);
            } catch (IllegalAccessException | InvocationTargetException ignored) {}
        }
        return result;
    }

    @Nullable
    private static Method tryGetSimpleResolverGetAddressMethod() {
        final Method possibleMethod = findPossibleSimpleResolverGetAddressMethod();
        return isValidSimpleResolverGetAddressMethod(possibleMethod) ? possibleMethod : null;
    }

    private static boolean isValidSimpleResolverGetAddressMethod(@Nullable Method possibleMethod) {
        final int modifiers = possibleMethod != null ? possibleMethod.getModifiers() : -1;
        return possibleMethod != null && !isStatic(modifiers) && InetSocketAddress.class.equals(possibleMethod.getReturnType());
    }

    @Nullable
    private static Method findPossibleSimpleResolverGetAddressMethod() {
        Method possibleMethod;
        try {
            possibleMethod = SimpleResolver.class.getDeclaredMethod("getAddress");
            possibleMethod.setAccessible(true);
        } catch (NoSuchMethodException | SecurityException ignored) {
            possibleMethod = null;
        }
        return possibleMethod;
    }

    @Nullable
    private static Field tryGetLookupResolverField() {
        Field field;
        try {
            field = Lookup.class.getDeclaredField("resolver");
            field.setAccessible(true);
            final int modifiers = field.getModifiers();
            if (isStatic(modifiers) || !Resolver.class.equals(field.getType())) {
                field = null;
            }
        } catch (NoSuchFieldException | SecurityException ignored) {
            field = null;
        }
        return field;
    }

    private ResolverUtils() {}
}

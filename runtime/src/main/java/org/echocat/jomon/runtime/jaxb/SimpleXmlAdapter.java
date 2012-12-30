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

package org.echocat.jomon.runtime.jaxb;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.lang.reflect.Modifier.isStatic;
import static java.util.Collections.unmodifiableMap;

public class SimpleXmlAdapter extends XmlAdapter<String, Object> {

    private static final Map<String, XmlAdapter<String, ?>> PREFIX_TO_ADAPTER = createPrefixedAdaptersFor(
        Boolean.class,
        Byte.class,
        Short.class,
        Character.class,
        Integer.class,
        BigInteger.class,
        Long.class,
        Float.class,
        Double.class,
        BigDecimal.class,
        String.class,
        Date.class
    );

    @Nonnull
    private static Map<String, XmlAdapter<String, ?>> createPrefixedAdaptersFor(@Nonnull Class<?>... types) {
        final Map<String, XmlAdapter<String, ?>> prefixToAdapter = new HashMap<>();
        for (Class<?> type : types) {
            XmlAdapter<String, ?> adapter;
            if (Character.class.equals(type)) {
                adapter = new CharacterBasedXmlAdapter();
            } else if (Date.class.equals(type)) {
                adapter = new DateBasedXmlAdapter();
            } else {
                adapter = ValueOfBasedXmlAdapter.tryCreateFor(type);
                if (adapter == null) {
                    adapter = ConstructorBasedXmlAdapter.tryCreateFor(type);
                }
            }
            if (adapter == null) {
                throw new IllegalArgumentException("Could not create adapter for: " + type.getName());
            }
            prefixToAdapter.put(type.getSimpleName().toLowerCase(), adapter);
        }
        return unmodifiableMap(prefixToAdapter);
    }

    @Override
    public Object unmarshal(String v) throws Exception {
        final Object result;
        if (v != null) {
            final int separator = v.indexOf(':');
            if (separator <= 0 && v.length() <= separator + 1) {
                throw new IllegalArgumentException("Could not parse: " + v);
            }
            final String prefix = v.substring(0, separator);
            final String value = v.substring(separator + 1);
            final XmlAdapter<String, ?> adapter = PREFIX_TO_ADAPTER.get(prefix);
            if (adapter == null) {
                throw new IllegalArgumentException("Could not parse: " + v);
            }
            result = adapter.unmarshal(value);
        } else {
            result = null;
        }
        return result;
    }

    @Override
    public String marshal(Object v) throws Exception {
        final String result;
        if (v != null) {
            final Class<?> type = v.getClass();
            final String prefix = type.getSimpleName().toLowerCase();
            // noinspection unchecked
            final XmlAdapter<String, Object> adapter = (XmlAdapter<String, Object>) PREFIX_TO_ADAPTER.get(prefix);
            if (adapter == null) {
                throw new IllegalArgumentException("Could not marshal: " + v);
            }
            result = prefix + ":" + adapter.marshal(v);
        } else {
            result = null;
        }
        return result;
    }

    private static class ValueOfBasedXmlAdapter<T> extends XmlAdapter<String, T> {

        @Nullable
        private static <T> ValueOfBasedXmlAdapter<T> tryCreateFor(@Nonnull Class<T> type) {
            final Method method = findValueOfMethodFor(type);
            // noinspection unchecked
            return method != null ? (ValueOfBasedXmlAdapter<T>) new ValueOfBasedXmlAdapter<>(method) : null;
        }

        @Nullable
        private static Method findValueOfMethodFor(@Nonnull Class<?> type) {
            Method result;
            try {
                result = type.getMethod("valueOf", String.class);
                final int modifiers = result.getModifiers();
                if (!result.getReturnType().equals(type) || !isStatic(modifiers)) {
                    result = null;
                }
            } catch (NoSuchMethodException ignored) {
                result = null;
            }
            return result;
        }

        private final Method _valueOfMethod;

        private ValueOfBasedXmlAdapter(@Nonnull Method valueOfMethod) {
            _valueOfMethod = valueOfMethod;
        }

        @Override
        public T unmarshal(String v) throws Exception {
            // noinspection unchecked
            return v != null ? (T) _valueOfMethod.invoke(null, v) : null;
        }

        @Override
        public String marshal(T v) throws Exception {
            return v != null ? v.toString() : null;
        }
    }

    private static class ConstructorBasedXmlAdapter<T> extends XmlAdapter<String, T> {

        @Nullable
        private static <T> ConstructorBasedXmlAdapter<T> tryCreateFor(@Nonnull Class<T> type) {
            final Constructor<T> constructor = findConstructorFor(type);
            return constructor != null ? new ConstructorBasedXmlAdapter<>(constructor) : null;
        }

        @Nullable
        private static <T> Constructor<T> findConstructorFor(@Nonnull Class<T> type) {
            Constructor<T> result;
            try {
                result = type.getConstructor(String.class);
            } catch (NoSuchMethodException ignored) {
                result = null;
            }
            return result;
        }

        private final Constructor<T> _constructor;

        private ConstructorBasedXmlAdapter(@Nonnull Constructor<T> constructor) {
            _constructor = constructor;
        }

        @Override
        public T unmarshal(String v) throws Exception {
            return v != null ? _constructor.newInstance(v) : null;
        }

        @Override
        public String marshal(T v) throws Exception {
            return v != null ? v.toString() : null;
        }
    }

    private static class CharacterBasedXmlAdapter extends XmlAdapter<String, Character> {

        @Override
        public Character unmarshal(String v) throws Exception {
            return v != null && !v.isEmpty() ? v.charAt(0) : null;
        }

        @Override
        public String marshal(Character v) throws Exception {
            return v != null ? new String(new char[]{v}) : null;
        }
    }

    private static class DateBasedXmlAdapter extends XmlAdapter<String, Date> {

        @Override
        public Date unmarshal(String v) throws Exception {
            return v != null ? new Date(Long.valueOf(v)) : null;
        }

        @Override
        public String marshal(Date v) throws Exception {
            return v != null ? Long.toString(v.getTime()) : null;
        }
    }


}

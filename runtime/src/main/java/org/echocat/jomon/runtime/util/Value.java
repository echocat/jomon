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

package org.echocat.jomon.runtime.util;

import com.google.common.collect.Sets;
import org.echocat.jomon.runtime.jaxb.PatternAdapter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Set;
import java.util.regex.Pattern;

@XmlTransient
public abstract class Value<T> {

    @SuppressWarnings("ClassReferencesSubclass")
    public static final Set<Class<? extends Value<?>>> ALL_DEFAULT_VALUE_TYPES = Sets.<Class<? extends Value<?>>>newHashSet(
        BooleanValue.class,
        ByteValue.class,
        CharacterValue.class,
        ShortValue.class,
        IntegerValue.class,
        LongValue.class,
        BigIntegerValue.class,
        FloatValue.class,
        DoubleValue.class,
        BigDecimalValue.class,
        StringValue.class,
        DateValue.class,
        PatternValue.class
    );
    
    @Nonnull
    public abstract T getValue();

    @Nullable
    public static <T> Value<T> valueOf(@Nullable T value) {
        final Value<?> result;
        if (value != null) {
            if (value instanceof Boolean) {
                result = new BooleanValue();
            } else if (value instanceof Byte) {
                result = new ByteValue();
            } else if (value instanceof Character) {
                result = new CharacterValue();
            } else if (value instanceof Short) {
                result = new ShortValue();
            } else if (value instanceof Integer) {
                result = new IntegerValue();
            } else if (value instanceof Long) {
                result = new LongValue();
            } else if (value instanceof BigInteger) {
                result = new BigIntegerValue();
            } else if (value instanceof Float) {
                result = new FloatValue();
            } else if (value instanceof Double) {
                result = new DoubleValue();
            } else if (value instanceof BigDecimal) {
                result = new BigDecimalValue();
            } else if (value instanceof String) {
                result = new StringValue();
            } else if (value instanceof Date) {
                result = new DateValue();
            } else if (value instanceof Pattern) {
                result = new PatternValue();
            } else {
                throw new IllegalArgumentException("Don't know how to handle: " + value);
            }
            // noinspection ClassReferencesSubclass,unchecked
            ((ValueSupport<Object>)result).setValue(value);
        } else {
            result = null;
        }
        // noinspection unchecked
        return (Value<T>) result;
    }

    @XmlType(name = "boolean")
    @XmlRootElement(name = "boolean")
    public static class BooleanValue extends ValueSupport<Boolean> {
            @Override @XmlAttribute(name = "content") public Boolean getValue() { return super.getValue(); }
            @Override public void setValue(Boolean content) { super.setValue(content); }
    }

    @XmlType(name = "byte")
    @XmlRootElement(name = "byte")
    public static class ByteValue extends ValueSupport<Byte> {
            @Override @XmlAttribute(name = "content") public Byte getValue() { return super.getValue(); }
            @Override public void setValue(Byte content) { super.setValue(content); }
    }

    @XmlType(name = "character")
    @XmlRootElement(name = "character")
    public static class CharacterValue extends ValueSupport<Character> {
            @Override @XmlAttribute(name = "content") public Character getValue() { return super.getValue(); }
            @Override public void setValue(Character content) { super.setValue(content); }
    }

    @XmlType(name = "short")
    @XmlRootElement(name = "short")
    public static class ShortValue extends ValueSupport<Short> {
            @Override @XmlAttribute(name = "content") public Short getValue() { return super.getValue(); }
            @Override public void setValue(Short content) { super.setValue(content); }
    }

    @XmlType(name = "integer")
    @XmlRootElement(name = "integer")
    public static class IntegerValue extends ValueSupport<Integer> {
            @Override @XmlAttribute(name = "content") public Integer getValue() { return super.getValue(); }
            @Override public void setValue(Integer content) { super.setValue(content); }
    }

    @XmlType(name = "long")
    @XmlRootElement(name = "long")
    public static class LongValue extends ValueSupport<Long> {
            @Override @XmlAttribute(name = "content") public Long getValue() { return super.getValue(); }
            @Override public void setValue(Long content) { super.setValue(content); }
    }

    @XmlType(name = "bigInteger")
    @XmlRootElement(name = "bigInteger")
    public static class BigIntegerValue extends ValueSupport<BigInteger> {
            @Override @XmlAttribute(name = "content") public BigInteger getValue() { return super.getValue(); }
            @Override public void setValue(BigInteger content) { super.setValue(content); }
    }

    @XmlType(name = "float")
    @XmlRootElement(name = "float")
    public static class FloatValue extends ValueSupport<Float> {
            @Override @XmlAttribute(name = "content") public Float getValue() { return super.getValue(); }
            @Override public void setValue(Float content) { super.setValue(content); }
    }

    @XmlType(name = "double")
    @XmlRootElement(name = "double")
    public static class DoubleValue extends ValueSupport<Double> {
            @Override @XmlAttribute(name = "content") public Double getValue() { return super.getValue(); }
            @Override public void setValue(Double content) { super.setValue(content); }
    }

    @XmlType(name = "bigDecimal")
    @XmlRootElement(name = "bigDecimal")
    public static class BigDecimalValue extends ValueSupport<BigDecimal> {
            @Override @XmlAttribute(name = "content") public BigDecimal getValue() { return super.getValue(); }
            @Override public void setValue(BigDecimal content) { super.setValue(content); }
    }

    @XmlType(name = "string")
    @XmlRootElement(name = "string")
    public static class StringValue extends ValueSupport<String> {
        @Override @XmlAttribute(name = "content") public String getValue() { return super.getValue(); }
        @Override public void setValue(String content) { super.setValue(content); }
    }

    @XmlType(name = "date")
    @XmlRootElement(name = "date")
    public static class DateValue extends ValueSupport<Date> {
            @Override @XmlAttribute(name = "content") public Date getValue() { return super.getValue(); }
            @Override public void setValue(Date content) { super.setValue(content); }
    }

    @XmlType(name = "pattern")
    @XmlRootElement(name = "pattern")
    public static class PatternValue extends ValueSupport<Pattern> {
            @Override @XmlAttribute(name = "content") @XmlJavaTypeAdapter(PatternAdapter.class) public Pattern getValue() { return super.getValue(); }
            @Override public void setValue(Pattern content) { super.setValue(content); }
    }

    @XmlTransient
    public abstract static class ValueSupport<T> extends Value<T> {

        private T _content;

        @Override
        @Nullable
        @XmlAttribute(name = "content")
        public T getValue() {
            return _content;
        }

        public void setValue(T content) {
            _content = content;
        }

        @Override
        public boolean equals(Object o) {
            final boolean result;
            if (this == o) {
                result = true;
            } else if (!(o instanceof Value)) {
                result = false;
            } else {
                final Value<?> that = (Value) o;
                final T content = getValue();
                result = content != null ? content.equals(that.getValue()) : that.getValue() == null;
            }
            return result;
        }

        @Override
        public int hashCode() {
            final T content = getValue();
            return content != null ? content.hashCode() : 0;
        }

        @Override
        public String toString() {
            final T content = getValue();
            return content != null ? content.toString() : "null";
        }
    }

}

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

import org.echocat.jomon.runtime.util.TypeEnabledPhoneNumber.Adapter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import static org.echocat.jomon.runtime.util.TypeEnabledPhoneNumber.Type.regular;
import static org.apache.commons.lang3.StringUtils.split;

@ThreadSafe
@Immutable
@XmlJavaTypeAdapter(Adapter.class)
public class TypeEnabledPhoneNumber extends PhoneNumber {

    public static enum Type {
        regular,
        fax,
        mobile,
        other
    }

    @Nonnull
    public static TypeEnabledPhoneNumber phoneNumber(@Nonnull Type type, @Nonnull String value) {
        return phoneNumber(type, value, null);
    }

    @Nonnull
    public static TypeEnabledPhoneNumber phoneNumber(@Nonnull Type type, @Nonnull String value, @Nullable String region) {
        return new TypeEnabledPhoneNumber(type, value, region);
    }

    @Nonnull
    public static TypeEnabledPhoneNumber phoneNumber(@Nonnull Type type, @Nonnull PhoneNumber value) {
        return new TypeEnabledPhoneNumber(type, value);
    }

    private final Type _type;

    public TypeEnabledPhoneNumber(@Nonnull Type type, @Nonnull String value) throws IllegalArgumentException {
        this(type, value, null);
    }

    public TypeEnabledPhoneNumber(@Nonnull Type type, @Nonnull String value, @Nullable String region) throws IllegalArgumentException {
        super(value, region);
        _type = type;
    }

    public TypeEnabledPhoneNumber(@Nonnull Type type, @Nonnull PhoneNumber value) throws IllegalArgumentException {
        super(value);
        _type = type;
    }


    @Nonnull
    public Type getType() {
        return _type;
    }

    public static class Adapter extends XmlAdapter<String, TypeEnabledPhoneNumber> {

        @Override
        public TypeEnabledPhoneNumber unmarshal(String v) throws Exception {
            final String[] parts = split(v, ":", 2);
            final Type type;
            final String value;
            if (parts.length == 2) {
                type = Type.valueOf(parts[0].trim());
                value = parts[1].trim();
            } else {
                type = regular;
                value = v.trim();
            }
            return v != null ? new TypeEnabledPhoneNumber(type, value) : null;
        }

        @Override
        public String marshal(TypeEnabledPhoneNumber v) throws Exception {
            return v != null ? v.getType() + ":" + v.format() : null;
        }
    }

}

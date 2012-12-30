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

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber;
import org.echocat.jomon.runtime.util.PhoneNumber.Adapter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import static com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat.*;
import static org.echocat.jomon.runtime.util.PhoneNumber.Format.*;

@ThreadSafe
@Immutable
@XmlJavaTypeAdapter(Adapter.class)
public class PhoneNumber {

    public enum Format {
        e164,
        international,
        national,
        rfc3966
    }

    private static final PhoneNumberUtil UTIL = PhoneNumberUtil.getInstance();

    private final Phonenumber.PhoneNumber _value;

    public PhoneNumber(@Nonnull String value) throws IllegalArgumentException {
        this(value, null);
    }

    public PhoneNumber(@Nonnull String value, @Nullable String region) throws IllegalArgumentException {
        try {
            _value = UTIL.parse(value, region != null ? region.toUpperCase() : null);
        } catch (NumberParseException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    protected PhoneNumber(@Nonnull PhoneNumber original) {
        _value = original._value;
        UTIL.getRegionCodeForNumber(_value);
    }

    public boolean hasCountry() {
        return _value.hasCountryCode();
    }

    @Nullable
    public Integer getCountry() {
        return _value.hasCountryCode() ? _value.getCountryCode() : null;
    }

    public boolean hasCountryCode() {
        return getCountryCode() != null;
    }

    @Nullable
    public String getCountryCode() {
        return _value.hasCountryCode() ? UTIL.getRegionCodeForNumber(_value) : null;
    }

    public boolean hasNational() {
        return _value.hasNationalNumber();
    }

    @Nullable
    public Long getNational() {
        return _value.hasExtension() ? _value.getNationalNumber() : null;
    }

    public boolean hasExtension() {
        return _value.hasExtension();
    }

    @Nullable
    public String getExtension() {
        return _value.hasExtension() ? _value.getExtension() : null;
    }

    @Nonnull
    public String format() {
        return format(e164);
    }

    @Nonnull
    public String format(@Nonnull Format format) {
        final PhoneNumberFormat phoneNumberFormat = asPhoneNumberFormat(format);
        return UTIL.format(_value, phoneNumberFormat);
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else if (!(o instanceof PhoneNumber)) {
            result = false;
        } else {
            final PhoneNumber that = (PhoneNumber) o;
            result = _value != null ? _value.equals(that._value) : that._value == null;
        }
        return result;
    }

    @Override
    public int hashCode() {
        return _value != null ? _value.hashCode() : 0;
    }

    @Override
    public String toString() {
        return format();
    }

    public static class Adapter extends XmlAdapter<String, PhoneNumber> {

        @Override
        public PhoneNumber unmarshal(String v) throws Exception {
            return v != null ? new PhoneNumber(v) : null;
        }

        @Override
        public String marshal(PhoneNumber v) throws Exception {
            return v != null ? v.format() : null;
        }
    }

    @Nonnull
    private static PhoneNumberFormat asPhoneNumberFormat(@Nonnull Format format) {
        final PhoneNumberFormat phoneNumberFormat;
        if (format == e164) {
            phoneNumberFormat = E164;
        } else if (format == international) {
            phoneNumberFormat = INTERNATIONAL;
        } else if (format == national) {
            phoneNumberFormat = NATIONAL;
        } else if (format == rfc3966) {
            phoneNumberFormat = RFC3966;
        } else {
            throw new IllegalArgumentException("Don't know to handle " + format + ".");
        }
        return phoneNumberFormat;
    }

}

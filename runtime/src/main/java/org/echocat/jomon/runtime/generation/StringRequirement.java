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

package org.echocat.jomon.runtime.generation;

import org.echocat.jomon.runtime.generation.StringRequirement.Adapter;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@ThreadSafe
@Immutable
@XmlJavaTypeAdapter(Adapter.class)
public class StringRequirement implements Requirement {

    public static final String UNIQUE_VALUE_PLACE_HOLDER = "${uniqueValue}";

    private final String _pattern;

    public StringRequirement(@Nonnull String pattern) {
        _pattern = pattern;
    }

    @Nonnull
    public String getPattern() {
        return _pattern;
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else if (o == null || getClass() != o.getClass()) {
            result = false;
        } else {
            final StringRequirement that = (StringRequirement) o;
            result = _pattern.equals(that._pattern);
        }
        return result;
    }

    @Override
    public int hashCode() {
        return _pattern.hashCode();
    }

    public static class Adapter extends XmlAdapter<String, StringRequirement> {

        @Override
        public StringRequirement unmarshal(String v) throws Exception {
            return v != null ? new StringRequirement(v) : null;
        }

        @Override
        public String marshal(StringRequirement v) throws Exception {
            return v != null ? v.getPattern() : null;
        }

    }
}

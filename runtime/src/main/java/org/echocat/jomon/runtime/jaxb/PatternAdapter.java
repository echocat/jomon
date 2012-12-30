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

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.regex.Pattern.compile;
import static org.apache.commons.lang3.ArrayUtils.toObject;

public class PatternAdapter extends XmlAdapter<String, Pattern> {

    private static final Pattern EXTRACT_PATTERN = Pattern.compile("^\\/(.*)\\/([imsx]*)$");

    @Override
    public Pattern unmarshal(String v) throws Exception {
        final Pattern pattern;
        if (v != null) {
            final Matcher matcher = EXTRACT_PATTERN.matcher(v);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("Could not parse: " + v);
            }
            final String patternAsString = matcher.group(1);
            final String flagsAsString = matcher.group(2);
            // noinspection MagicConstant
            pattern = compile(patternAsString, toFlags(flagsAsString));
        } else {
            pattern = null;
        }
        return pattern;
    }

    @Override
    public String marshal(Pattern v) throws Exception {
        final String patternAsString;
        if (v != null) {
            patternAsString = "/" + v.pattern() + "/" + toFlagsAsString(v.flags());
        } else {
            patternAsString = null;
        }
        return patternAsString;
    }

    @Nonnegative
    protected int toFlags(@Nonnull String flagsAsString) {
        final Set<Character> flagsAsCharacters = newHashSet(toObject(flagsAsString.toCharArray()));
        int flags = 0;
        if (flagsAsCharacters.contains('i')) {
            flags |= Pattern.CASE_INSENSITIVE;
        }
        if (flagsAsCharacters.contains('m')) {
            flags |= Pattern.MULTILINE;
        }
        if (flagsAsCharacters.contains('s')) {
            flags |= Pattern.DOTALL;
        }
        if (flagsAsCharacters.contains('x')) {
            flags |= Pattern.COMMENTS;
        }
        return flags;
    }

    @Nonnull
    protected String toFlagsAsString(@Nonnegative int flags) {
        final StringBuilder sb = new StringBuilder();
        if ((flags & Pattern.CASE_INSENSITIVE) != 0) {
            sb.append('i');
        }
        if ((flags & Pattern.MULTILINE) != 0) {
            sb.append('m');
        }
        if ((flags & Pattern.DOTALL) != 0) {
            sb.append('s');
        }
        if ((flags & Pattern.COMMENTS) != 0) {
            sb.append('x');
        }
        return sb.toString();
    }
}

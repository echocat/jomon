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

package org.echocat.jomon.process;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;
import static org.echocat.jomon.runtime.CollectionUtils.asImmutableList;

public class ProcessUtils {

    private static final Pattern ESCAPED_ARGUMENT_PATTERN = compile("([\\s\"'])");

    @Nonnull
    public static String toEscapedCommandLine(@Nonnull String[] commandLine) {
        return toEscapedCommandLine(asImmutableList(commandLine));
    }

    @Nonnull
    public static String toEscapedCommandLine(@Nonnull Iterable<String> commandLine) {
        final StringBuilder sb = new StringBuilder();
        for (String argument : commandLine) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            if (ESCAPED_ARGUMENT_PATTERN.matcher(argument).matches()) {
                sb.append('"').append(ESCAPED_ARGUMENT_PATTERN.matcher(argument).replaceAll("\\$1")).append('"');
            } else {
                sb.append(argument);
            }
        }
        return sb.toString();
    }

    private ProcessUtils() {}


}

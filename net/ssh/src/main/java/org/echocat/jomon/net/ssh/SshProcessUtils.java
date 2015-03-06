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

package org.echocat.jomon.net.ssh;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static org.echocat.jomon.runtime.CollectionUtils.addAll;
import static org.echocat.jomon.runtime.CollectionUtils.asImmutableList;

public class SshProcessUtils {

    @Nonnull
    public static List<String> getCompleteCommandLine(@Nonnull String executable, @Nullable List<String> arguments) {
        final List<String> command = new ArrayList<>();
        command.add(executable);
        addAll(command, arguments);
        return asImmutableList(command);
    }

    @Nonnull
    public static String getCompleteCommandLineArguments(@Nullable Iterable<String> commandLine) {
        final StringBuilder sb = new StringBuilder();
        for (final String arg :commandLine) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            if (arg.isEmpty()) {
                sb.append("\"\"");
            } if (arg.contains(" ") || arg.contains("\\") || arg.contains("\"")) {
                sb.append('"').append(arg.replace("\\", "\\\\").replace("\"", "\\\"")).append('"');
            } else {
                sb.append(arg);
            }
        }
        return sb.toString();
    }
    @Nonnull
    public static String getCompleteCommandLineArguments(@Nonnull String executable, @Nullable List<String> arguments) {
        return getCompleteCommandLineArguments(getCompleteCommandLine(executable, arguments));
    }


    private SshProcessUtils() {}

}

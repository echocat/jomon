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

package org.echocat.jomon.process.execution;

import org.echocat.jomon.runtime.generation.Requirement;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

public interface ProcessExecuter<R extends Requirement> {

    @Nonnull
    public Response execute(@Nonnull R requirement) throws InterruptedException, IOException;

    @Nonnull
    public Response execute(@Nonnull R requirement, @Nullable Drains drains) throws InterruptedException, IOException;

    public static class Response {

        @Nonnull
        private final String _stdout;
        @Nonnull
        private final String _stderr;
        @Nonnegative
        private final int _exitCode;

        public Response(@Nonnull String stdout, @Nonnull String stderr, @Nonnegative int exitCode) {
            _stdout = stdout;
            _stderr = stderr;
            _exitCode = exitCode;
        }

        @Nonnull
        public String getStdout() {
            return _stdout;
        }

        @Nonnull
        public String getStderr() {
            return _stderr;
        }

        @Nonnegative
        public int getExitCode() {
            return _exitCode;
        }

        @Override
        public String toString() {
            final String stderr = getStderr().trim();
            return "ExitCode: " + _exitCode + "\n\n====Stdout====\n\n" + getStdout() + (stderr.isEmpty() ? "" : "\n\n====Stderr====\n\n" + stderr);
        }

        public boolean wasSuccessful() {
            return getExitCode() == 0;
        }

        public boolean hasErrorOutput() {
            return !getStderr().trim().isEmpty();
        }

        public boolean isStdoutMatching(@Nonnull Pattern pattern) {
            return pattern.matcher(getStdout()).matches();
        }

        public boolean isStdoutMatching(@Nonnull String pattern) {
            return isStdoutMatching(compile(pattern));
        }

        public boolean isStderrMatching(@Nonnull Pattern pattern) {
            return pattern.matcher(getStderr()).matches();
        }

        public boolean isStderrMatching(@Nonnull String pattern) {
            return isStderrMatching(compile(pattern));
        }

    }

}

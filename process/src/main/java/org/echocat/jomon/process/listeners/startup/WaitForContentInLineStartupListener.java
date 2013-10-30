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

package org.echocat.jomon.process.listeners.startup;

import org.echocat.jomon.process.GeneratedProcess;
import org.echocat.jomon.runtime.io.StreamType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.compile;

public class WaitForContentInLineStartupListener<P extends GeneratedProcess<?, ?>> extends LineBasedStartupListenerSupport<P, WaitForContentInLineStartupListener<P>> {

    @Nonnull
    public static <P extends GeneratedProcess<?, ?>> WaitForContentInLineStartupListener<P> waitForContentInLine(@Nonnull Pattern successLinePattern) {
        return new WaitForContentInLineStartupListener<>(successLinePattern);
    }

    @Nonnull
    public static <P extends GeneratedProcess<?, ?>> WaitForContentInLineStartupListener<P> waitForContentInLine(@Nonnull String successLinePattern) {
        return waitForContentInLine(compile(successLinePattern, DOTALL));
    }

    @Nonnull
    private final Pattern _successLinePattern;

    @Nullable
    private Pattern _failLinePattern;

    public WaitForContentInLineStartupListener(@Nonnull Pattern successLinePattern) {
        _successLinePattern = successLinePattern;
    }

    @Nonnull
    public WaitForContentInLineStartupListener<P> whichFailsOnLineWith(@Nonnull Pattern pattern) {
        _failLinePattern = pattern;
        return thisObject();
    }

    @Nonnull
    public WaitForContentInLineStartupListener<P> whichFailsOnLineWith(@Nonnull String pattern) {
        return whichFailsOnLineWith(compile(pattern));
    }

    @Nonnull
    public Pattern getSuccessLinePattern() {
        return _successLinePattern;
    }

    @Nullable
    public Pattern getFailLinePattern() {
        return _failLinePattern;
    }

    public void setFailLinePattern(Pattern failLinePattern) {
        _failLinePattern = failLinePattern;
    }

    @Override
    protected void notifyOutputWhileStartup(@Nonnull P process, @Nonnull String line, @SuppressWarnings("UnusedParameters") @Nonnull StreamType streamType) {
        if (_successLinePattern.matcher(line).matches()) {
            notifyProcessStartupDone(process, true);
        } else {
            final Pattern failLinePattern = _failLinePattern;
            if (failLinePattern != null && failLinePattern.matcher(line).matches()) {
                notifyProcessStartupDone(process, false);
            }
        }
    }

}

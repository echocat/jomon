package org.echocat.jomon.process.daemon;

import org.echocat.jomon.process.GeneratedProcess;
import org.echocat.jomon.process.daemon.listeners.startup.StartupListenerSupport;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

public class WaitForContentInLineStartupListener extends StartupListenerSupport<WaitForContentInLineStartupListener> {

    @Nonnull
    public static WaitForContentInLineStartupListener waitForContentInLine(@Nonnull Pattern successLinePattern) {
        return new WaitForContentInLineStartupListener(successLinePattern);
    }

    @Nonnull
    public static WaitForContentInLineStartupListener waitForContentInLine(@Nonnull String successLinePattern) {
        return waitForContentInLine(compile(successLinePattern));
    }

    @Nonnull
    private final Pattern _successLinePattern;

    @Nullable
    private Pattern _failLinePattern;

    public WaitForContentInLineStartupListener(@Nonnull Pattern successLinePattern) {
        _successLinePattern = successLinePattern;
    }

    @Nonnull
    public WaitForContentInLineStartupListener whichFailsOnLineWith(@Nonnull Pattern pattern) {
        _failLinePattern = pattern;
        return thisListener();
    }

    @Nonnull
    public WaitForContentInLineStartupListener whichFailsOnLineWith(@Nonnull String pattern) {
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
    protected void notifyLineOutputWhileStartup(@Nonnull GeneratedProcess process, @Nonnull String line, @Nonnull StreamType streamType) {
        if (_successLinePattern.matcher(line).matches()) {
            notifyStartupDone(process, true);
        } else {
            final Pattern failLinePattern = _failLinePattern;
            if (failLinePattern != null && failLinePattern.matcher(line).matches()) {
                notifyStartupDone(process, false);
            }
        }
    }

}

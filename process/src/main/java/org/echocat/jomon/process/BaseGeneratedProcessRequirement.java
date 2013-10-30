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

import org.echocat.jomon.runtime.generation.Requirement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static org.echocat.jomon.process.ProcessUtils.toEscapedCommandLine;

public abstract class BaseGeneratedProcessRequirement<E, T extends BaseGeneratedProcessRequirement<E, T>> implements Requirement {

    @Nonnull
    private final Map<String, String> _environment = new HashMap<>();
    @Nonnull
    private final List<String> _arguments = new ArrayList<>();

    @Nonnull
    private final E _executable;

    @Nullable
    private E _workingDirectory;

    private boolean _daemon;

    public BaseGeneratedProcessRequirement(@Nonnull E executable) {
        _executable = executable;
    }

    @Nonnull
    public T withArgument(@Nonnull String argument) {
        return withArguments(argument);
    }

    @Nonnull
    public T withArguments(@Nonnull String... arguments) {
        return withArguments(asList(arguments));
    }

    @Nonnull
    public T withArguments(@Nonnull Iterable<String> arguments) {
        for (String argument : arguments) {
            _arguments.add(argument);
        }
        return thisObject();
    }

    @Nonnull
    public T withWorkingDirectory(@Nonnull E workingDirectory) {
        _workingDirectory = workingDirectory;
        return thisObject();
    }

    @Nonnull
    public T withEnvironment(@Nonnull String key, @Nonnull String value) {
        _environment.put(key, value);
        return thisObject();
    }

    @Nonnull
    public T withEnvironment(@Nonnull Map<String, String> environment) {
        _environment.putAll(environment);
        return thisObject();
    }

    @Nonnull
    public T whichIsDaemon() {
        return whichIsDaemon(true);
    }

    @Nonnull
    public T whichIsNoDaemon() {
        return whichIsDaemon(false);
    }

    @Nonnull
    public T whichIsDaemon(boolean daemon) {
        _daemon = daemon;
        return thisObject();
    }

    @Nonnull
    public Map<String, String> getEnvironment() {
        return unmodifiableMap(_environment);
    }

    @Nonnull
    public List<String> getArguments() {
        return unmodifiableList(_arguments);
    }

    @Nonnull
    public E getExecutable() {
        return _executable;
    }

    @Nullable
    public E getWorkingDirectory() {
        return _workingDirectory;
    }

    public boolean isDaemon() {
        return _daemon;
    }

    @Nonnull
    public List<String> getCompleteCommandLine() {
        final List<String> command = new ArrayList<>();
        command.add(getExecutable().toString());
        command.addAll(getArguments());
        return unmodifiableList(command);
    }

    @Nonnull
    public String getCompleteCommandLineAsString() {
        return toEscapedCommandLine(getCompleteCommandLine());
    }

    @Nonnull
    protected T thisObject() {
        //noinspection unchecked
        return (T) this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Execute ").append(isDaemon() ? "daemon" : "process").append(' ');
        final E workingDirectory = getWorkingDirectory();
        if (workingDirectory != null) {
            sb.append("in '").append(workingDirectory).append("' ");
        }
        sb.append(getCompleteCommandLineAsString());
        sb.append(" with environment ").append(getEnvironment());
        return sb.toString();
    }

}

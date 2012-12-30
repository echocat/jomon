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

package org.echocat.jomon.process;

import org.echocat.jomon.runtime.generation.Requirement;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.getProperty;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

public class GeneratedProcessRequirement implements Requirement {

    @Nonnull
    public static GeneratedProcessRequirement process(@Nonnull File executable) {
        return new GeneratedProcessRequirement(executable);
    }

    @Nonnull
    public static GeneratedProcessRequirement process(@Nonnull String executable) {
        return new GeneratedProcessRequirement(executable);
    }

    private final Map<String, String> _environment = new HashMap<>();
    private final List<String> _arguments = new ArrayList<>();

    private final String _executable;

    private File _workingDirectory = new File(getProperty("user.dir", "."));

    private boolean _daemon;

    public GeneratedProcessRequirement(@Nonnull File executable) {
        this(executable.getPath());
    }

    public GeneratedProcessRequirement(@Nonnull String executable) {
        _executable = executable;
    }

    @Nonnull
    public GeneratedProcessRequirement withArgument(@Nonnull String argument) {
        return withArguments(argument);
    }

    @Nonnull
    public GeneratedProcessRequirement withArguments(@Nonnull String... arguments) {
        return withArguments(asList(arguments));
    }

    @Nonnull
    public GeneratedProcessRequirement withArguments(@Nonnull Iterable<String> arguments) {
        for (String argument : arguments) {
            _arguments.add(argument);
        }
        return this;
    }

    @Nonnull
    public GeneratedProcessRequirement withWorkingDirectory(@Nonnull File workingDirectory) {
        if (workingDirectory.isDirectory()) {
            throw new IllegalArgumentException(workingDirectory + " does not exist.");
        }
        _workingDirectory = workingDirectory;
        return this;
    }

    @Nonnull
    public GeneratedProcessRequirement withWorkingDirectory(@Nonnull String workingDirectory) {
        return withWorkingDirectory(new File(workingDirectory));
    }

    @Nonnull
    public GeneratedProcessRequirement withEnvironment(@Nonnull String key, @Nonnull String value) {
        _environment.put(key, value);
        return this;
    }

    @Nonnull
    public GeneratedProcessRequirement withEnvironment(@Nonnull Map<String, String> environment) {
        _environment.putAll(environment);
        return this;
    }

    @Nonnull
    public GeneratedProcessRequirement whichIsDaemon() {
        return whichIsDaemon(true);
    }

    @Nonnull
    public GeneratedProcessRequirement whichIsNoDaemon() {
        return whichIsDaemon(false);
    }

    @Nonnull
    public GeneratedProcessRequirement whichIsDaemon(boolean daemon) {
        _daemon = daemon;
        return this;
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
    public String getExecutable() {
        return _executable;
    }

    @Nonnull
    public File getWorkingDirectory() {
        return _workingDirectory;
    }

    public boolean isDaemon() {
        return _daemon;
    }

    @Nonnull
    public List<String> getCompleteCommandLine() {
        final List<String> command = new ArrayList<>();
        command.add(_executable);
        command.addAll(_arguments);
        return unmodifiableList(command);
    }
}

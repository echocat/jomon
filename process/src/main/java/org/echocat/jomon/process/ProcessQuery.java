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

import com.google.common.base.Predicate;
import org.echocat.jomon.runtime.repository.Query;
import org.echocat.jomon.runtime.util.Glob;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.lang.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.ArrayUtils.toObject;
import static org.apache.commons.lang3.StringUtils.join;

public class ProcessQuery implements Query, Predicate<Process> {

    @Nonnull
    public static ProcessQuery query() {
        return new ProcessQuery();
    }

    private List<Long> _ids;
    private File _executable;
    private Glob _commandLineGlob;

    @Nonnull
    public ProcessQuery withId(long... id) {
        return withIds(id);
    }

    @Nonnull
    public ProcessQuery withIds(@Nonnull long... ids) {
        return withIds(toObject(ids));
    }

    @Nonnull
    public ProcessQuery withIds(@Nonnull Long... ids) {
        return withIds(asList(ids));
    }

    @Nonnull
    public ProcessQuery withIds(@Nonnull Iterable<Long> ids) {
        final List<Long> idsAsList = new ArrayList<>();
        for (Long id : ids) {
            idsAsList.add(id);
        }
        return withIds(idsAsList);
    }

    @Nonnull
    public ProcessQuery withIds(@Nonnull List<Long> ids) {
        if (_ids != null) {
            throw new IllegalStateException("Ids already set.");
        }
        _ids = ids;
        return this;
    }

    @Nonnull
    public ProcessQuery withExecutable(@Nonnull File executable) {
        if (_executable != null) {
            throw new IllegalStateException("Executable already set.");
        }
        _executable = executable;
        return this;
    }

    @Nonnull
    public ProcessQuery withCommandLineLike(@Nonnull String commandLineGlobPattern) {
        try {
            return withCommandLineLike(new Glob(commandLineGlobPattern));
        } catch (ParseException e) {
            throw new IllegalArgumentException("Illegal pattern: " + commandLineGlobPattern, e);
        }
    }

    @Nonnull
    public ProcessQuery withCommandLineLike(@Nonnull Glob commandLine) {
        if (_commandLineGlob != null) {
            throw new IllegalStateException("Executable already set.");
        }
        _commandLineGlob = commandLine;
        return this;
    }

    @Nullable
    public List<Long> getIds() {
        return _ids;
    }

    @Nullable
    public File getExecutable() {
        return _executable;
    }

    @Nullable
    public Glob getCommandLineGlob() {
        return _commandLineGlob;
    }

    @Override
    public boolean apply(@Nullable Process input) {
        return input != null
            && applyId(input)
            && applyExecutable(input)
            && applyCommandLine(input)
            ;
    }

    protected boolean applyId(@Nonnull Process process) {
        return _ids == null || _ids.contains(process.getId());
    }

    protected boolean applyExecutable(@Nonnull Process process) {
        return _executable == null || _executable.equals(process.getExecutable());
    }

    protected boolean applyCommandLine(@Nonnull Process process) {
        final boolean result;
        if (_commandLineGlob == null) {
            result = true;
        } else {
            final String[] commandLine = process.getCommandLine();
            if (commandLine != null) {
                final String joinedCommandLine = join(commandLine, ' ');
                result = _commandLineGlob.matches(joinedCommandLine);
            } else {
                result = false;
            }
        }
        return result;
    }
}

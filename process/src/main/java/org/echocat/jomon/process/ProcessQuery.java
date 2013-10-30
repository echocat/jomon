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

import com.google.common.base.Predicate;
import org.echocat.jomon.runtime.repository.Query;
import org.echocat.jomon.runtime.util.Glob;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.join;

public abstract class ProcessQuery<E, ID, T extends ProcessQuery<E, ID, T>> implements Query, Predicate<Process<E, ID>> {

    private List<ID> _ids;
    private E _executable;
    private Glob _commandLineGlob;

    @Nonnull
    public T withId(@Nonnull ID id) {
        return withIds(id);
    }

    @Nonnull
    public T withIds(@Nonnull ID... ids) {
        return withIds(asList(ids));
    }

    @Nonnull
    public T withIds(@Nonnull Iterable<ID> ids) {
        final List<ID> idsAsList = new ArrayList<>();
        for (ID id : ids) {
            idsAsList.add(id);
        }
        return withIds(idsAsList);
    }

    @Nonnull
    public T withIds(@Nonnull List<ID> ids) {
        if (_ids != null) {
            throw new IllegalStateException("Ids already set.");
        }
        _ids = ids;
        return thisObject();
    }

    @Nonnull
    public T withExecutable(@Nonnull E executable) {
        if (_executable != null) {
            throw new IllegalStateException("Executable already set.");
        }
        _executable = executable;
        return thisObject();
    }

    @Nonnull
    public T withCommandLineLike(@Nonnull String commandLineGlobPattern) {
        try {
            return withCommandLineLike(new Glob(commandLineGlobPattern));
        } catch (ParseException e) {
            throw new IllegalArgumentException("Illegal pattern: " + commandLineGlobPattern, e);
        }
    }

    @Nonnull
    public T withCommandLineLike(@Nonnull Glob commandLine) {
        if (_commandLineGlob != null) {
            throw new IllegalStateException("Executable already set.");
        }
        _commandLineGlob = commandLine;
        return thisObject();
    }

    @Nullable
    public List<ID> getIds() {
        return _ids;
    }

    @Nullable
    public E getExecutable() {
        return _executable;
    }

    @Nullable
    public Glob getCommandLineGlob() {
        return _commandLineGlob;
    }

    @Override
    public boolean apply(@Nullable Process<E, ID> input) {
        return input != null
            && applyId(input)
            && applyExecutable(input)
            && applyCommandLine(input)
            ;
    }

    protected boolean applyId(@Nonnull Process<E, ID> process) {
        return _ids == null || _ids.contains(process.getId());
    }

    protected boolean applyExecutable(@Nonnull Process<E, ID> process) {
        return _executable == null || _executable.equals(process.getExecutable());
    }

    protected boolean applyCommandLine(@Nonnull Process<E, ID> process) {
        final boolean result;
        if (_commandLineGlob == null) {
            result = true;
        } else {
            final List<String> arguments = process.getArguments();
            if (arguments != null) {
                final String joinedCommandLine = join(arguments, ' ');
                result = _commandLineGlob.matches(joinedCommandLine);
            } else {
                result = false;
            }
        }
        return result;
    }

    @Nonnull
    protected T thisObject() {
        // noinspection unchecked
        return (T) this;
    }
}

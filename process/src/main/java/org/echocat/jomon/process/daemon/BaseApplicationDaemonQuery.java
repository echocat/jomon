package org.echocat.jomon.process.daemon;

import com.google.common.base.Predicate;
import org.echocat.jomon.runtime.repository.Query;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Iterator;
import java.util.List;

import static org.echocat.jomon.runtime.CollectionUtils.asImmutableList;
import static org.echocat.jomon.runtime.CollectionUtils.asIterator;

public abstract class BaseApplicationDaemonQuery<Q extends BaseApplicationDaemonQuery<Q, T>, T extends ApplicationDaemon<?>> implements Query, Predicate<T> {

    @Nullable
    private Long _pid;
    @Nullable
    private File _executable;
    @Nullable
    private Class<? extends T> _type;
    @Nullable
    private Boolean _isAlive;
    @Nullable
    private List<String> _firstArguments;

    @Nonnull
    public Q withPid(long pid) {
        if (_pid != null) {
            throw new IllegalStateException("pid was already defined.");
        }
        _pid = pid;
        return thisQuery();
    }

    @Nonnull
    public Q withExecutable(@Nonnull File executable) {
        if (_executable != null) {
            throw new IllegalStateException("executable was already defined.");
        }
        _executable = executable;
        return thisQuery();
    }

    @Nonnull
    public Q whichIsOfType(@Nonnull Class<? extends T> type) {
        if (_type != null) {
            throw new IllegalStateException("executable was already defined.");
        }
        _type = type;
        return thisQuery();
    }

    @Nonnull
    public Q whichIsAlive() {
        return whichIsAlive(true);
    }

    @Nonnull
    public Q whichIsNotAlive() {
        return whichIsAlive(false);
    }

    @Nonnull
    public Q whichIsAlive(boolean alive) {
        if (_isAlive != null) {
            throw new IllegalStateException("isAlive was already defined.");
        }
        _isAlive = alive;
        return thisQuery();
    }

    @Nonnull
    public Q whichStartsWithArguments(@Nullable Iterable<String> firstArguments) {
        if (_firstArguments != null) {
            throw new IllegalStateException("firstArguments was already defined.");
        }
        _firstArguments = asImmutableList(firstArguments);
        return thisQuery();
    }

    @Nonnull
    public Q whichStartsWithArguments(@Nullable String... firstArguments) {
        if (_firstArguments != null) {
            throw new IllegalStateException("firstArguments was already defined.");
        }
        _firstArguments = asImmutableList(firstArguments);
        return thisQuery();
    }

    @Nonnull
    public Q whichStartsWithArgument(@Nonnull String firstArgument) {
        return whichStartsWithArguments(firstArgument);
    }

    @Nullable
    public Long getPid() {
        return _pid;
    }

    @Nullable
    public File getExecutable() {
        return _executable;
    }

    @Nullable
    public Class<? extends T> getType() {
        return _type;
    }

    @Nullable
    public Boolean getAlive() {
        return _isAlive;
    }

    @Nullable
    public List<String> getFirstArguments() {
        return _firstArguments;
    }

    @Override
    public boolean apply(@Nullable T daemon) {
        return daemon != null
            && applyPid(daemon)
            && applyExecutable(daemon)
            && applyType(daemon)
            && applyIsAlive(daemon)
            && applyFirstArguments(daemon)
            ;
    }

    protected boolean applyPid(@Nonnull T daemon) {
        return _pid == null || _pid.equals(daemon.getPid());
    }

    protected boolean applyExecutable(@Nonnull T daemon) {
        return _executable == null || _executable.equals(daemon.getExecutable());
    }

    protected boolean applyType(@Nonnull T daemon) {
        return _type == null || _type.isInstance(daemon);
    }

    protected boolean applyIsAlive(@Nonnull T daemon) {
        return _isAlive == null || _isAlive.equals(daemon.isAlive());
    }

    protected boolean applyFirstArguments(@Nonnull T daemon) {
        boolean result;
        final List<String> firstArguments = _firstArguments;
        if (firstArguments == null || firstArguments.isEmpty()) {
            result = true;
        } else {
            final String[] commandLine = daemon.getCommandLine();
            if (firstArguments.size() <= (commandLine.length + 1)) {
                final Iterator<String> ei = firstArguments.iterator();
                final Iterator<String> et = asIterator(commandLine);
                if (et.hasNext()) {
                    result = true;
                    et.next(); // Skip command
                    while (result && ei.hasNext() && et.hasNext()) {
                        result = ei.next().equals(et.next());
                    }
                    if (result && !et.hasNext()) {
                        result = !ei.hasNext();
                    }
                } else {
                    result = false;
                }
            } else {
                result = false;
            }
        }
        return result;
    }

    @Nonnull
    protected Q thisQuery() {
        // noinspection unchecked
        return (Q) this;
    }

}

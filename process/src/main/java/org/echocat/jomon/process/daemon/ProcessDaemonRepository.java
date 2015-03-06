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

package org.echocat.jomon.process.daemon;

import com.google.common.base.Predicate;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.echocat.jomon.process.CouldNotStartException;
import org.echocat.jomon.process.GeneratedProcess;
import org.echocat.jomon.runtime.CollectionUtils;
import org.echocat.jomon.runtime.generation.Generator;
import org.echocat.jomon.runtime.iterators.CloseableIterator;
import org.echocat.jomon.runtime.repository.QueryableRepository;
import org.echocat.jomon.runtime.repository.RemovingRepository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.sort;
import static org.echocat.jomon.runtime.CollectionUtils.*;
import static org.echocat.jomon.runtime.iterators.IteratorUtils.filter;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

@ThreadSafe
public class ProcessDaemonRepository<
    E,
    ID,
    P extends GeneratedProcess<E, ID>,
    D extends ProcessDaemon<E, ID, P, ?, ?>,
    R extends ProcessDaemonRequirement<E, ID, P, ? extends D>,
    Q extends BaseProcessDaemonQuery<E, ID, ?, ? extends D>,
    G extends Generator<P, ?>
> implements QueryableRepository<Q, ID, D>, Iterable<D>, RemovingRepository<Q, ID>, Generator<D, R>, AutoCloseable {

    @Nonnull
    private static final Comparator<Constructor<?>> BY_PARAMETER_COUNT = new Comparator<Constructor<?>>() { @Override public int compare(Constructor<?> o1, Constructor<?> o2) {
        return Integer.compare(o2.getParameterTypes().length, o1.getParameterTypes().length);
    }};

    @Nonnull
    private final Thread _shutdownHook = new Thread("ShutdownProcessDaemons") { @Override public void run() {
        close();
    }};
    @Nonnull
    private final Map<ID, D> _idToProcess = new ConcurrentHashMap<>();
    @Nonnull
    private final G _processGenerator;

    public ProcessDaemonRepository(@Nonnull G processGenerator) {
        _processGenerator = processGenerator;
        Runtime.getRuntime().addShutdownHook(_shutdownHook);
    }

    @Nonnull
    @Override
    public D generate(@Nonnull R requirement) {
        final D daemon = createInstanceFor(requirement);
        _idToProcess.put(daemon.getProcess().getId(), daemon);
        return daemon;
    }

    @Nullable
    @Override
    public D findOneBy(@Nonnull ID id) {
        return _idToProcess.get(id);
    }

    @Nullable
    @Override
    public D findOneBy(@Nonnull Q query) {
        return findFirstOf(findBy(query));
    }

    @Nonnull
    @Override
    public CloseableIterator<D> findBy(@Nonnull Q query) {
        //noinspection unchecked
        return filter(iterator(), (Predicate<? super D>) query);
    }

    @Override
    public long countBy(@Nonnull Q query) {
        // noinspection unchecked
        return countElementsOf(iterator(), (Predicate<D>) query);
    }

    @Override
    public boolean removeBy(@Nonnull ID id) {
        final D daemon = _idToProcess.remove(id);
        if (daemon != null) {
            closeQuietly(daemon);
        }
        return daemon != null;
    }

    @Override
    public long removeBy(@Nonnull Q query) {
        long removed = 0;
        try (final CloseableIterator<D> i = findBy(query)) {
            while (i.hasNext()) {
                final D daemon = i.next();
                final ID id = daemon.getProcess().getId();
                if (_idToProcess.remove(id) != null) {
                    closeQuietly(daemon);
                    removed++;
                }
            }
        }
        return removed;
    }

    @Nonnull
    protected Collection<D> getCopySafe() {
        synchronized (_idToProcess) {
            return new HashSet<>(_idToProcess.values());
        }
    }

    @Override
    public Iterator<D> iterator() {
        return getCopySafe().iterator();
    }

    @Nonnull
    protected Pair<Constructor<D>, Object[]> getConstructorFor(@Nonnull R requirement) {
        return getConstructorFor(requirement.getType(), requirement);
    }

    @Nonnull
    protected Pair<Constructor<D>, Object[]> getConstructorFor(@Nonnull Class<? extends D> type, @Nonnull R requirement) {
        final List<Object> potentialParameters = getPotentialParametersFor(requirement);
        Pair<Constructor<D>, Object[]> result = null;
        for (final Constructor<D> potentialConstructor : getPotentialConstructorsSortedFor(type)) {
            final Object[] parameters = findRightParametersFor(potentialConstructor, potentialParameters);
            if (parameters != null) {
                result = new ImmutablePair<>(potentialConstructor, parameters);
            }
        }
        if (result == null) {
            throw new IllegalArgumentException("Could not find a valid constructor for daemon " + type.getName() + ".");
        }
        return result;
    }

    @Nullable
    protected Object[] findRightParametersFor(@Nonnull Constructor<D> potentialConstructor, @Nonnull List<Object> potentialParameters) {
        final Class<?>[] types = potentialConstructor.getParameterTypes();
        final Object[] parameters = new Object[types.length];
        boolean allFound = true;
        for (int i = 0; i < types.length; i++) {
            final Class<?> expectedType = types[i];
            parameters[i] = findRightParameterFor(expectedType, potentialParameters);
            if (parameters[i] == null) {
                allFound = false;
                break;
            }

        }
        return allFound ? parameters : null;
    }

    @Nullable
    protected Object findRightParameterFor(@Nonnull Class<?> expectedType, @Nonnull List<Object> potentialParameters) {
        Object result = null;
        for (final Object potentialParameter : potentialParameters) {
            if (expectedType.isInstance(potentialParameter)) {
                result = potentialParameter;
                break;
            }
        }
        return result;
    }

    @Nonnull
    protected List<Constructor<D>> getPotentialConstructorsSortedFor(@Nonnull Class<? extends D> type) {
        // noinspection unchecked
        final List<Constructor<D>> potentialConstructors = asList((Constructor<D>[])type.getConstructors());
        sort(potentialConstructors, BY_PARAMETER_COUNT);
        return potentialConstructors;
    }

    @Nonnull
    protected List<Object> getPotentialParametersFor(@Nonnull R requiremnt) {
        return asImmutableList(requiremnt, _processGenerator, this);
    }

    @Nonnull
    protected D createInstanceFor(@Nonnull R requirement) {
        return createInstanceFor(getConstructorFor(requirement), requirement);
    }

    @Nonnull
    protected  D createInstanceFor(@Nonnull Pair<Constructor<D>, Object[]> constructorAndParameters, @Nonnull R requirement) {
        final D result;
        try {
            result = constructorAndParameters.getLeft().newInstance(constructorAndParameters.getRight());
        } catch (final Exception e) {
            final Throwable target;
            // noinspection InstanceofCatchParameter
            if (e instanceof InvocationTargetException) {
                final Throwable cause = e.getCause();
                target = cause != null ? cause : e;
            } else {
                target = e;
            }
            if (target instanceof RuntimeException) {
                //noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
                throw (RuntimeException)target;
            } else if (target instanceof Error) {
                //noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
                throw (Error)target;
            } else {
                throw new CouldNotStartException("Could not start daemon " + requirement.getType().getName() + ".", e);
            }
        }
        return result;
    }
    
    @Nonnull
    protected Iterator<Class<?>> getTypesIteratorFor(@Nonnull R requirement) {
        return CollectionUtils.<Class<?>>asSingletonIterator(requirement.getClass());
    }

    @Override
    public void close() {
        try {
            synchronized (_idToProcess) {
                final Iterator<D> i = _idToProcess.values().iterator();
                while (i.hasNext()) {
                    final D daemon = i.next();
                    closeQuietly(daemon);
                    i.remove();
                }
            }
        } finally {
            try {
                Runtime.getRuntime().removeShutdownHook(_shutdownHook);
            } catch (final IllegalStateException ignored) {}
        }
    }

}

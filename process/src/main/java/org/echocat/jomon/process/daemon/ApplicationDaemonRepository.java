package org.echocat.jomon.process.daemon;

import com.google.common.base.Predicate;
import org.echocat.jomon.process.ProcessRepository;
import org.echocat.jomon.runtime.CollectionUtils;
import org.echocat.jomon.runtime.generation.Generator;
import org.echocat.jomon.runtime.iterators.CloseableIterator;
import org.echocat.jomon.runtime.repository.InsertingRepository;
import org.echocat.jomon.runtime.repository.QueryableRepository;
import org.echocat.jomon.runtime.repository.RemovingRepository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.echocat.jomon.process.ProcessRepository.processRepository;
import static org.echocat.jomon.process.daemon.ApplicationDaemonQuery.applicationDaemon;
import static org.echocat.jomon.runtime.CollectionUtils.countElementsOf;
import static org.echocat.jomon.runtime.CollectionUtils.findFirstOf;
import static org.echocat.jomon.runtime.iterators.IteratorUtils.filter;
import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;

@ThreadSafe
public class ApplicationDaemonRepository implements QueryableRepository<BaseApplicationDaemonQuery<?, ?>, Long, ApplicationDaemon<?>>, Iterable<ApplicationDaemon<?>>, RemovingRepository<BaseApplicationDaemonQuery<?, ?>, Long>, InsertingRepository<ApplicationDaemon<?>>, Generator<ApplicationDaemon<?>, ApplicationDaemonRequirement<?>>, AutoCloseable {

    private static final ApplicationDaemonRepository INSTANCE = new ApplicationDaemonRepository();

    private final Thread _shutdownHook = new Thread("ShutdownApplicationDaemons") { @Override public void run() {
        close();
    }};

    @Nonnull
    public static ApplicationDaemonRepository getInstance() {
        return INSTANCE;
    }

    @Nonnull
    public static ApplicationDaemonRepository applicationDaemonRepository() {
        return getInstance();
    }

    private final Map<Long, ApplicationDaemon<?>> _idToProcess = new ConcurrentHashMap<>();
    private final ProcessRepository _processRepository;

    public ApplicationDaemonRepository() {
        this(processRepository());
    }

    public ApplicationDaemonRepository(@Nonnull ProcessRepository processRepository) {
        _processRepository = processRepository;
        Runtime.getRuntime().addShutdownHook(_shutdownHook);
    }

    @Nonnull
    @Override
    public ApplicationDaemon<?> generate(@Nonnull ApplicationDaemonRequirement<?> requirement) {
        return generateDirect((ApplicationDaemonRequirement)requirement);
    }
    
    @Nonnull
    public <R extends ApplicationDaemonRequirement<T>, T extends ApplicationDaemon<R>> T generateDirect(@Nonnull R requirement) {
        final T applicationDaemon = createInstanceFor(requirement);
        _idToProcess.put(applicationDaemon.getPid(), applicationDaemon);
        return applicationDaemon;
    }

    @Override
    public void insert(@Nonnull ApplicationDaemon<?> applicationDaemon) {
        _idToProcess.put(applicationDaemon.getPid(), applicationDaemon);
    }

    @Nullable
    @Override
    public ApplicationDaemon<?> findOneBy(@Nonnull Long pid) {
        return _idToProcess.get(pid);
    }

    @Nullable
    @Override
    public ApplicationDaemon<?> findOneBy(@Nonnull BaseApplicationDaemonQuery<?, ?> query) {
        return findFirstOf(findBy(query));
    }

    @Nonnull
    @Override
    public CloseableIterator<ApplicationDaemon<?>> findBy(@Nonnull BaseApplicationDaemonQuery<?, ?> query) {
        //noinspection unchecked
        return filter(iterator(), (Predicate<? super ApplicationDaemon<?>>) query);
    }

    @Override
    public long countBy(@Nonnull BaseApplicationDaemonQuery<?, ?> query) {
        //noinspection unchecked
        return countElementsOf(iterator(), (Predicate<ApplicationDaemon<?>>) query);
    }

    @Override
    public boolean removeBy(@Nonnull Long pid) {
        final ApplicationDaemon<?> applicationDaemon = _idToProcess.remove(pid);
        if (applicationDaemon != null) {
            closeQuietly(applicationDaemon);
        }
        return applicationDaemon != null;
    }

    @Override
    public long removeBy(@Nonnull BaseApplicationDaemonQuery<?, ?> query) {
        long removed = 0;
        try (final CloseableIterator<ApplicationDaemon<?>> i = findBy(query)) {
            while (i.hasNext()) {
                final ApplicationDaemon<?> applicationDaemon = i.next();
                final long pid = applicationDaemon.getPid();
                if (_idToProcess.remove(pid) != null) {
                    closeQuietly(applicationDaemon);
                    removed++;
                }
            }
        }
        return removed;
    }

    @Nonnull
    protected Collection<ApplicationDaemon<?>> getCopySafe() {
        synchronized (_idToProcess) {
            return new HashSet<>(_idToProcess.values());
        }
    }

    @Override
    public Iterator<ApplicationDaemon<?>> iterator() {
        return getCopySafe().iterator();
    }

    @Nonnull
    protected <R extends ApplicationDaemonRequirement<T>, T extends ApplicationDaemon<R>> Constructor<T> getConstructorFor(@Nonnull R requirement) {
        return getConstructorFor(requirement.getType(), requirement);
    }

    @Nonnull
    protected <R extends ApplicationDaemonRequirement<T>, T extends ApplicationDaemon<R>> Constructor<T> getConstructorFor(@Nonnull Class<T> type, @Nonnull R requirement) {
        Constructor<T> constructor = null;
        final Iterator<Class<?>> i = getTypesIteratorFor(requirement);
        while (i.hasNext()) {
            final Class<?> requirementType = i.next();
            try {
                constructor = type.getConstructor(ProcessRepository.class, requirementType);
            } catch (NoSuchMethodException ignored) {}
        }
        if (constructor == null) {
            try {
                constructor = type.getConstructor(ProcessRepository.class);
            } catch (NoSuchMethodException ignored) {}
        }
        if (constructor == null) {
            throw new IllegalArgumentException("Could not create application daemon for " + type.getName() + " because could not find a valid constructor like:"
                + "\n\t" + type.getName() + "(" + ProcessRepository.class.getName() + ", " + requirement.getType().getName() + ")"
                + "\n\t" + type.getName() + "(" + ProcessRepository.class.getName() + ")"
            );
        }
        return constructor;
    }

    @Nonnull
    protected <R extends ApplicationDaemonRequirement<T>, T extends ApplicationDaemon<R>> T createInstanceFor(@Nonnull R requirement) {
        return createInstanceFor(getConstructorFor(requirement), requirement);
    }

    @Nonnull
    protected <R extends ApplicationDaemonRequirement<T>, T extends ApplicationDaemon<R>> T createInstanceFor(@Nonnull Constructor<T> constructor, @Nonnull R requirement) {
        final T result;
        try {
            if (constructor.getParameterTypes().length == 1) {
                result = constructor.newInstance(_processRepository);
            } else {
                result = constructor.newInstance(_processRepository, requirement);
            }
        } catch (Exception e) {
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
                throw new CouldNotStartProcessException("Could not start application daemon " + requirement.getType().getName() + ".", e);
            }
        }
        return result;
    }
    
    @Nonnull
    protected Iterator<Class<?>> getTypesIteratorFor(@Nonnull ApplicationDaemonRequirement<?> requirement) {
        return CollectionUtils.<Class<?>>asSingletonIterator(requirement.getClass());
    }

    @Override
    public void close() {
        try {
            removeBy(applicationDaemon());
        } finally {
            try {
                Runtime.getRuntime().removeShutdownHook(_shutdownHook);
            } catch (IllegalStateException ignored) {}
        }
    }

}

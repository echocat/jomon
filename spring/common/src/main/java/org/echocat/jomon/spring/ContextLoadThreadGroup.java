/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012-2014 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.spring;

import org.echocat.jomon.runtime.concurrent.StopWatch;
import org.echocat.jomon.runtime.util.Duration;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.*;

import static java.util.Collections.synchronizedMap;
import static java.util.Collections.synchronizedSet;
import static java.util.Collections.unmodifiableSet;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class ContextLoadThreadGroup implements ApplicationContextAware, Iterable<Thread>, UncaughtExceptionHandler {

    private final Map<Thread, List<Throwable>> _failedThreads = synchronizedMap(new HashMap<Thread, List<Throwable>>());
    private final Set<Thread> _threads = synchronizedSet(new HashSet<Thread>());

    private String _name;

    public void addAndStart(@Nonnull Thread thread) {
        thread.setUncaughtExceptionHandler(this);
        _threads.add(thread);
        thread.start();
    }

    public void join() throws InterruptedException {
        for (final Thread thread : this) {
            if (thread.isAlive()) {
                thread.join();
            }
        }
        throwRuntimeExceptionOn();
    }

    public boolean join(@Nonnull Duration duration) throws InterruptedException {
        boolean result = true;
        final Iterator<Thread> i = iterator();
        final StopWatch stopWatch = new StopWatch();
        while (result && i.hasNext()) {
            final Thread thread = i.next();
            if (thread.isAlive()) {
                thread.join(duration.minus(stopWatch.getCurrentDuration()).in(MILLISECONDS));
            }
            result = !thread.isAlive();
        }
        if (result) {
            throwRuntimeExceptionOn();
        }
        return result;
    }

    public void throwExceptionOn() throws Exception {
        throwOn(Exception.class);
    }

    public void throwRuntimeExceptionOn() throws RuntimeException {
        throwOn(RuntimeException.class);
    }

    public <T extends Throwable> void throwOn(@Nonnull Class<T> allowedThrowableType) throws T {
        synchronized (_failedThreads) {
            final Throwable highestRatedThrowable = findHighestRatedThrowableIn(_failedThreads);
            if (highestRatedThrowable != null) {
                for (final List<Throwable> throwables : _failedThreads.values()) {
                    for (final Throwable throwable : throwables) {
                        if (!highestRatedThrowable.equals(throwable)) {
                            highestRatedThrowable.addSuppressed(throwable);
                        }
                    }
                }
                if (allowedThrowableType.isInstance(highestRatedThrowable)) {
                    throw allowedThrowableType.cast(highestRatedThrowable);
                } else if (highestRatedThrowable instanceof RuntimeException) {
                    throw (RuntimeException) highestRatedThrowable;
                } else if (highestRatedThrowable instanceof Error) {
                    throw (Error) highestRatedThrowable;
                } else {
                    throw new RuntimeException(highestRatedThrowable);
                }
            }
        }
    }

    @Nullable
    protected Throwable findHighestRatedThrowableIn(@Nonnull Map<Thread, List<Throwable>> threadToExceptions) {
        Throwable result = null;
        for (final List<Throwable> exceptions : threadToExceptions.values()) {
            final Throwable current = findHighestRatedThrowableIn(exceptions);
            if (isHigherRated(current, result)) {
                result = current;
            }
        }
        return result;
    }

    @Nullable
    protected Throwable findHighestRatedThrowableIn(@Nonnull Iterable<Throwable> exceptions) {
        Throwable result = null;
        for (final Throwable exception : exceptions) {
            if (isHigherRated(exception, result)) {
                result = exception;
            }
        }
        return result;
    }

    protected boolean isHigherRated(@Nonnull Throwable what, @Nullable Throwable inRelationTo) {
        final boolean result;
        if (inRelationTo instanceof Error) {
            result = false;
        } else if (inRelationTo instanceof Throwable && !(inRelationTo instanceof Exception)) {
            result = what instanceof Error;
        } else if (inRelationTo != null) {
            result = what instanceof Error || (what instanceof Throwable && !(what instanceof Exception));
        } else {
            result = true;
        }
        return result;
    }

    public boolean isAtLeastOneThreadAlive() {
        boolean result = false;
        final Iterator<Thread> i = iterator();
        while (!result && i.hasNext()) {
            result = i.next().isAlive();
        }
        return result;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (_name == null) {
            _name = applicationContext.getDisplayName();
        }
    }

    @Override
    public Iterator<Thread> iterator() {
        return getThreads().iterator();
    }

    @Nonnull
    public Set<Thread> getThreads() {
        final Set<Thread> threads;
        synchronized (_threads) {
            threads = unmodifiableSet(new HashSet<>(_threads));
        }
        return threads;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    @Nonnull
    protected String getNameInternal() {
        final String name = _name;
        return name != null ? name : "ContextLoadThreadGroup";
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        synchronized (_failedThreads) {
            List<Throwable> throwables = _failedThreads.get(t);
            if (throwables == null) {
                throwables = new ArrayList<>();
                _failedThreads.put(t, throwables);
            }
            throwables.add(e);
        }
    }

    @Override
    public String toString() {
        return getNameInternal();
    }
}

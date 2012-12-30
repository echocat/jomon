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

package org.echocat.jomon.testing.concurrent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ParallelTestRunner {

    public static interface Worker {
        void run() throws Exception;
    }

    private static class WorkerThread extends Thread {
        private final Worker _worker;
        private Throwable _throwable;

        private WorkerThread(String name, Worker worker) {
            super(name);
            _worker = worker;
        }

        @Override
        public void run() {
            try {
                _worker.run();
            } catch (Throwable t) {
                _throwable = t;
            }
        }

        public Throwable getThrowable() {
            return _throwable;
        }
    }

    private final List<Worker> _workers;

    public ParallelTestRunner(List<Worker> workers) {
        _workers = workers;
    }

    public static void run(@Nonnull List<Worker> workers) {
        new ParallelTestRunner(workers).run();
    }

    public void run() {
        final List<WorkerThread> threads = new ArrayList<>(_workers.size());
        int i = 1;
        for (Worker worker : _workers) {
            final WorkerThread thread = new WorkerThread("WorkerThread #" + i, worker);
            ++i;
            threads.add(thread);
            thread.start();
        }
        for (WorkerThread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ignored) {
                interrupt(threads);
            }
            final Throwable e = thread.getThrowable();
            if (e != null) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else if (e instanceof Error) {
                    throw (Error) e;
                } else {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void interrupt(List<WorkerThread> threads) {
        for (WorkerThread thread : threads) {
            thread.interrupt();
        }
    }

}

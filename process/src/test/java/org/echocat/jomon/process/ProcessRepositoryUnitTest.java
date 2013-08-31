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

import org.echocat.jomon.runtime.iterators.CloseableIterator;
import org.echocat.jomon.testing.environments.LogEnvironment;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import static org.echocat.jomon.process.ProcessQuery.query;

public class ProcessRepositoryUnitTest {

    @Rule
    public final LogEnvironment _logEnvironment = new LogEnvironment();

    @Test
    public void testGetInstance() throws Exception {
        final ProcessRepository repository = ProcessRepository.getInstance();
        try (final CloseableIterator<Process> i = repository.findBy(query())) {
            while (i.hasNext()) {
                final Process process = i.next();
                final File executable = process.getExecutable();
                if (executable != null) {
                    executable.getCanonicalPath();
                }
            }
        }
    }

}

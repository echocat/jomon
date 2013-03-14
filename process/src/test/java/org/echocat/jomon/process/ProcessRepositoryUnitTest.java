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

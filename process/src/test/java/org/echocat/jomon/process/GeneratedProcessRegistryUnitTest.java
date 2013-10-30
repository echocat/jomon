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

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.annotation.Nonnull;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class GeneratedProcessRegistryUnitTest {

    @Test
    public void testRegister() throws Exception {
        try (final GeneratedProcessRegistry<Long, GeneratedProcess<Object, Long>> registry = new GeneratedProcessRegistry<>(666, "local", Long.class)) {
            registry.clear();
            registry.register(daemonProcess(1));
            registry.register(daemonProcess(2));
            registry.register(process(3));
            registry.register(daemonProcess(4));
            final Set<Long> ids = registry.getAllIds();
            assertThat(ids.size(), is(3));
            assertThat(ids.contains(1L), is(true));
            assertThat(ids.contains(2L), is(true));
            assertThat(ids.contains(4L), is(true));
        }
    }

    @Nonnull
    protected static GeneratedProcess<Object, Long> process(long id) {
        // noinspection unchecked
        final GeneratedProcess<Object, Long> process = mock(GeneratedProcess.class);
        doReturn(id).when(process).getId();
        return process;
    }

    @Nonnull
    protected static GeneratedProcess<Object, Long> daemonProcess(long id) throws Exception {
        final GeneratedProcess<Object, Long> process = process(id);
        doReturn(true).when(process).isDaemon();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(100);
                return 0;
            }
        }).when(process).waitFor();
        return process;
    }
}

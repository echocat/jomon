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

package org.echocat.jomon.net;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.NoSuchElementException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class FreeTcpPortDetectorUnitTest {

    @Test
    public void testDetect() throws Exception {
        final InetAddress localHost = InetAddress.getByName("localhost");
        final FreeTcpPortDetector detector = new FreeTcpPortDetector(localHost, 40000, 65000);
        final int port = detector.detect();
        assertThat(port, isBetween(40000, 65000));

        final FreeTcpPortDetector detector2 = new FreeTcpPortDetector(localHost, port, port);
        final ServerSocket socket = new ServerSocket(port, 50, localHost);
        // noinspection TryFinallyCanBeTryWithResources
        try {
            try {
                detector2.detect();
                fail("Exception missing.");
            } catch (NoSuchElementException expected) {}
        } finally {
            socket.close();
        }
        assertThat(detector2.detect(), equalTo(port));
    }

    private BaseMatcher<Integer> isBetween(final int min, final int max) {
        return new BaseMatcher<Integer>() {
            @Override
            public boolean matches(Object o) {
                return o instanceof Integer && (Integer) o >= min && (Integer) o <= max;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is between ").appendValue(min).appendText(" and ").appendValue(max);
            }
        };
    }
}

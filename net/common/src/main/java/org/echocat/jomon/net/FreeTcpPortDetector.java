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

import org.echocat.jomon.runtime.annotations.Excluding;
import org.echocat.jomon.runtime.annotations.Including;
import org.echocat.jomon.runtime.numbers.IntegerRange;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.*;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

public class FreeTcpPortDetector {

    public static final int DEFAULT_MIN_PORT = 10000;
    public static final int DEFAULT_MAX_PORT = 45000;

    private final Random _random = new Random();
    private final InetAddress _address;
    private final int _from;
    private final int _to;
    private final int _range;

    public FreeTcpPortDetector(@Nullable InetAddress address, @Including @Nonnegative int minPort, @Excluding @Nonnegative int maxPort) {
        if (minPort > maxPort) {
            throw new IllegalArgumentException("The maxPort have to larger than or equal to the minPort.");
        }
        _address = address;
        _from = minPort;
        _to = maxPort;
        _range = maxPort - minPort;
    }

    public FreeTcpPortDetector(@Nullable InetAddress address, @Nullable IntegerRange portRange) {
        _address = address;
        _from = portRange != null ? portRange.getFrom(DEFAULT_MIN_PORT) : DEFAULT_MIN_PORT;
        _to = portRange != null ? portRange.getTo(DEFAULT_MAX_PORT) : DEFAULT_MAX_PORT;
        _range = _to - _from;
    }

    @Nonnegative
    public int detect() throws NoSuchElementException {
        final Set<Integer> alreadyTriedPorts = new HashSet<>();
        int tries = 0;
        Integer port = null;
        while (port == null && tries <= _range) {
            final int currentPort = _range > 0 ? _random.nextInt(_to - _from) + _from : _from;
            if (!alreadyTriedPorts.contains(currentPort)) {
                tries++;
                alreadyTriedPorts.add(currentPort);
                try {
                    final ServerSocket socket = new ServerSocket();
                    socket.bind(new InetSocketAddress(_address, currentPort));
                    socket.close();
                    port = currentPort;
                } catch (BindException ignored) {
                } catch (IOException e) {
                    throw new RuntimeException("Could not find a free port on '" + _address + "' between ports " + _from + " and " + _to + ".", e);
                }
            }
        }
        if (port == null) {
            throw new NoSuchElementException("Could not find a free port on '" + _address + "' between ports " + _from + " and " + _to + ".");
        }
        return port;
    }
}
